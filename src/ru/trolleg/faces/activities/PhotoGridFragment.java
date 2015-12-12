package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.GridPhotosAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PhotoGridFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gallery_photo_fragment, container, false);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
        List<String> photosId = dbHelper.getAllPhotos();
        GridView photos = (GridView) rootView.findViewById(R.id.gallery_photos);
        Log.i("s",  "" + photos);
        photos.setAdapter(new GridPhotosAdapter(getActivity(), photosId));
        photos.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getActivity(), PhotoGalleryCommon.class);
                in.putExtra(PhotoGalleryCommon.PHOTO_ID, position);
                startActivity(in);
                
            }
        });
//        TextView nameView = (TextView) rootView.findViewById(R.id.name_man);
//        final ViewPager mPager = (ViewPager) rootView.findViewById(R.id.pager);
//        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(getActivity());
//        List<String> photos = dbHelper.getAllPhotos();
//        final PagerAdapter mPagerAdapter = new CommonPhotoAdapter2(getActivity(), photos, nameView);
//        mPager.setAdapter(mPagerAdapter);
        
        return rootView;
    }

}
