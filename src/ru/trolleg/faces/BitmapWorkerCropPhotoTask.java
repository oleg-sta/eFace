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

public class BitmapWorkerCropPhotoTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final ProgressBar bar;
    private String data = "";
    Context context;

    public BitmapWorkerCropPhotoTask(ImageView imageView, ProgressBar bar, Context context) {
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.bar = bar;
        this.context = context; 
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        data = params[0];
        //final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap myBitmap = DataHolder.getInstance().getLittleCropedPhoto(data, context);
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
