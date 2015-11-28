package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter;
import ru.trolleg.faces.adapters.FacesCommonAdapter;
import ru.trolleg.faces.adapters.HorizontalListView;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Просмотр общего фото
 * @author sov
 *
 */
public class DisplayCommonPhoto extends Activity {

    private float scale = 1f;
    private ScaleGestureDetector mScaleDetector;
    private Matrix matrix = new Matrix();
    
    int lastPosition;
    View  imLast;
    public HorizontalListView horizontal;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        Log.i("DisplayCommonPhoto", "onCreate");
        setContentView(R.layout.comon_photo_pager);
        Integer faceId = getIntent().getIntExtra(DataHolder.FACE_ID, 0);
        TextView nameView = (TextView) findViewById(R.id.name_man);
        final DeactivableViewPager mPager = (DeactivableViewPager) findViewById(R.id.pager);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        Integer personId = dbHelper.getPersonIdByFaceId(faceId);
        nameView.setText(dbHelper.getPersonName(personId));
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(personId);
        int position = faces.indexOf(faceId);
        Log.i("DisplayCommonPhoto", "pos " + position);
        final PagerAdapter mPagerAdapter = new CommonPhotoAdapter(this, faces, mPager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            
            @Override
            public void onPageSelected(int arg0) {
                setCurrentFromBig(arg0);
                
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
        final FacesCommonAdapter facesAdapter = new FacesCommonAdapter(this, faces);
        facesAdapter.selected = position;
        horizontal.setAdapter(facesAdapter);
        
        
        horizontal.scrollTo(position * DataHolder.dp2Px(80, getApplicationContext()));
        //horizontal.setSelection(position);
        //facesAdapter.get
        horizontal.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("dsds", "po " + position);
                if (imLast != null) {
                    imLast.setPadding(0, 0, 0, 0);
                    imLast.setBackgroundColor(Color.TRANSPARENT);
                    imLast.invalidate();
                } else if (facesAdapter.imLast != null) {
                    facesAdapter.imLast.setPadding(0, 0, 0, 0);
                    facesAdapter.imLast.setBackgroundColor(Color.TRANSPARENT);
                    facesAdapter.imLast.invalidate();
                }
                mPager.setCurrentItem(position);
                //ImageView  im = (ImageView)view.findViewById(R.id.one_face1);
                view.setPadding(2, 2, 2, 2);
                view.setBackgroundColor(Color.YELLOW);
                view.invalidate();
                facesAdapter.selected = position;
                lastPosition =position;
                imLast = view;
                
                facesAdapter.notifyDataSetChanged();
            }
        });
        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        
    }
    
    public void setCurrentFromBig(int position) {
        ((FacesCommonAdapter)horizontal.getAdapter()).selected = position;
        ((FacesCommonAdapter)horizontal.getAdapter()).notifyDataSetChanged();
        Log.i("w", "" + horizontal.mNextX);
        //horizontal.get
        if (horizontal.mNextX > position * DataHolder.dp2Px(80, getApplicationContext())) {
            horizontal.scrollTo(position * DataHolder.dp2Px(80, getApplicationContext()));
        } else if (position * DataHolder.dp2Px(80, getApplicationContext()) - horizontal.mNextX >  getApplicationContext().getResources().getDisplayMetrics().widthPixels) {
            horizontal.scrollTo(position * DataHolder.dp2Px(80, getApplicationContext()));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return true;
    }

private class ScaleListener extends ScaleGestureDetector.
    
    SimpleOnScaleGestureListener {
       @Override
       public boolean onScale(ScaleGestureDetector detector) {
           Log.i("d", "d");
          scale *= detector.getScaleFactor();
          scale = Math.max(0.1f, Math.min(scale, 5.0f));
          
          matrix.setScale(scale, scale);
          //imgDisplay.setImageMatrix(matrix);
          return true;
       }
    }
}
