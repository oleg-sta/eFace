package ru.flightlabs.eface.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.flightlabs.eface.BitmapWorkerCropPhotoTask;
import ru.flightlabs.eface.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class HorizontalPhotoAdapter extends ArrayAdapter<String> {
    
    public int selected = -1;
    private final Activity context;
    public final List<String> photos;
    public Map<Integer, ViewHolder> forUpdate = new HashMap<Integer, ViewHolder>();
    
    public HorizontalPhotoAdapter(Activity context, List<String> faces) {
        super(context, R.layout.one_face2, faces);
        this.photos = faces;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face2, null, true);
            holder = new ViewHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.one_face1);
            holder.view2 = (ImageView)convertView.findViewById(R.id.one_face2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        forUpdate.put(position, holder);
        final String faceId = photos.get(position);
        BitmapWorkerCropPhotoTask.loadImage(faceId, context, holder, position);
        if (position != selected) {
            holder.view2.setVisibility(View.INVISIBLE);
        } else {
            holder.view2.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
    
    public static class ViewHolder extends GridPhotosAdapter.ViewHolder {
        public ImageView view2;
    }

}
