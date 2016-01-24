package ru.trolleg.faces.adapters;

import java.util.ArrayList;
import java.util.List;

import ru.trolleg.faces.BitmapWorkerCropPhotoTask;
import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.PhotoGalleryCommon;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import ru.trolleg.faces.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;


public class GridPhotosAdapter extends ArrayAdapter<String> {

    private final Activity context;
    public final List<String> photos;
    public String albumId;
    
    public GridPhotosAdapter(Activity context, List<String> photos) {
        super(context, R.layout.one_squared_image, photos);
        this.photos = photos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return photos.size() + FacesGridAdapter.WIDTH_NUM_PICS;
    }


    @Override
    public int getItemViewType(int position) {
        return (position < FacesGridAdapter.WIDTH_NUM_PICS) ? 1 : 0;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public String getItem(int position) {
        return (position < FacesGridAdapter.WIDTH_NUM_PICS ) ?
                null : photos.get(position - FacesGridAdapter.WIDTH_NUM_PICS);
    }
    
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        final int position = pos - FacesGridAdapter.WIDTH_NUM_PICS;
        if (position < 0) {
            if (convertView == null) {
                convertView = new View(context);
            }
            convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 0));
            return convertView;
        }
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_squared_image, null, true);
            holder = new ViewHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final String photo = photos.get(position);
        holder.position = position;
        holder.image.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    Log.i("Data", "down");
                    ImageView view = (ImageView) v;
                    Drawable drawable = view.getDrawable();
                    boolean hasImage = (drawable != null);
                    if (hasImage && (drawable instanceof BitmapDrawable)) {
                        hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
                    }
                    if (hasImage) {
                        view.setBackgroundColor(Color.WHITE);
                        view.setPadding(5, 5, 5, 5);
                        view.invalidate();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    Log.i("Data", "up");
                    ImageView view = (ImageView) v;
                    view.setPadding(0, 0, 0, 0);
                    view.invalidate();
                    break;
                }
                }
                return false;
            }

        });
        holder.image.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent in = new Intent(context, PhotoGalleryCommon.class);
                Log.i("PhotoGalleryCommon", "put " + photos);
                in.putStringArrayListExtra(PhotoGalleryCommon.PHOTOS_ARRAY, new ArrayList(photos));
                in.putExtra(PhotoGalleryCommon.PHOTO_ID, position);
                context.startActivity(in);
            }
        });
        BitmapWorkerCropPhotoTask.loadImage(photo, context, holder, position);
        
        return convertView;
    }
    
    public static class ViewHolder {
        public ImageView image;
        public int position;
    }
}

