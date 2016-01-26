package ru.flightlabs.eface.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.adapters.FacesGridAdapter;
import ru.flightlabs.eface.adapters.GridPhotosAdapter;

public class ShowSearchResultActivity extends AppCompatActivity {
    
    public static final String PERSON_IDS = "personIds";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getIntent().hasExtra(START_DATE)) {
            startDate = new Date(getIntent().getLongExtra(START_DATE, 0));
        }
        if (getIntent().hasExtra(END_DATE)) {
            endDate = new Date(getIntent().getLongExtra(END_DATE, 0));
        }
        if (getIntent().hasExtra(PERSON_IDS)) {
            filterMan = new HashSet<Integer>();
            for (int i : getIntent().getIntArrayExtra(PERSON_IDS)) {
                filterMan.add(i);
            }
        }
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        final List<String> photos2 = dbHelper.getPhotoIds(filterMan, startDate, endDate);
        GridView photos = (GridView) findViewById(R.id.gallery_photos);
        photos.setNumColumns(FacesGridAdapter.WIDTH_NUM_PICS);
        photos.setAdapter(new GridPhotosAdapter(this, photos2));
        
    }
}