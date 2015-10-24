package com.example.testfaceplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.example.testfaceplus.data.Face;

public class DataHolder {

	public static int SIZE_PHOTO_TO_FIND_FACES = 500;
    // кэш фотографий лиц
    private static LruCache<String, Bitmap> mMemoryCache;

    public boolean processPhotos = false;
    // TODO убрать infos
    // public Map<String, InfoPhoto> infos = new HashMap<String, InfoPhoto>();

    private static final DataHolder holder = new DataHolder();

    public static DataHolder getInstance() {
        // TODO использовать кэш или файлами хранить
        if (mMemoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;

            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        return holder;
    }
    
    public Face getFace(SQLiteDatabase db, String faceId) {
        Face face = new Face();
        Cursor c = db.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY from faces where guid = '"+faceId+"'", null);
        if (c.moveToNext()) {
            face.guid = c.getString(0);
            face.photoId = c.getString(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
        }
        c.close();
        return face;
    }
    
    public String getPathPhoto(SQLiteDatabase db, String guid) {
        String res = null;
        Cursor c = db.rawQuery("select path from photos where guid = '"+guid+"'", null);
        if (c.moveToNext()) {
            res = c.getString(0);
        }
        c.close();
        return res;
    }
    
    // TODO методы работы с кэшированными фотографиями
    public Bitmap getLittleFace(SQLiteDatabase db, String faceId, Context context) {
        // TODO save to disc
        // TODO сохранения на диск делать не во время показа, делать во время поиска
        Bitmap bm = mMemoryCache.get(faceId);
        if (bm == null) {
            Face faceCur = getFace(db, faceId);
            String path = getPathPhoto(db, faceCur.photoId);
            File file = new File(context.getFilesDir(), faceId + ".jpg");
            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                try {
                    bm = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                // уменьшаем полную фотографию
                Bitmap background_image = FaceFinderService.decodeSampledBitmapFromResource(path, SIZE_PHOTO_TO_FIND_FACES, SIZE_PHOTO_TO_FIND_FACES, options);
                Bitmap bmTmp = Bitmap.createBitmap(background_image, (int) (background_image.getWidth()
                        * (faceCur.centerX - faceCur.width / 2) / 100), (int) (background_image.getHeight()
                        * (faceCur.centerY - faceCur.height / 2) / 100),
                        (int) (background_image.getWidth() * faceCur.width / 100), (int) (background_image.getHeight()
                                * faceCur.height / 100));
                bm = getResizedBitmap(bmTmp, FacesList.FACES_SIZE, FacesList.FACES_SIZE);
                Log.v("DataHolder", "file dir " + context.getFilesDir());
                file = new File(context.getFilesDir(), faceId + ".jpg");
                try {
                    if (file.createNewFile()) {
                        FileOutputStream os = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            mMemoryCache.put(faceId, bm);
        }
        return bm;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    /**
     * Получить полную фотографию для отображении в общем списке
     * 
     * @param path
     * @return
     */
    public Bitmap getLittlePhoto(String path) {
        Bitmap bm = mMemoryCache.get(path);
        if (bm == null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            bm = FaceFinderService.decodeSampledBitmapFromResource(path, 150, 150, options);
            mMemoryCache.put(path, bm);
        }
        return bm;
    }
    
}
