package ru.trolleg.faces.activities;

import android.app.Activity;
import android.os.Bundle;
import ru.trolleg.faces.Log;
import android.view.Window;
import android.view.WindowManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import ru.trolleg.faces.R;

public class ShowCommonPhoto extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        Log.i("DisplayCommonPhoto", "onCreate");
        setContentView(R.layout.show_common_photo);
        
        String infoPh = getIntent().getStringExtra("photo");
        
        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);
        imageView.setMaxScale(5);
        imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        //imageView.setDebug(DataHolder.debugMode);
        
        imageView.setImage(ImageSource.uri(infoPh));
    }

}
