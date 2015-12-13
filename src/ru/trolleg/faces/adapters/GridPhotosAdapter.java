package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.BitmapWorkerCropPhotoTask;
import ru.trolleg.faces.R;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;


public class GridPhotosAdapter extends ArrayAdapter<String> {

    private final Activity context;
    public final List<String> photos;
    
    public GridPhotosAdapter(Activity context, List<String> photos) {
        super(context, R.layout.one_face, photos);
        this.photos = photos;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face, null, true);
            holder = new ViewHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.one_face1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        //holder.bar.setVisibility(ProgressBar.VISIBLE);
        //final ImageView view2 = (ImageView)convertView.findViewById(R.id.checked);
        //ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        final String photo = photos.get(position);
        holder.position = position;
        BitmapWorkerCropPhotoTask.loadImage(photo, context, holder, position);
//        final BitmapWorkerCropPhotoTask task = new BitmapWorkerCropPhotoTask(holder, context, position);
//        task.execute(photo);
        
        return convertView;
    }
    
    public static class ViewHolder {
        public ImageView image;
        public int position;
    }
}

