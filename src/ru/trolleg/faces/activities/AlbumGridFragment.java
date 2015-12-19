package ru.trolleg.faces.activities;

import java.util.List;
import java.util.Map;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.GridAlbumsAdapter;
import ru.trolleg.faces.data.Album;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class AlbumGridFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.album_fragment, container, false);
        //DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
        List<Album> albums = MainActivity.getBucketImages(getActivity());
        GridView photos = (GridView) rootView.findViewById(R.id.gallery_photos);
        photos.setAdapter(new GridAlbumsAdapter(getActivity(), albums));
        return rootView;
    }

}
