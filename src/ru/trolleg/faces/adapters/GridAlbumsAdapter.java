package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.BitmapWorkerCropPhotoTask;
import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.PhotoGridFragment;
import ru.trolleg.faces.adapters.GridPhotosAdapter.ViewHolder;
import ru.trolleg.faces.data.Album;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personIntent = new Intent(context, PhotoGridFragment.class);
                personIntent.putExtra(DataHolder.ALBUM_ID, album.id);
                context.startActivity(personIntent);
                
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
    
    public static class ViewHolder extends ru.trolleg.faces.adapters.GridPhotosAdapter.ViewHolder {
        TextView textView;
    }
}