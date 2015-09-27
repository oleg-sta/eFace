package com.example.testfaceplus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
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
 * Сервис поиска лиц на фотографиях и их группировки. 
 * TODO проверка на wifi соединение
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
        try {
            // нельзя допускать повторного запуска
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
            
            DataHolder dataHolder = DataHolder.getInstance();
            if (dataHolder.processPhotos) {
                Log.d("FaceFinderService", "onHandleIntent is process now");
                return;
            }
            dataHolder.processPhotos = true;
            

            final ResultReceiver rec = (ResultReceiver) intent.getParcelableExtra("receiver");
            List<String> photos = MainActivity.getCameraImages(getApplicationContext());
            // положить фотки в БД
            int newFaces = dbHelper.addNewPhotos(photos);
            
            
            HttpRequests httpRequests = new HttpRequests("b6452a0139a94e5a2d7013b8d0146f01", "WU7c-QNzVteqO3JREDOkfcZXw-qj2CVp");
            
            // find faces on photos
            photos = dbHelper.getAllPhotosToBeProcessed();
            
            for (String photo : photos) {
                Log.d("service222", "photo" + photo);
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

                result = httpRequests.detectionDetect(new PostParameters().setImg(imageInByte));
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
                }

                // сообщения для UI о готовности фото
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    // Messenger messenger = (Messenger)
                    // bundle.get("receiver");
                    // Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putString("photo", photo);
                    rec.send(0, b);
                    // messenger.send(msg);
                }

            }
            // если нет новых лиц, то не надо ничего группировать
            if (newFaces == 0) {
                Log.d("FaceFinderService", "onHandleIntent no new photos");
                dataHolder.processPhotos = false;
                return;
            }
            dbHelper.removeGroups();
            String facesToRequest = "";
            Log.d("service222", "onStartCommand grouping faces");
            // grouping faces
            List<Face> faces = dbHelper.getAllFaces();
            for (Face face : faces) {
                if ("".equals(facesToRequest)) {
                    facesToRequest = face.guid;
                } else {
                    facesToRequest += "," + face.guid;
                }
            }

            if (!"".equals(facesToRequest)) {
                FaceppResult result = httpRequests.request("faceset", "create", new PostParameters().setFaceId(facesToRequest));
                String faceSet = result.get("faceset_id", JsonType.STRING).toString();

                result = httpRequests.request("grouping", "grouping", new PostParameters().setFaceSetId(faceSet));
                String sessId = result.get("session_id").toString();

                Log.d("service222", "onStartCommand get grouping result");
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
                            dbHelper.addPerson("group"+i);
                            
                            FaceppResult group = groupRes.get("group").getArray(i);
                            for (int j = 0; j < group.getCount(); ++j) {
                                String faceId = group.get(j).get("face_id").toString();
                                dbHelper.addFaceToPerson(faceId, "group"+i);
                            }
                        }  
                        FaceppResult group = groupRes.get("ungrouped", JsonType.JSON.ARRAY);
                        dbHelper.addPerson("ungrouped");
                        //DataHolder.getInstance().catnames.add("ungrouped");
                        for (int j = 0; j < group.getCount(); ++j) {
                            String faceId = group.get(j).get("face_id").toString();
                            dbHelper.addFaceToPerson(faceId, "ungrouped");
                        }
                        dataHolder.processPhotos = false;
                        return;
                    }
                }

            }
            dataHolder.processPhotos = false;
        } catch (FaceppParseException | IOException e) {
            // TODO Auto-generated catch block
            Log.d("service222", "error" + e.getMessage());
            e.printStackTrace();
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

}
