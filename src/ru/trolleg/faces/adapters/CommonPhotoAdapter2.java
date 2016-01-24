package ru.trolleg.faces.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.PhotoGalleryCommon;
import ru.trolleg.faces.data.InfoPhoto;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import ru.trolleg.faces.Log;
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
 * Адаптер для просмотра фото в галерее
 * @author sov
 *
 */
public class CommonPhotoAdapter2 extends PagerAdapter {

    public boolean showFaces;
    public int currentPosition;
    public List<String> photos;
    private LayoutInflater inflater;
    private Activity _activity;
    DeactivableViewPager mPager;
    Map<Integer, SubsamplingScaleImageView> cc = new HashMap<Integer, SubsamplingScaleImageView>();

    public CommonPhotoAdapter2(Activity activity, List<String> photos, DeactivableViewPager mPager) {
        this._activity = activity;
        this.photos = photos;
        this.mPager = mPager;
    }

    @Override
    public int getCount() {
        return this.photos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        //TouchImageView imgDisplay;

        Log.i("Skia", "" + position);
        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container, false);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String path = photos.get(position);
        InfoPhoto infoPh = dbHelper.getInfoPhotoFull(path);
        final ProgressBar pb = (ProgressBar)viewLayout.findViewById(R.id.progressBar);
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) viewLayout.findViewById(R.id.imageView);
        if (position != currentPosition) {
            imageView.preview = true;
        } else {
            imageView.preview = false;
        }
        imageView.showFaces = showFaces;
        cc.put(position, imageView);
        imageView.faces = infoPh.faces;
        imageView.setMaxScale(5);
        imageView.setMaximumDpi(5);
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        //imageView.setDebug(DataHolder.debugMode);
        
        final BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(infoPh.path, options2);
        int height = options2.outHeight;
        int width = options2.outWidth;
        if (height > 1500 || width > 1500 || true) {
            imageView.setImage(ImageSource.uri(infoPh.path).dimensions(width, height), ImageSource.uri(infoPh.path));
        } else {
            imageView.setImage(ImageSource.uri(infoPh.path));
        }
        imageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                View v2 = ((PhotoGalleryCommon) _activity).horizontal;
                v2.setVisibility(v2.getVisibility() == View.VISIBLE? View.INVISIBLE : View.VISIBLE);
                v2 = ((PhotoGalleryCommon) _activity).v;
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
                pb.setVisibility(View.GONE);
            }
            
            @Override
            public void onPreviewLoadError(Exception e) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onImageLoaded() {
                pb.setVisibility(View.GONE);
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
