package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

/**
 * ����������� ����� ����������
 * @author sov
 *
 */
public class DisplayCommonPhoto extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("DisplayCommonPhoto", "onCreate");
        setContentView(R.layout.show_common_photo);
        Integer faceId = getIntent().getIntExtra(DataHolder.FACE_ID, 0);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        String photoPath = dbHelper.getPhotoPathByFaceId(faceId);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap myBitmap = FaceFinderService.decodeSampledBitmapFromResource(photoPath, 500, 500, options);
        ImageView myImage = (ImageView) findViewById(R.id.img);
        myImage.setImageBitmap(myBitmap);
    }

}
