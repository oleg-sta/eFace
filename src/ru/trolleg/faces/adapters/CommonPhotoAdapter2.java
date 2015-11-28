package ru.trolleg.faces.adapters;

import java.io.File;
import java.util.List;

import ru.trolleg.faces.BitmapWorkerTask;
import ru.trolleg.faces.DeactivableViewPager;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.TouchImageView;
import ru.trolleg.faces.TouchImageView.OnPageScaleListener;
import ru.trolleg.faces.data.InfoPhoto;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Адаптер для просмотра фото в галерее
 * @author sov
 *
 */
public class CommonPhotoAdapter2 extends PagerAdapter {

    private List<String> photos;
    private LayoutInflater inflater;
    private Activity _activity;
    TextView textView;
    DeactivableViewPager mPager;

    public CommonPhotoAdapter2(Activity activity, List<String> photos, TextView textView, DeactivableViewPager mPager) {
        this._activity = activity;
        this.photos = photos;
        this.textView = textView;
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
        TouchImageView imgDisplay;

        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container, false);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String path = photos.get(position);
        String fileName = new File(path).getName();
        InfoPhoto infoPh = dbHelper.getInfoPhotoFull(path);
        
        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.img);
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
        ProgressBar bar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);

        textView.setText(fileName);
        final BitmapWorkerTask task = new BitmapWorkerTask(imgDisplay, bar, infoPh);
        task.execute(path);

        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);

    }

}
