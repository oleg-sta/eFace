package ru.trolleg.faces.activities;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import ru.trolleg.faces.BitmapWorkerTask;
import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.R;
import ru.trolleg.faces.TouchImageView;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

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
