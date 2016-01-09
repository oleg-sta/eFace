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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.support.v4.util.LruCache;
import android.util.Log;


public class DataHolder {

    private static final String TAG = DataHolder.class.getSimpleName();
    public static final double FACE_MORE = 1.5d;
    public static final int FACES_SIZE = 150;
    public static final int FACES_PADDING_MAIN = 2;
	public static int SIZE_PHOTO_TO_FIND_FACES = 500;
	public static final String FACE_ID = "faceId";
	public static final String ALBUM_ID = "albumId";
    public static final String PERSON_ID = "personId";
    public static LruCache<String, Bitmap> mMemoryCache;
    public static boolean debugMode = true;
    
    public static int photoCount = -1;
    public static int photoProcessedCount;
    public static int facesCount;

    private static final DataHolder holder = new DataHolder();

    public static DataHolder getInstance() {
        if (mMemoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 4;

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
    
    public Bitmap getLittleFaceInCirle(SQLiteDatabase db, String faceId, Context context) {
        String key = faceId + "_circle";
        Bitmap bm = mMemoryCache.get(key);
        if (bm != null) {
            return bm;
        }
        
        Bitmap squaredFace = getLittleFace(db, faceId, context);
        Bitmap output = Bitmap.createBitmap(squaredFace.getWidth(),
                squaredFace.getHeight(), Config.ARGB_8888);
        
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, squaredFace.getWidth(),
                squaredFace.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // paint.setColor(color);
        canvas.drawCircle(squaredFace.getWidth() / 2,
                squaredFace.getHeight() / 2, squaredFace.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(squaredFace, rect, rect, paint);
        mMemoryCache.put(key, output);
        return output;
        
    }
    public Bitmap getLittleFace(SQLiteDatabase db, String faceId, Context context) {
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
                    return null;
                }
            } else {
                //final BitmapFactory.Options options = new BitmapFactory.Options();
                
                
                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, tmpOptions);
                int width = tmpOptions.outWidth;
                int height = tmpOptions.outHeight;
                int width1 = width;
                int height1 = height;
                Log.i("DataHolder", path + " width " + width + " height " + height);
                
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options2.inPreferredConfig = Bitmap.Config.RGB_565;
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(path);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int orient = FaceFinderService.getOrient(orientation);
                if (orient % 2 == 1) {
                    int w1 = height1;
                    height1 = width1;
                    width1 = w1;
                }
                //Bitmap background_image = FaceFinderService.decodeSampledBitmapFromResource(path, SIZE_PHOTO_TO_FIND_FACES, SIZE_PHOTO_TO_FIND_FACES, options, true);
                if (width == 0) {
                    Log.i("DataHolder", "null path " + path + " " + faceId + " " + faceCur.photoId);
                    return null;
                }
                double faceWidth = faceCur.width * width1 / 100;
                double faceHeight = faceCur.height * height1 / 100;
                
                Log.i("DataHolder", "cut " + path+ " "+ faceWidth + " " + faceHeight);
                if (faceWidth > 15800 || faceHeight > 15800 || path.toUpperCase().endsWith(".BMP")) {
                    Log.i("DataHolder", "old way");
                    bm = getLittleFaceoldWay(path, faceCur);
                } else {
                    BitmapRegionDecoder bitmapRegionDecoder  = null;
                    try {
                        bitmapRegionDecoder = BitmapRegionDecoder.newInstance(path, false);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        return null;
                    }
                    Log.i("DataHolder", "new way");
                    RectF f = new RectF((float) (faceCur.centerX - faceCur.width / 2),
                            (float) (faceCur.centerY - faceCur.height / 2),
                            (float) (faceCur.centerX + faceCur.width / 2),
                            (float) (faceCur.centerY + faceCur.height / 2));
                    
                    Log.i("DataHolder", "rect before " + f.left + " " + f.right + " " + f.top + " " + f.bottom + " " + orient);
                    Matrix m = new Matrix();
                    // point is the point about which to rotate.
                    //m.setRotate(-orient * 90, 50, 50);
                    m.setRotate(-orient * 90, 50, 50);
                    m.mapRect(f);
                    
                    Log.i("DataHolder", "rect after " + f.left + " " + f.right + " " + f.top + " " + f.bottom);
                    float centerX = Math.abs(f.left + f.right) / 2;
                    float centerY = Math.abs(f.top + f.bottom) / 2;
                    float fWidth = Math.abs(f.right - f.left);
                    float fHeight = Math.abs(f.bottom - f.top);

                    double k1 = Math.min(2 * centerX / fWidth, 2 * (100 - centerX) / fWidth);
                    double k2 = Math.min(2 * centerY / fHeight, 2 * (100 - centerY) / fHeight);
                    double k = Math.min(k1, k2);
                    k = Math.min(FACE_MORE, k);
                    double faceCurWidth = fWidth * k;
                    double faceCurHeight = fHeight * k;
                    Log.i("sdsd", "dd " + k);
                    // TODO проверить на дисктретность
                    int x1 = (int) (width * (centerX - faceCurWidth / 2) / 100);
                    int y1 = (int) (height * (centerY - faceCurHeight / 2) / 100);
                    int x2 = x1 + (int) (width * faceCurWidth / 100);
                    int y2 = y1 + (int) (height * faceCurHeight / 100);

                    x1 = Math.max(x1, 0);
                    y1 = Math.max(y1, 0);
                    x2 = Math.min(x2, width);
                    y2 = Math.min(y2, height);

                    Rect rect = new Rect(x1, y1, x2, y2);

                    options2.inSampleSize = FaceFinderService.calculateInSampleSize(rect.right - rect.left, rect.bottom - rect.top, FACES_SIZE, FACES_SIZE);
                    Log.i("DataHolder", "sampleSize " + options2.inSampleSize + " " + (rect.right - rect.left));
                    // TODO если размер лица больше 800, то добавить sample size
                    Bitmap bmTmp = bitmapRegionDecoder.decodeRegion(rect, options2);
                    Log.i("DataHolder", "size " + bmTmp.getWidth() + " " + bmTmp.getHeight());
                    // Bitmap bmTmp = Bitmap.createBitmap(background_image, x1,
                    // y1,
                    // x2 - x1, y2 - y1);
                    //bm = getResizedBitmap(bmTmp, bmTmp.getWidth(), bmTmp.getHeight(), true, orient);

                    // Matrix matrix = new Matrix();
                    // matrix.postRotate(orient * 90);
                    //bm = bmTmp;
                    bm = getResizedBitmap(bmTmp, bmTmp.getWidth(), bmTmp.getHeight(), true, orient);
                    //bm = Bitmap.createBitmap(bm, 0, 0, bmTmp.getWidth(), bmTmp.getHeight(), matrix, true);
                }
                
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
    private Bitmap getLittleFaceoldWay(String path, Face faceCur) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap background_image = FaceFinderService.decodeSampledBitmapFromResource(path, SIZE_PHOTO_TO_FIND_FACES, SIZE_PHOTO_TO_FIND_FACES, options, true);
        if (background_image == null) {
            Log.i("DataHolder", "null path " + path + " " + " " + faceCur.photoId);
            return null;
        }
       
        double k1 = Math.min(2 * faceCur.centerX / faceCur.width, 2 * (100 -  faceCur.centerX) / faceCur.width);
        double k2 = Math.min(2 * faceCur.centerY / faceCur.height, 2 * (100 -  faceCur.centerY) / faceCur.height);
        double k = Math.min(k1, k2);
        k = Math.min(FACE_MORE, k);
        double faceCurWidth = faceCur.width * k;
        double faceCurHeight = faceCur.height * k;
        Log.i("sdsd", "dd " + k);
        // TODO проверить на дисктретность
        int x1 = (int) (background_image.getWidth() * (faceCur.centerX - faceCurWidth / 2) / 100);
        int y1 = (int) (background_image.getHeight() * (faceCur.centerY - faceCurHeight / 2) / 100);
        int x2 = x1 + (int) (background_image.getWidth() * faceCurWidth / 100);
        int y2 = y1 + (int) (background_image.getHeight() * faceCurHeight / 100);
        x1 = Math.max(x1, 0);
        y1 = Math.max(y1,  0);
        x2 = Math.min(x2, background_image.getWidth());
        y2 = Math.min(y2, background_image.getHeight());
        
        Bitmap bmTmp = Bitmap.createBitmap(background_image, x1, y1,
                x2 - x1, y2 - y1);
        Bitmap bm = getResizedBitmap(bmTmp, FACES_SIZE, FACES_SIZE);
        return bm;
    }

    public Bitmap getLittleCropedPhoto(String photo, Context context) {
        return getLittleCropedPhoto(photo, context, FACES_SIZE);
    }
    public Bitmap getLittleCropedPhoto(String photo, Context context, int size) {
        String key = photo + "_" + size;
        Bitmap bm = mMemoryCache.get(key);
        String toSave = new File(key).getName() + key.hashCode();
        if (bm == null) {
            File file = new File(context.getCacheDir(), toSave);
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
                bm = FaceFinderService.decodeSampledBitmapFromResource(photo, size, size, options, true);
                if (bm == null) {
                    return null;
                }
                Log.i(TAG, "photo " + photo + " size " + (1.0f * bm.getWidth() * bm.getHeight()) / (size * size));
                bm = ThumbnailUtils.extractThumbnail(bm, size, size);
                if (bm == null) {
                    return null;
                }
                Log.i(TAG, "photo " + photo + " size " + bm.getWidth() + " " + bm.getHeight() + " koef " + (1.0f * bm.getWidth() * bm.getHeight()) / (size * size));
                file = new File(context.getCacheDir(), toSave);
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
            mMemoryCache.put(key, bm);
        }
        return bm;
    }
    
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }
    
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean filter, int orient) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        matrix.postRotate(orient * 90);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, filter);
        if (bm != resizedBitmap) {
            bm.recycle();
        }
        return resizedBitmap;
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
        if (bm != resizedBitmap) {
            bm.recycle();
        }
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
