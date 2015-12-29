package ru.trolleg.faces.activities;

import java.util.List;
import java.util.Map;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.GridAlbumsAdapter;
import ru.trolleg.faces.data.Album;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;

public class AlbumGridFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.album_fragment, container, false);
        // DictionaryOpenHelper dbHelper = new
        // DictionaryOpenHelper(getActivity());
        List<Album> albums = MainActivity.getBucketImages(getActivity());
        GridView photos = (GridView) rootView.findViewById(R.id.gallery_photos);
        photos.setAdapter(new GridAlbumsAdapter(getActivity(), albums));
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_album, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }
}
