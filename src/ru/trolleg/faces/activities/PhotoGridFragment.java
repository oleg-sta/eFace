package ru.trolleg.faces.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter;
import ru.trolleg.faces.adapters.GridPhotosAdapter;

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
        
        final String albumId = getIntent().getStringExtra(DataHolder.ALBUM_ID);
        setTitle(MainActivity.getAlbumName(getApplication(), albumId));
        List<String> photosId = MainActivity.getCameraImages(this, albumId);
        final PhotoGridFragment d =this;
        GridView photos = (GridView) findViewById(R.id.gallery_photos);
        photos.setNumColumns(FacesGridAdapter.WIDTH_NUM_PICS);
        GridPhotosAdapter as = new GridPhotosAdapter(this, photosId);
        as.albumId = albumId;
        photos.setAdapter(as);

    }

}
