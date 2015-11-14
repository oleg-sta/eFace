package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        return view == ((RelativeLayout) object);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
  
        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container,
                false);
  
        imgDisplay = (ImageView) viewLayout.findViewById(R.id.img);
         
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(_activity);
        String photoPath = dbHelper.getPhotoPathByFaceId(faces.get(position));
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap myBitmap = FaceFinderService.decodeSampledBitmapFromResource(photoPath, 500, 500, options);
        imgDisplay.setImageBitmap(myBitmap);
         
         ((ViewPager) container).addView(viewLayout);
  
        return viewLayout;
    }
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
  
    }
     
}
