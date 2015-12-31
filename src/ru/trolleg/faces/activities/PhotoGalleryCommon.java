package ru.trolleg.faces.activities;

import java.util.ArrayList;
import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter2;
import ru.trolleg.faces.adapters.HorizontalListView;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class PhotoGalleryCommon extends Activity {
    public static String PHOTO_ID = "photoId";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String albumId = getIntent().getStringExtra(DataHolder.ALBUM_ID);
        int position = getIntent().getIntExtra(PHOTO_ID, 0);
        List<String> photos = getIntent().getExtras().getStringArrayList("photos_array");
        Log.i("PhotoGalleryCommon", "photos " + photos);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.comon_photo_pager);

        TextView nameView = (TextView) findViewById(R.id.name_man);
        final DeactivableViewPager mPager = (DeactivableViewPager) findViewById(R.id.pager);
        if (photos == null) {
            photos = MainActivity.getCameraImages(this, albumId);
        }
        final PagerAdapter mPagerAdapter = new CommonPhotoAdapter2(this, photos, nameView, mPager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
        HorizontalListView horizontal = (HorizontalListView) findViewById(R.id.gallery1);
        horizontal.setVisibility(View.GONE);

    }
}
