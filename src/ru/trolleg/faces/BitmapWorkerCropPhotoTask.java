package ru.trolleg.faces;

import java.lang.ref.WeakReference;

import ru.trolleg.faces.adapters.FacesGridAdapter.ViewHolder2;
import ru.trolleg.faces.adapters.GridPhotosAdapter.ViewHolder;
import ru.trolleg.faces.data.Face;
import ru.trolleg.faces.data.InfoPhoto;
import android.app.Activity;
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
    private final WeakReference<ViewHolder> imageViewReference;
    private final int position;
    private String data = "";
    Context context;

    public BitmapWorkerCropPhotoTask(ViewHolder holder, Activity context2, int position) {
        imageViewReference = new WeakReference<ViewHolder>(holder);
        this.context = context2;
        this.position = position;
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
            final ViewHolder imageView = imageViewReference.get();
            if (imageView != null && imageView.position == position && imageView.image != null) {
                imageView.image.setImageBitmap(bitmap);
                imageView.bar.setVisibility(ProgressBar.INVISIBLE);
            }
        }
    }
    
    public static void loadImage(String photo, Activity context, ViewHolder holder, int position) {
        Bitmap value = null;
        value = DataHolder.mMemoryCache.get(photo);
        if (value != null) {
            holder.image.setImageBitmap(value);
            holder.bar.setVisibility(ProgressBar.INVISIBLE);
        } else {
            holder.image.setImageBitmap(null);
            holder.image.setBackgroundColor(Color.GRAY);
            final BitmapWorkerCropPhotoTask task = new BitmapWorkerCropPhotoTask(holder, context, position);
            task.execute(photo);
        }

    }

}
