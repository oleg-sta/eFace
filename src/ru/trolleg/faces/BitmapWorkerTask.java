package ru.trolleg.faces;

import java.lang.ref.WeakReference;

import ru.trolleg.faces.data.Face;
import ru.trolleg.faces.data.InfoPhoto;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final ProgressBar bar;
    private String data = "";
    InfoPhoto info;
    Context context;

    public BitmapWorkerTask(ImageView imageView, ProgressBar bar) {
        this(imageView, bar, null);
    }

    public BitmapWorkerTask(ImageView imageView, ProgressBar bar, InfoPhoto infoPh) {
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.bar = bar;
        this.info = infoPh;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        data = params[0];
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap myBitmap = FaceFinderService.decodeSampledBitmapFromResource(data, DataHolder.SIZE_PHOTO_TO_FIND_FACES, DataHolder.SIZE_PHOTO_TO_FIND_FACES, options, true);

        if (info == null || myBitmap == null) {
            return myBitmap;
        }
        android.graphics.Bitmap.Config bitmapConfig = myBitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        myBitmap = myBitmap.copy(bitmapConfig, true);

        int heightImg = myBitmap.getHeight();
        int widthImg = myBitmap.getWidth();
        
        Canvas canvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setAlpha(100);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0);
        for (int i = 0; i < info.faceCount; i++) {
            Face face = info.faces[i];
            int heightFace = (int) (face.height * heightImg) / 100;
            int widthFace = (int) (face.width * widthImg) / 100;
            int x = (int) (face.centerX * widthImg) / 100 - widthFace / 2;
            int y = (int) (face.centerY * heightImg) / 100 - heightFace / 2;
            canvas.drawRect(x, y, x + widthFace, y + heightFace, paint);
        }
        return myBitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                bar.setVisibility(ProgressBar.INVISIBLE);
            }
        }
    }

}
