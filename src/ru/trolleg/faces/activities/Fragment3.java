package ru.trolleg.faces.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Fragment3 extends AppCompatActivity {
    
    public Set<Integer> filterMan;
    public Date startDate;
    public Date endDate;
    
    FragmentAlbumManager fragmentAlbumManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_searched);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

        if (getIntent().hasExtra("startDate")) {
            startDate = new Date(getIntent().getLongExtra("startDate", 0));
        }
        if (getIntent().hasExtra("endDate")) {
            endDate = new Date(getIntent().getLongExtra("endDate", 0));
        }
        if (getIntent().hasExtra("personIds")) {
            filterMan = new HashSet<Integer>();
            for (int i : getIntent().getIntArrayExtra("personIds")) {
                filterMan.add(i);
            }
        }
        
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        final List<String> photos2 = dbHelper.getPhotoIds(filterMan, startDate, endDate);
        GridView photos = (GridView) findViewById(R.id.gallery_photos);
        photos.setNumColumns(FacesGridAdapter.WIDTH_NUM_PICS);
        photos.setAdapter(new GridPhotosAdapter(this, photos2));
        
        photos.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getApplication(), PhotoGalleryCommon.class);
                in.putStringArrayListExtra("photos_array", new ArrayList(photos2));
                in.putExtra(PhotoGalleryCommon.PHOTO_ID, position - FacesGridAdapter.WIDTH_NUM_PICS);
                startActivity(in);
                
            }
        });
        
    }
}