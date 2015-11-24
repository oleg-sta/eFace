package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter2;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class PhotoGalleryCommon extends Activity {
    public static String PHOTO_ID = "photoId";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int position = getIntent().getIntExtra(PHOTO_ID, 0);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.comon_photo_pager);

        TextView nameView = (TextView) findViewById(R.id.name_man);
        final ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        List<String> photos = dbHelper.getAllPhotos();
        final PagerAdapter mPagerAdapter = new CommonPhotoAdapter2(this, photos, nameView);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
    }
}
