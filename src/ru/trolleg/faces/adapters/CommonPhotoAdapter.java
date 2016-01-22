package ru.trolleg.faces.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import ru.trolleg.faces.data.InfoPhoto;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.OnImageEventListener;

/**
 * просмотр обще фото для лица в людях
 * @author sov
 *
 */
public class CommonPhotoAdapter extends PagerAdapter {

    public int currentPosition;
    private List<Integer> faces;
    private LayoutInflater inflater;
    private Activity _activity;
    //TouchImageView imgDisplay;
    DeactivableViewPager mPager;
    Map<Integer, SubsamplingScaleImageView> cc = new HashMap<Integer, SubsamplingScaleImageView>();
    public boolean showFaces;

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
  
        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) viewLayout.findViewById(R.id.imageView);
        if (position != currentPosition) {
            imageView.preview = true;
        } else {
            imageView.preview = false;
        }
        imageView.showFaces = showFaces;
        cc.put(position, imageView);
        final ProgressBar bar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String photoPath = dbHelper.getPhotoPathByFaceId(faces.get(position));
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        imageView.setMaxScale(5);
        InfoPhoto infoPh = dbHelper.getInfoPhotoFull(photoPath);
        imageView.faces = infoPh.faces;
        //imageView.setDebug(DataHolder.debugMode);
        
        final BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options2);
        //imageView.setImage(ImageSource.uri(photoPath));
        imageView.setImage(ImageSource.uri(photoPath).dimensions(options2.outWidth, options2.outHeight), ImageSource.uri(photoPath));
        imageView.setOnClickListener(new OnClickListener() {
           
            @Override
            public void onClick(View v) {
                View v2 = ((DisplayCommonPhoto) _activity).horizontal;
                v2.setVisibility(v2.getVisibility() == View.VISIBLE? View.INVISIBLE : View.VISIBLE);
                v2 = ((DisplayCommonPhoto) _activity).v;
                v2.setVisibility(v2.getVisibility() == View.VISIBLE? View.INVISIBLE : View.VISIBLE);
                
            }
        });
        imageView.setOnImageEventListener(new OnImageEventListener() {
            
            @Override
            public void onTileLoadError(Exception e) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onReady() {
                bar.setVisibility(View.GONE);
            }
            
            @Override
            public void onPreviewLoadError(Exception e) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onImageLoaded() {
                bar.setVisibility(View.GONE);
            }
            
            @Override
            public void onImageLoadError(Exception e) {
                // TODO Auto-generated method stub
                
            }
        });
        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }
    
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((SubsamplingScaleImageView)cc.get(position)).recycle();
        cc.remove(position);
        ((ViewPager) container).removeView((FrameLayout) object);
  
    }
        
    public void redrawView() {
        Log.i("CommonPhotoAdapter2", "redrawView");
        SubsamplingScaleImageView imageView = cc.get(currentPosition);
        if (imageView != null) {
            Log.i("CommonPhotoAdapter2", "redrawView2 " + imageView.uri);
            imageView.preview = false;
            imageView.invalidate();
        }
    }

    public void setFacesCheck(boolean isChecked) {
        showFaces = isChecked;
        for (SubsamplingScaleImageView vv : cc.values()) {
            vv.showFaces = isChecked;
        }
        SubsamplingScaleImageView imageView = cc.get(currentPosition);
        if (imageView != null) {
            Log.i("CommonPhotoAdapter2", "redrawView2 " + imageView.uri);
            imageView.showFaces = isChecked;
            imageView.invalidate();
        }
    }
}
