package ru.trolleg.faces.adapters;

import java.io.IOException;
import java.util.List;

import ru.trolleg.faces.BitmapWorkerTask;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.R;
import ru.trolleg.faces.data.InfoPhoto;

import android.app.Activity;
import android.content.Context;
import android.media.ExifInterface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

    public CommonPhotoAdapter2(Activity activity, List<String> photos, TextView textView) {
        this._activity = activity;
        this.photos = photos;
        this.textView = textView;
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
        ImageView imgDisplay;

        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container, false);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String path = photos.get(position);
        InfoPhoto infoPh = dbHelper.getInfoPhotoFull(path);
        
        imgDisplay = (ImageView) viewLayout.findViewById(R.id.img);
        ProgressBar bar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);

        textView.setText(path);
        final BitmapWorkerTask task = new BitmapWorkerTask(imgDisplay, bar, infoPh);
        task.execute(path);

        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);

    }

}
