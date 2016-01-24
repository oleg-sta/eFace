package ru.trolleg.faces;

import java.lang.ref.WeakReference;

import ru.trolleg.faces.adapters.GridPhotosAdapter.ViewHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapWorkerCropPhotoTask extends AsyncTask<String, Void, BitmapDrawable> {
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
    protected BitmapDrawable doInBackground(String... params) {
        data = params[0];
        Bitmap myBitmap = DataHolder.getInstance().getLittleCropedPhoto(data, context);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), myBitmap);
        return drawable;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(BitmapDrawable bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ViewHolder imageView = imageViewReference.get();
            if (imageView != null && imageView.position == position && imageView.image != null) {
                imageView.image.setImageDrawable(bitmap);
            }
        }
    }
    
    public static void loadImage(String photo, Activity context, ViewHolder holder, int position) {
        BitmapDrawable value = null;
        Bitmap alue = DataHolder.getInstance().mMemoryCache.get(photo + "_" + DataHolder.FACES_SIZE);
        if (alue != null) {
            value = new BitmapDrawable(context.getResources(), alue);
        }
        if (value != null) {
            holder.image.setImageDrawable(value);
        } else if (cancelPotentialWork(photo, holder)) {
            holder.image.setImageBitmap(null);
            holder.image.setBackgroundColor(Color.GRAY);
            final BitmapWorkerCropPhotoTask task = new BitmapWorkerCropPhotoTask(holder, context, position);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), null, task);
            holder.image.setImageDrawable(asyncDrawable);
            task.execute(photo);
        }

    }
    
    // Остановлена ли для view загрузка
    public static boolean cancelPotentialWork(String photo, ViewHolder holder) {
        ImageView imageView = holder.image;
        final BitmapWorkerCropPhotoTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(photo)) {
                Log.i("BitmapWorkerCr", "cancel for photo " + bitmapData + " new " + photo);
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerCropPhotoTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

   
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerCropPhotoTask> bitmapWorkerTaskReference;
        @SuppressLint("NewApi")
        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerCropPhotoTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerCropPhotoTask>(bitmapWorkerTask);
        }
        public BitmapWorkerCropPhotoTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    

}
