package com.example.testfaceplus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.example.testfaceplus.data.Face;
import com.example.testfaceplus.data.InfoPhoto;
import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.facepp.result.FaceppResult;
import com.facepp.result.FaceppResult.JsonType;

/**
 * ������ ������ ��� �� ����������� � �� �����������. 
 * TODO �������� �� wifi ����������
 * 
 * @author sov
 *
 */
public class FaceFinderService extends IntentService {

    public FaceFinderService() {
        super("FaceFinderService");
    }

    public FaceFinderService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("FaceFinderService", "onHandleIntent");
        Bundle bundle = null;
        ResultReceiver rec = null;
        try {
            // ������ ��������� ���������� �������
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
            
            DataHolder dataHolder = DataHolder.getInstance();
            if (dataHolder.processPhotos) {
                Log.d("FaceFinderService", "onHandleIntent is process now");
                return;
            }
            dataHolder.processPhotos = true;
            bundle = intent.getExtras();

            rec = (ResultReceiver) intent.getParcelableExtra("receiver");
            List<String> photos = MainActivity.getCameraImages(getApplicationContext());
            // �������� ����� � ��
            int newFaces = dbHelper.addNewPhotos(photos);
            
            
            HttpRequests httpRequests = new HttpRequests("b6452a0139a94e5a2d7013b8d0146f01", "WU7c-QNzVteqO3JREDOkfcZXw-qj2CVp");
            
            // find faces on photos
            photos = dbHelper.getAllPhotosToBeProcessed();
            
            int iPh = 0;
            for (String photo : photos) {
                if (bundle != null) {
                    Bundle b = new Bundle();
                    b.putString("progress", ((iPh * 100)/ (photos.size() + 5)) + "");
                    b.putString("message", iPh + " ���������� �� " + photos.size());
                    rec.send(0, b);
                }
                iPh++;
                Log.d("FaceFinderService", "photo" + photo);
                Bitmap littleBit = BitmapWorkerTask.shrinkBitmap(photo, 50, 50);

                BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
                bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
                // Bitmap background_image = BitmapFactory.decodeFile(photo,
                // bitmap_options);

                final BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap background_image = decodeSampledBitmapFromResource(photo, 500, 500, options);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                background_image.compress(CompressFormat.JPEG, 90, baos);
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                // use different methods
                // try {
                FaceppResult result;

                if (isAndroidEmulator() || isWifiOnline()) {
                    Log.v("FaceFinderService", "wifi is on");
                    result = httpRequests.detectionDetect(new PostParameters().setImg(imageInByte));
                } else {
                    // TODO return;
                    Log.v("FaceFinderService", "wifi is off");
                    dataHolder.processPhotos = false;
                    if (bundle != null) {
                        Bundle b = new Bundle();
                        b.putString("message", "wifi ��������");
                        rec.send(0, b);
                    }
                    return;
                }
                String imgId = result.get("img_id").toString();
                Face[] faces = new Face[result.get("face").getCount()];
                for (int i = 0; i < result.get("face").getCount(); ++i) {
                    if (i == 0) {
                        dbHelper.updatePhoto(photo, imgId);
                    }
                    FaceppResult face = result.get("face").get(i);
                    FaceppResult position = face.get("position");
                    Face faceCur = new Face();
                    faces[i] = faceCur;
                    faceCur.height = position.get("height").toDouble();
                    faceCur.width = position.get("width").toDouble();
                    faceCur.centerX = position.get("center").get("x").toDouble();
                    faceCur.centerY = position.get("center").get("y").toDouble();
                    faceCur.guid = face.get("face_id").toString();
                    dbHelper.addFace(faceCur, imgId);
                    // ��������� ����������
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    dataHolder.getLittleFace(db, faceCur.guid, getApplicationContext());
                    db.close();
                }

                // ��������� ��� UI � ���������� ����
                if (bundle != null) {
                    Bundle b = new Bundle();
                    b.putString("photo", photo);
                    rec.send(0, b);
                }
            }
            // ���� ��� ����� ���, �� �� ���� ������ ������������
            if (newFaces == 0) {
                Log.d("FaceFinderService", "onHandleIntent no new photos");
                dataHolder.processPhotos = false;
                return;
            }
            dbHelper.removeGroups();
            String facesToRequest = "";
            Log.d("FaceFinderService", "onStartCommand grouping faces");
            // grouping faces
            List<Face> faces = dbHelper.getAllFaces();
            for (Face face : faces) {
                if ("".equals(facesToRequest)) {
                    facesToRequest = face.guid;
                } else {
                    facesToRequest += "," + face.guid;
                }
            }

            if (bundle != null) {
                Bundle b = new Bundle();
                b.putString("message", "����������� ����������...");
                rec.send(0, b);
            }
            if (!"".equals(facesToRequest)) {
                FaceppResult result = httpRequests.request("faceset", "create", new PostParameters().setFaceId(facesToRequest));
                String faceSet = result.get("faceset_id", JsonType.STRING).toString();

                result = httpRequests.request("grouping", "grouping", new PostParameters().setFaceSetId(faceSet));
                String sessId = result.get("session_id").toString();

                Log.d("FaceFinderService", "onStartCommand get grouping result");
                for (int i1 = 0; i1 < 100; i1++) {
                    result = httpRequests.request("info", "get_session", new PostParameters().setSessionId(sessId));
                    System.out.println(result);
                    String res = result.get("status").toString();
                    if ("FAILED".equals(res)) {
                        dataHolder.processPhotos = false;
                        return;
                    } else if ("INQUEUE".equals(res)) {
                    } else if ("SUCC".equals(res)) {
                        FaceppResult groupRes = result.get("result");
                        for (int i = 0; i < groupRes.get("group").getCount(); ++i) {
                            String groupName = "������" + (i + 1); 
                            dbHelper.addPerson(groupName);
                            
                            FaceppResult group = groupRes.get("group").getArray(i);
                            for (int j = 0; j < group.getCount(); ++j) {
                                String faceId = group.get(j).get("face_id").toString();
                                dbHelper.addFaceToPerson(faceId, groupName);
                            }
                        }  
                        FaceppResult group = groupRes.get("ungrouped", JsonType.JSON.ARRAY);
                        String groupName = "�����������������";
                        dbHelper.addPerson(groupName);
                        //DataHolder.getInstance().catnames.add("ungrouped");
                        for (int j = 0; j < group.getCount(); ++j) {
                            String faceId = group.get(j).get("face_id").toString();
                            dbHelper.addFaceToPerson(faceId, groupName);
                        }
                        dataHolder.processPhotos = false;
                        return;
                    }
                }

            }
            dataHolder.processPhotos = false;
        } catch (FaceppParseException | IOException e) {
            // TODO Auto-generated catch block
            Log.d("FaceFinderService", "error" + e.getMessage());
            e.printStackTrace();
        } finally {
            DataHolder.getInstance().processPhotos = false;
            if (bundle != null) {
                Bundle b = new Bundle();
                b.putString("progress", "100");
                b.putString("message", "���������");
                rec.send(0, b);
            }
            // TODO send status message
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String photo, int reqWidth, int reqHeight, Options options) {

        // First decode with inJustDecodeBounds=true to check dimensions
        // final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(photo, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    
    /**
     * ���� �� wifi ����������
     * @return
     */
    public boolean isWifiOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo != null && networkInfo.isConnected());
    }
    public static boolean isAndroidEmulator() {
        String model = Build.MODEL;
        //Log.d(TAG, "model=" + model);
        String product = Build.PRODUCT;
        //Log.d(TAG, "product=" + product);
        boolean isEmulator = false;
        if (product != null) {
            isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
        }
        //Log.d(TAG, "isEmulator=" + isEmulator);
        return isEmulator;
    }

}
