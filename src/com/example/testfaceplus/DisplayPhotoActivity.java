package com.example.testfaceplus;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testfaceplus.data.Face;
import com.example.testfaceplus.data.InfoPhoto;

public class DisplayPhotoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("DisplayPhotoActivity", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_pic);
        
        
        String message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
        
        TextView textView = (TextView) findViewById(R.id.txt);
        textView.setText(message);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        InfoPhoto info = dbHelper.getInfoPhotoFull(message);

        if (info != null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = info.scaleFactor;
            Bitmap myBitmap = FaceFinderService.decodeSampledBitmapFromResource(message, 500, 500, options);

            android.graphics.Bitmap.Config bitmapConfig = myBitmap.getConfig();
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            myBitmap = myBitmap.copy(bitmapConfig, true);

            int heightImg = myBitmap.getHeight();
            int widthImg = myBitmap.getWidth();
            
            Canvas canvas = new Canvas(myBitmap);
            //PointF tmp_point = new PointF();
            Paint tmp_paint = new Paint();

            for (int i = 0; i < info.faceCount; i++) {
                Face face = info.faces[i];
                tmp_paint.setColor(Color.RED);
                tmp_paint.setAlpha(100);
                
                int heightFace = (int) (face.height * heightImg) / 100;
                int widthFace = (int) (face.width * widthImg) / 100;
                int x = (int) (face.centerX * widthImg) / 100 - widthFace / 2;
                int y = (int) (face.centerY * heightImg) / 100 - heightFace / 2;

                
                //face.getMidPoint(tmp_point);
                canvas.drawRect(x, y, x + widthFace, y + heightFace, tmp_paint);
                //canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(), tmp_paint);
            }

            ImageView myImage = (ImageView) findViewById(R.id.img);
            myImage.setImageBitmap(myBitmap);
        }
    }

}
