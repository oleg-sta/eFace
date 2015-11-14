package ru.trolleg.faces.activities;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.CommonPhotoAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageView;

/**
 * Просмотр общего фото
 * @author sov
 *
 */
public class DisplayCommonPhoto extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("DisplayCommonPhoto", "onCreate");
        setContentView(R.layout.comon_photo_pager);
        Integer faceId = getIntent().getIntExtra(DataHolder.FACE_ID, 0);
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        Integer personId = dbHelper.getPersonIdByFaceId(faceId);
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(personId);
        int position = faces.indexOf(faceId);
        Log.i("DisplayCommonPhoto", "pos " + position);
        PagerAdapter mPagerAdapter = new CommonPhotoAdapter(this, faces);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
//        String photoPath = dbHelper.getPhotoPathByFaceId(faceId);
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        Bitmap myBitmap = FaceFinderService.decodeSampledBitmapFromResource(photoPath, 500, 500, options);
//        ImageView myImage = (ImageView) findViewById(R.id.img);
//        myImage.setImageBitmap(myBitmap);
    }

}
