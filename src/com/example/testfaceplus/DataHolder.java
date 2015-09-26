package com.example.testfaceplus;

import java.util.HashMap;
import java.util.Map;

import com.example.testfaceplus.data.Face;
import com.example.testfaceplus.data.InfoPhoto;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

public class DataHolder {

    private static LruCache<String, Bitmap> mMemoryCache;

    // TODO ������ infos
    public Map<String, InfoPhoto> infos = new HashMap<String, InfoPhoto>();

    private static final DataHolder holder = new DataHolder();

    public static DataHolder getInstance() {
        // TODO ������������ ��� ��� ������� �������
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
    
    // TODO ������ ������ � ������������� ������������
    public Bitmap getLittleFace(SQLiteDatabase db, String faceId) {
        Bitmap bm = mMemoryCache.get(faceId);
        if (bm == null) {
            Face faceCur = getFace(db, faceId);
            String path = getPathPhoto(db, faceCur.photoId);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap background_image = FaceFinderService.decodeSampledBitmapFromResource(path, 500, 500, options);
            bm = Bitmap.createBitmap(background_image, (int)(background_image.getWidth() * (faceCur.centerX - faceCur.width / 2) / 100), (int)(background_image.getHeight() * (faceCur.centerY - faceCur.height / 2) / 100), (int)(background_image.getWidth() * faceCur.width / 100) , (int)(background_image.getHeight() * faceCur.height / 100));
            mMemoryCache.put(faceId, bm);
        }
        return bm;
    }

    
}
