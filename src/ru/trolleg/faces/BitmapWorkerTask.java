package ru.trolleg.faces;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final ProgressBar bar;
    private String data = "";

    public BitmapWorkerTask(ImageView imageView, ProgressBar bar) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.bar = bar; 
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        data = params[0];
        final BitmapFactory.Options options = new BitmapFactory.Options();
        return FaceFinderService.decodeSampledBitmapFromResource(data, 500, 500, options);
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
