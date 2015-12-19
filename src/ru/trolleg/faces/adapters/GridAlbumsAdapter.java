package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import ru.trolleg.faces.activities.PhotoGridFragment;
import ru.trolleg.faces.data.Album;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face_and_name, null, true);
        }
        final Album album = albums.get(position);
        ImageView im = (ImageView) convertView.findViewById(R.id.one_face1);
        im.setImageBitmap(DataHolder.getInstance().getLittleCropedPhoto(album.firstImage, context));
        TextView s = (TextView) convertView.findViewById(R.id.name_face);
        s.setText(album.name + "(" + album.count + ")");
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
    

}

