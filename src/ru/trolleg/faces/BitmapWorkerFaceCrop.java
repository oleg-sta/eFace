package ru.trolleg.faces;

import java.lang.ref.WeakReference;



import ru.trolleg.faces.adapters.FacesGridAdapter.ViewHolder2;
import ru.trolleg.faces.data.Face;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class BitmapWorkerFaceCrop  extends AsyncTask<String, Void, BitmapDrawable> {
    private final WeakReference<ViewHolder2> imageViewReference;
    private String data = "";
    private final int position;
    Face face;
    Context context;

    public BitmapWorkerFaceCrop(ViewHolder2 holder, Context context, Face face, int position) {
        imageViewReference = new WeakReference<ViewHolder2>(holder);
        this.face = face;
        this.context = context;
        this.position = position;
    }

    // Decode image in background.
    @Override
    protected BitmapDrawable doInBackground(String... params) {
        //data = params[0];
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap myBitmap = DataHolder.getInstance().getLittleFace(db, face.guid, context);
        db.close();
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), myBitmap);
        DataHolder.mMemoryCache2.put(face.guid, drawable);
        return drawable;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(BitmapDrawable bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (imageViewReference != null && bitmap != null) {
            final ViewHolder2 holder = imageViewReference.get();
            // TODO похоже, что cancelPotentialWork решает проблему holder.position == position
            if (holder != null && holder.position == position && holder.view != null) {
                holder.view.setImageDrawable(bitmap);
            }
        }
    }

    public static void loadImage(Face face, Context context, ViewHolder2 holder, int position2) {
        BitmapDrawable value = null;
        value = DataHolder.mMemoryCache2.get(face.guid);
        // TODO add disk cache
        if (value != null) {
            holder.view.setImageDrawable(value);
        } else if (cancelPotentialWork(face, holder)) {
//            holder.view.setBackgroundColor(Color.GRAY);
//            final BitmapWorkerFaceCrop task = new BitmapWorkerFaceCrop(holder, context, face, position2);
//            task.execute();
            
            final BitmapWorkerFaceCrop task = new BitmapWorkerFaceCrop(holder, context, face, position2);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), null, task);
            holder.view.setImageDrawable(asyncDrawable);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.execute();
        }

    }
    
    // Остановлена ли для view загрузка
    public static boolean cancelPotentialWork(Face face2, ViewHolder2 holder) {
        ImageView imageView = holder.view;
        //BEGIN_INCLUDE(cancel_potential_work)
        final BitmapWorkerFaceCrop bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Face bitmapData = bitmapWorkerTask.face;
            if (bitmapData == null || bitmapData.guid == null || !bitmapData.guid.equals(face2.guid)) {
                bitmapWorkerTask.cancel(true);
                Log.i("BitmapWorkerFaceCrop", "cancelPotentialWork - cancelled work for " + face2.guid);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
        //END_INCLUDE(cancel_potential_work)
    }

    private static BitmapWorkerFaceCrop getBitmapWorkerTask(ImageView imageView) {
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
        private final WeakReference<BitmapWorkerFaceCrop> bitmapWorkerTaskReference;
        @SuppressLint("NewApi")
        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerFaceCrop bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerFaceCrop>(bitmapWorkerTask);
        }
        public BitmapWorkerFaceCrop getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}