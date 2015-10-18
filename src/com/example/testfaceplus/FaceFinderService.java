package com.example.testfaceplus;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.example.testfaceplus.data.Face;

import detection.Detector;
import detection.Rectangle;

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
        Bundle bundle = null;
        ResultReceiver rec = null;
        try {
            // нельзя допускать повторного запуска1
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
            
            DataHolder dataHolder = DataHolder.getInstance();
            if (dataHolder.processPhotos) {
                Log.d("FaceFinderService", "onHandleIntent is process now");
                return;
            }
            dataHolder.processPhotos = true;
            bundle = intent.getExtras();

            rec = (ResultReceiver) intent.getParcelableExtra("receiver");
            boolean useCpp = intent.getBooleanExtra("useCpp", false);
            Log.d("FaceFinderService", "onHandleIntent useCpp " + useCpp);
            int threadsNum = intent.getIntExtra("threads", 1);
            Log.d("FaceFinderService", "onHandleIntent threads " + threadsNum);
            List<String> photos = MainActivity.getCameraImages(getApplicationContext());
            // положить фотки в БД
            int newFaces = dbHelper.addNewPhotos(photos);
            
            
            // find faces on photos
            photos = dbHelper.getAllPhotosToBeProcessed();
            
            Log.d("FaceFinderService", "loading casade...");
            InputStream inputHaas = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            Detector detector = Detector.create(inputHaas);
            Log.d("FaceFinderService", "casade loaded");
            inputHaas.close();
            int iPh = 0;
            for (String photo : photos) {
                if (bundle != null) {
                    Bundle b = new Bundle();
                    b.putString("progress", ((iPh * 100)/ (photos.size() + 5)) + "");
                    b.putString("message", iPh + " обработано из " + photos.size());
                    rec.send(0, b);
                }
                iPh++;
                Log.d("FaceFinderService", "photo" + photo);

                BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
                bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;

                final BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap background_image = decodeSampledBitmapFromResource(photo, 200, 200, options);

                Log.i("FaceFinderService", "size " + background_image.getWidth() + " " + background_image.getHeight());
                long time = System.currentTimeMillis();
                List<Rectangle> res = detector.getFaces(background_image, 1.2f, 1.1f, .05f, 2, true, useCpp, threadsNum);
                time = (System.currentTimeMillis() - time) / 1000;
                Log.i("FaceFinderService", "foune " + res.size() + " faces");
                
                String imgId = UUID.randomUUID().toString();
                Face[] faces = new Face[res.size()];
                if (res.size() == 0) {
                    dbHelper.updatePhoto(photo, time);
                }
                for (int i = 0; i < res.size(); ++i) {
                    if (i == 0) {
                        dbHelper.updatePhoto(photo, imgId, time);
                    }
                    //FaceppResult face = result.get("face").get(i);
                    Rectangle face = res.get(i);
                    //FaceppResult position = face.get("position");
                    Face faceCur = new Face();
                    faces[i] = faceCur;
                    faceCur.height = 100 * face.height / (double) background_image.getHeight();
                    faceCur.width = 100 * face.width / (double) background_image.getWidth();
                    faceCur.centerY = 100 * (face.y + face.height / 2) / (double) background_image.getHeight();
                    faceCur.centerX = 100 * (face.x + face.width / 2) / (double) background_image.getWidth();
                    faceCur.guid = UUID.randomUUID().toString();
                    dbHelper.addFace(faceCur, imgId);
                    String personGuid = UUID.randomUUID().toString();
                    dbHelper.addPerson(personGuid);
                    dbHelper.addFaceToPerson(faceCur.guid, personGuid);
                    // сохраняем фотографию
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    dataHolder.getLittleFace(db, faceCur.guid, getApplicationContext());
                    db.close();
                }

                // сообщения для UI о готовности фото
                if (bundle != null) {
                    Bundle b = new Bundle();
                    b.putString("photo", photo);
                    rec.send(0, b);
                }
            }
            dataHolder.processPhotos = false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("FaceFinderService", "error" + e.getMessage());
            e.printStackTrace();
        } finally {
            DataHolder.getInstance().processPhotos = false;
            if (bundle != null) {
                Bundle b = new Bundle();
                b.putString("progress", "100");
                b.putString("message", "Состояние");
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
     * Есть ли wifi соединения
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
