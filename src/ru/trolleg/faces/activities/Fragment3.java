package ru.trolleg.faces.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter;
import ru.trolleg.faces.adapters.GridPhotosAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment3 extends Fragment {
    
    public Set<Integer> filterMan;
    public Date startDate;
    public Date endDate;
    
    FragmentAlbumManager fragmentAlbumManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photo_searched, null);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
        final List<String> photos2 = dbHelper.getPhotoIds(filterMan, startDate, endDate);
        GridView photos = (GridView) v.findViewById(R.id.gallery_photos);
        photos.setNumColumns(FacesGridAdapter.WIDTH_NUM_PICS);
        photos.setAdapter(new GridPhotosAdapter(getActivity(), photos2));
        
        photos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getActivity(), PhotoGalleryCommon.class);
                in.putStringArrayListExtra("photos_array", new ArrayList(photos2));
                in.putExtra(PhotoGalleryCommon.PHOTO_ID, position - FacesGridAdapter.WIDTH_NUM_PICS);
                startActivity(in);
                
            }
        });
        
        return v;
    }
}