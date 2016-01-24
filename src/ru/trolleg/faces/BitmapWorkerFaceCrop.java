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
import android.widget.ImageView;

public class BitmapWorkerFaceCrop  extends AsyncTask<String, Void, BitmapDrawable> {
    private final WeakReference<ViewHolder2> imageViewReference;
    private String data = "";
    private final int position;
    Face face;
    Context context;
    boolean inCircle;

    public BitmapWorkerFaceCrop(ViewHolder2 holder, Context context, Face face, int position) {
        this(holder, context, face, position, false);
    }
    public BitmapWorkerFaceCrop(ViewHolder2 holder, Context context, Face face, int position, boolean inCircle) {
        imageViewReference = new WeakReference<ViewHolder2>(holder);
        this.face = face;
        this.context = context;
        this.position = position;
        this.inCircle = inCircle;
    }

    // Decode image in background.
    @Override
    protected BitmapDrawable doInBackground(String... params) {
        //data = params[0];
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap myBitmap;
        if (!inCircle) {
            myBitmap = DataHolder.getInstance().getLittleFace(db, face.guid, context);
        } else {
            myBitmap = DataHolder.getInstance().getLittleFaceInCirle(db, face.guid, context);
        }
        db.close();
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), myBitmap);
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
        loadImage(face, context, holder, position2, false);
    }
    public static void loadImage(Face face, Context context, ViewHolder2 holder, int position2, boolean inCircle) {
        BitmapDrawable value = null;
        String key = face.guid;
        if (inCircle) {
            key = face.guid + "_circle";
        }
        Bitmap alue = DataHolder.getInstance().mMemoryCache.get(key);
        if (alue != null) {
            value = new BitmapDrawable(context.getResources(), alue);
        }
        // TODO add disk cache
        if (value != null) {
            holder.view.setImageDrawable(value);
        } else if (cancelPotentialWork(face, holder)) {
            holder.view.setImageBitmap(null);
            if (!inCircle) {
                holder.view.setBackgroundColor(Color.GRAY);
            }
            final BitmapWorkerFaceCrop task = new BitmapWorkerFaceCrop(holder, context, face, position2, inCircle);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), null, task);
            holder.view.setImageDrawable(asyncDrawable);
            task.execute();
        }

    }
    
    // Остановлена ли для view загрузка
    public static boolean cancelPotentialWork(Face face2, ViewHolder2 holder) {
        ImageView imageView = holder.view;
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