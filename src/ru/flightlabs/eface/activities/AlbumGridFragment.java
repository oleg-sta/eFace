package ru.flightlabs.eface.activities;

import java.util.List;

import ru.flightlabs.eface.R;
import ru.flightlabs.eface.adapters.GridAlbumsAdapter;
import ru.flightlabs.eface.data.Album;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class AlbumGridFragment extends Fragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.album_fragment, container, false);
        
        // TODO use filters

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
        switch (item.getItemId()) {
        case R.id.action_search2:
            Intent searchIntent = new Intent(getActivity(), SearchPhotoActivity.class);
            getActivity().startActivity(searchIntent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }

    }
}
