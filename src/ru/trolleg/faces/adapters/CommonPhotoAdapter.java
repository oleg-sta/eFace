package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.BitmapWorkerTask;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.TouchImageView;
import ru.trolleg.faces.TouchImageView.OnPageScaleListener;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * просмотр обще фото для лица в людях
 * @author sov
 *
 */
public class CommonPhotoAdapter extends PagerAdapter {

    private List<Integer> faces;
    private LayoutInflater inflater;
    private Activity _activity;
    TouchImageView imgDisplay;
    DeactivableViewPager mPager;
    
    public CommonPhotoAdapter(Activity activity, List<Integer> faces, DeactivableViewPager mPager) {
        this._activity = activity;
        this.faces = faces;
        this.mPager = mPager;
    }
    @Override
    public int getCount() {
        return this.faces.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    public Object instantiateItem(ViewGroup container, int position) {

  
        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container,
                false);
  
        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.img);
         Matrix matrix = new Matrix();
         matrix.setScale(2, 2);
         imgDisplay.setImageMatrix(matrix);
         
        ProgressBar bar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String photoPath = dbHelper.getPhotoPathByFaceId(faces.get(position));
        
        final BitmapWorkerTask task = new BitmapWorkerTask(imgDisplay, bar);
        task.execute(photoPath);
        imgDisplay.setOnScaleListener(new OnPageScaleListener() {
            @Override
            public void onScaleBegin() {
                mPager.deactivate();
            }

            @Override
            public void onScaleEnd(float scale) {
                if (scale > 1.0) {
                    mPager.deactivate();
                } else {
                    mPager.activate();
                }
            }
        });
        imgDisplay.setOnDragListener(new OnDragListener() {
            
            @Override
            public boolean onDrag(View v, DragEvent event) {
                Log.i("s", "s");
                return false;
            }
        });
        imgDisplay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                View v2 = ((DisplayCommonPhoto) _activity).horizontal;
                v2.setVisibility(v2.getVisibility() == View.VISIBLE? View.INVISIBLE : View.VISIBLE);
                
            }
        });
//        final ScaleGestureDetector SGD = new ScaleGestureDetector(_activity,new ScaleListener());
//        imgDisplay.setOnTouchListener(new OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                SGD.onTouchEvent(event);
//                return true;
//            }
//            
//        });
        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }
    
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);
  
    }
        
    
     
}
