package ru.trolleg.faces.activities;

import ru.trolleg.faces.BitmapWorkerTask;
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
        
        TouchImageView img = (TouchImageView) findViewById(R.id.img);
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        
        
        final BitmapWorkerTask task = new BitmapWorkerTask(img,  bar);
        task.execute(infoPh);
    }

}
