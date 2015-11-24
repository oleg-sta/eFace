package ru.trolleg.faces;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.trolleg.faces.data.Face;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;


public class DataHolder {

    public static final int FACES_SIZE = 150;
    public static final int FACES_PADDING_MAIN = 2;
	public static int SIZE_PHOTO_TO_FIND_FACES = 500;
	public static final String FACE_ID = "faceId";
    public static final String PERSON_ID = "personId";
    // ��� ���������� ���
    private static LruCache<String, Bitmap> mMemoryCache;

    //public boolean processPhotos = false;

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
            face.photoId = c.getInt(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
        }
        c.close();
        return face;
    }
    
    public String getPathPhoto(SQLiteDatabase db, int photoId) {
        String res = null;
        Cursor c = db.rawQuery("select path from photos where id = "+photoId, null);
        if (c.moveToNext()) {
            res = c.getString(0);
        }
        c.close();
        return res;
    }
    
    public Bitmap getLittleFace(SQLiteDatabase db, String faceId, Context context) {
        Bitmap bm = mMemoryCache.get(faceId);
        Log.i("DataHolder", "faceId " + faceId);
        Log.i("DataHolder", "faceId " + context.getFilesDir().toString());
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
                Bitmap background_image = FaceFinderService.decodeSampledBitmapFromResource(path, SIZE_PHOTO_TO_FIND_FACES, SIZE_PHOTO_TO_FIND_FACES, options, true);
                if (background_image == null) {
                    Log.i("DataHolder", "null path " + path + " " + faceId + " " + faceCur.photoId);
                    return null;
                }
                Bitmap bmTmp = Bitmap.createBitmap(background_image, (int) (background_image.getWidth()
                        * (faceCur.centerX - faceCur.width / 2) / 100), (int) (background_image.getHeight()
                        * (faceCur.centerY - faceCur.height / 2) / 100),
                        (int) (background_image.getWidth() * faceCur.width / 100), (int) (background_image.getHeight()
                                * faceCur.height / 100));
                bm = getResizedBitmap(bmTmp, FACES_SIZE, FACES_SIZE);
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

    
    public Bitmap getLittleCropedPhoto(String photo, Context context) {
        Bitmap bm = mMemoryCache.get(photo);
        String toSave = new File(photo).getName() + photo.hashCode();
        Log.i("DataHolder", "getLittleCropedPhoto " + photo);
        Log.i("DataHolder", "getLittleCropedPhoto " + context.getFilesDir().toString());
        if (bm == null) {
            Log.i("DataHolder", "not in cache " + photo);
            File file = new File(context.getFilesDir(), toSave);
            if (file.exists()) {
                Log.i("DataHolder", "in file " + photo);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                try {
                    bm = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
                    Log.i("DataHolder", "got file " + photo);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Log.i("DataHolder", "cropping " + photo);
                final BitmapFactory.Options options = new BitmapFactory.Options();
                bm = FaceFinderService.decodeSampledBitmapFromResource(photo, 150, 150, options, true);
                if (bm == null) {
                    return null;
                }
                // обрезаем по квадратику
                int height = bm.getHeight();
                int width = bm.getWidth();
                int x = 0;
                int y = 0;
                if (width > height) {
                    x = (width - height) / 2;
                    width = height;
                } else {
                    y = (height - width) / 2;
                    height = width;
                }
                bm = Bitmap.createBitmap(bm, x, y, width, height);
                bm = getResizedBitmap(bm, FACES_SIZE, FACES_SIZE);
                file = new File(context.getFilesDir(), toSave);
                try {
                    if (file.createNewFile()) {
                        Log.i("DataHolder", "saving " + photo);
                        FileOutputStream os = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                        Log.i("DataHolder", "saved " + photo);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            mMemoryCache.put(photo, bm);
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
    
    public static int px2Dp(int px, Context ctx)
    {
        return (int)(px / ctx.getResources().getDisplayMetrics().density);
    }
 
    public static int dp2Px(int dp, Context ctx)
    {
        return (int)(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
