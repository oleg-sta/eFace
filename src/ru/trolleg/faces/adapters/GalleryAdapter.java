package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.data.Face;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class GalleryAdapter extends PagerAdapter  {
    private Context context;
    List<Integer> faces;
    private LayoutInflater inflater;
    
    public GalleryAdapter(Context c, List<Integer> faces) {
        this.faces = faces;
        context = c;
    }

    @Override
    public int getCount() {
        return faces.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
  
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.show_common_photo, container,
                false);
        imgDisplay = (ImageView) viewLayout.findViewById(R.id.img);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, context);
        db.close();
        imgDisplay.setImageBitmap(bm);
       
        ((ViewPager) container).addView(viewLayout);
        return viewLayout;
    }
    
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((FrameLayout) object);
  
    }

    @Override
    public float getPageWidth(int position) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return (float)DataHolder.dp2Px(80, context) / size.x;
    }
    
}
