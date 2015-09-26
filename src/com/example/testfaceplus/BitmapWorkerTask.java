package com.example.testfaceplus;

import java.lang.ref.WeakReference;
import java.util.Map;

import com.example.testfaceplus.data.Face;
import com.example.testfaceplus.data.InfoPhoto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.TextView;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    public static final int MAX_FACES = 10;

    //private LruCache<String, Bitmap> mMemoryCache;
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<TextView> txtViewReference;
    //private final WeakReference<LruCache<String, InfoPhoto>> mMemoryCacheReference;
    
    private String data = null;
    private int faceCount;
    Face[] faces = null;

    public BitmapWorkerTask(ImageView imageView, TextView numFaces) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        //this.mMemoryCacheReference = new WeakReference<LruCache<String, InfoPhoto>>(mMemoryCache);
        txtViewReference = new WeakReference<TextView>(numFaces);
    }

    // Decode image in background.
    // не используется
    @Override
    protected Bitmap doInBackground(String... params) {
        data = params[0];
        
        Log.v("BitmapWorkerTask", "file " + data);
        // нахождение лица
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap background_image = BitmapFactory.decodeFile(data, bitmap_options);
        FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(), 10);
        faces = new Face[MAX_FACES];
        //faceCount = face_detector.findFaces(background_image, faces);
        Log.v("BitmapWorkerTask", "faces " + faceCount);
        // уменьшение картинки
        return shrinkBitmap( data, 50, 50);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final Map<String, InfoPhoto> mMemoryCache = DataHolder.getInstance().infos;
            final TextView txtView = txtViewReference.get();
            if (imageView != null) {
                if (mMemoryCache != null) {
                    InfoPhoto infoPhoto = new InfoPhoto();
                    infoPhoto.littlePhoto = bitmap;
                    infoPhoto.faces = faces;
                    infoPhoto.faceCount = faceCount;
                    mMemoryCache.put(data, infoPhoto);
                }
                imageView.setImageBitmap(bitmap);
                txtView.setText("" + faceCount);
            }
        }
    }
    
    public static Bitmap shrinkBitmap(String file, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }
}