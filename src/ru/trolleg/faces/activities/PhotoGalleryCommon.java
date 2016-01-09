package ru.trolleg.faces.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter2;
import ru.trolleg.faces.adapters.FacesCommonAdapter;
import ru.trolleg.faces.adapters.HorizontalListView;
import ru.trolleg.faces.adapters.HorizontalPhotoAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PhotoGalleryCommon extends Activity {
    public static String PHOTO_ID = "photoId";
    public TextView nameView;
    public HorizontalListView horizontal;
    CommonPhotoAdapter2 mPagerAdapter;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String albumId = getIntent().getStringExtra(DataHolder.ALBUM_ID);
        int position = getIntent().getIntExtra(PHOTO_ID, 0);
        List<String> photos = getIntent().getExtras().getStringArrayList("photos_array");
        Log.i("PhotoGalleryCommon", "photos " + photos);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.comon_photo_pager);

        nameView = (TextView) findViewById(R.id.name_man);
        final DeactivableViewPager mPager = (DeactivableViewPager) findViewById(R.id.pager);
        if (photos == null) {
            photos = MainActivity.getCameraImages(this, albumId);
        }
        mPagerAdapter = new CommonPhotoAdapter2(this, photos, mPager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
        String fileName = new File(mPagerAdapter.photos.get(position)).getName();
        nameView.setText(fileName);
        mPagerAdapter.currentPosition = position;
        mPager.addOnPageChangeListener(new OnPageChangeListener() {
            
            @Override
            public void onPageSelected(int position) {
                String fileName = new File(mPagerAdapter.photos.get(position)).getName();
                nameView.setText(fileName);
                mPagerAdapter.currentPosition = position;
                mPagerAdapter.redrawView();
                setCurrentFromBig(position, true);
            }
            
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        horizontal = (HorizontalListView) findViewById(R.id.gallery1);
        HorizontalPhotoAdapter adapter = new HorizontalPhotoAdapter(this, photos);
        horizontal.setAdapter(adapter);
        adapter.selected = position;
        horizontal.scrollTo(position * DataHolder.dp2Px(80, getApplicationContext()));
        horizontal.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
                setCurrentFromBig(position, false);
            }
        });
    }
    
    public void setCurrentFromBig(int position, boolean fromBig) {
        mPagerAdapter.currentPosition = position;
        Log.i("DisplayCommonPhoto", "setCurrentFromBig " + position + " " + fromBig);
        int lastPos = ((HorizontalPhotoAdapter)horizontal.getAdapter()).selected;
        
        ((HorizontalPhotoAdapter)horizontal.getAdapter()).selected = position;
        HorizontalPhotoAdapter.ViewHolder lstViewHolder = ((HorizontalPhotoAdapter)horizontal.getAdapter()).forUpdate.get(lastPos);
        HorizontalPhotoAdapter.ViewHolder viewHolder = ((HorizontalPhotoAdapter)horizontal.getAdapter()).forUpdate.get(position);
        
        if (lastPos >= 0 && lstViewHolder != null && lastPos == lstViewHolder.position) {
            Log.i("DisplayCommonPhoto", "old");
            lstViewHolder.view2.setVisibility(View.INVISIBLE);
        }
        if (viewHolder != null && position == viewHolder.position) {
            Log.i("DisplayCommonPhoto", "new");
            viewHolder.view2.setVisibility(View.VISIBLE);
        }
        Log.i("DisplayCommonPhoto", "" + lastPos + " " + position);
        //horizontal.get
        if (fromBig) {
            if (horizontal.mNextX > position * DataHolder.dp2Px(80, getApplicationContext())) {
                horizontal.scrollTo(position * DataHolder.dp2Px(80, getApplicationContext()));
            } else if ((position + 1) * DataHolder.dp2Px(80, getApplicationContext()) - horizontal.mNextX > getApplicationContext()
                    .getResources().getDisplayMetrics().widthPixels) {
                horizontal.scrollTo((position + 1) * DataHolder.dp2Px(80, getApplicationContext()) - getApplicationContext()
                        .getResources().getDisplayMetrics().widthPixels);
            }
        }
        mPagerAdapter.redrawView();
    }
}
