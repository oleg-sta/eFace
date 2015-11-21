package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter;
import ru.trolleg.faces.adapters.GalleryAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.TextView;

/**
 * Просмотр общего фото
 * @author sov
 *
 */
public class DisplayCommonPhoto extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("DisplayCommonPhoto", "onCreate");
        setContentView(R.layout.comon_photo_pager);
        Integer faceId = getIntent().getIntExtra(DataHolder.FACE_ID, 0);
        TextView nameView = (TextView) findViewById(R.id.name_man);
        final ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        Integer personId = dbHelper.getPersonIdByFaceId(faceId);
        nameView.setText(dbHelper.getPersonName(personId));
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(personId);
        int position = faces.indexOf(faceId);
        Log.i("DisplayCommonPhoto", "pos " + position);
        final PagerAdapter mPagerAdapter = new CommonPhotoAdapter(this, faces);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
        
        final Gallery gallery = (Gallery) findViewById(R.id.gallery1);
        gallery.setAdapter(new GalleryAdapter(this, faces));
        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position, false);
            }
        });
        gallery.setSelection(position);
        
    }

}
