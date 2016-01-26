package ru.flightlabs.eface.adapters;

import java.util.List;

import ru.flightlabs.eface.BitmapWorkerCropPhotoTask;
import ru.flightlabs.eface.DataHolder;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.activities.PhotoGridFragment;
import ru.flightlabs.eface.data.Album;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import ru.flightlabs.eface.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAlbumsAdapter extends BaseAdapter {

    private final Activity context;
    public final List<Album> albums;
    
    public GridAlbumsAdapter(Activity context, List<Album> albums) {
        this.albums = albums;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.album_name, null, true);
            holder = new ViewHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.one_face1);
            holder.textView = (TextView) convertView.findViewById(R.id.name_face);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        final Album album = albums.get(position);
        holder.textView.setText(album.name + "(" + album.count + ")");
        BitmapWorkerCropPhotoTask.loadImage(album.firstImage, context, holder, position);
        holder.image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personIntent = new Intent(context, PhotoGridFragment.class);
                personIntent.putExtra(DataHolder.ALBUM_ID, album.id);
                context.startActivity(personIntent);
                
            }
        });
        holder.image.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())  {
                case MotionEvent.ACTION_DOWN: {
                    Log.i("Data", "down");
                    ImageView view = (ImageView) v;
                    Drawable drawable = view.getDrawable();
                    boolean hasImage = (drawable != null);
                    if (hasImage && (drawable instanceof BitmapDrawable)) {
                        hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
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
        return convertView;
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public static class ViewHolder extends ru.flightlabs.eface.adapters.GridPhotosAdapter.ViewHolder {
        TextView textView;
    }
}