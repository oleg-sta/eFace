package com.example.testfaceplus;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public class DisplayPhotoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DisplayPhotoActivity", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_pic);

        String message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);

        InfoPhoto info = DataHolder.getInstance().infos.get(message);

        if (info != null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = info.scaleFactor;
            Bitmap myBitmap = BitmapFactory.decodeFile(message, options);

            android.graphics.Bitmap.Config bitmapConfig = myBitmap.getConfig();
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            myBitmap = myBitmap.copy(bitmapConfig, true);

            Canvas canvas = new Canvas(myBitmap);
            PointF tmp_point = new PointF();
            Paint tmp_paint = new Paint();

            for (int i = 0; i < info.faceCount; i++) {
                FaceDetector.Face face = info.faces[i];
                tmp_paint.setColor(Color.RED);
                tmp_paint.setAlpha(100);
                face.getMidPoint(tmp_point);
                canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(), tmp_paint);
            }

            ImageView myImage = (ImageView) findViewById(R.id.img);
            myImage.setImageBitmap(myBitmap);
        }
    }

}
