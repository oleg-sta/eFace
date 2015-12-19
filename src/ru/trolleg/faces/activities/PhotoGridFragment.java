package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.GridPhotosAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * просмотр всех фотографий альбома
 * 
 * @author sov
 *
 */
public class PhotoGridFragment extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_photo_fragment);
        final Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        final String albumId = getIntent().getStringExtra(DataHolder.ALBUM_ID);
        List<String> photosId = MainActivity.getCameraImages(this, albumId);
        final PhotoGridFragment d =this;
        //DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        //List<String> photosId = dbHelper.convertByNameToIds(photosNames);
        GridView photos = (GridView) findViewById(R.id.gallery_photos);
        Log.i("s",  "" + photos);
        photos.setAdapter(new GridPhotosAdapter(this, photosId));
        photos.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(d, PhotoGalleryCommon.class);
                in.putExtra(DataHolder.ALBUM_ID, albumId);
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
        
    }

}
