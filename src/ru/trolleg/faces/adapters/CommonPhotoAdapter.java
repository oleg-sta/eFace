package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.BitmapWorkerTask;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class CommonPhotoAdapter extends PagerAdapter {

    private List<Integer> faces;
    private LayoutInflater inflater;
    private Activity _activity;
    
    public CommonPhotoAdapter(Activity activity, List<Integer> faces) {
        this._activity = activity;
        this.faces = faces;
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
        ImageView imgDisplay;
  
        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container,
                false);
  
        imgDisplay = (ImageView) viewLayout.findViewById(R.id.img);
        ProgressBar bar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String photoPath = dbHelper.getPhotoPathByFaceId(faces.get(position));
        
        final BitmapWorkerTask task = new BitmapWorkerTask(imgDisplay, bar);
        task.execute(photoPath);
        
        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);
  
    }
    
    
     
}
