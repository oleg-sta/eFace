package com.example.testfaceplus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.Computations;
import com.example.testfaceplus.data.InfoPhoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity implements NotificationReceiver.Listener {
    
    DictionaryOpenHelper dbHelper;
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    public static final String EXTRA_MESSAGE = "com.example.test1.MESSAGE";

    private PhotoList adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Computations s = new Computations();
    	Log.i("MainActivity", "jni " + s.stringFromJNI());
    	int[] d22 = new int[] {33};
    	Log.i("MainActivity", "jni3 " + d22[0]);
    	s.intFromJni2(d22);
    	Log.i("MainActivity", "jni5 " + d22[0]);
    	Log.i("MainActivity", "jni2 " + s.intFromJni(new int[] {33,43}));
    	Log.i("MainActivity", "jni55 " + s.findFaces(new int[][] {{1,2}, {3,4}}, 0, 0, true));
    	//Log.i("MainActivity", "jni7 " + s.findFaces(new int[][] {{33,43}, {33,43}, {33,43}}));
        Log.v("MainActivity", "onCreate");
        dbHelper = new DictionaryOpenHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //dbHelper.onUpgrade(db, 1, 1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.start);
        final Context context = this;
        final MainActivity d = this;
        
        List<String> photosArray = new ArrayList<String>();
        Cursor c = db.rawQuery("select guid, path from photos", null);
        while(c.moveToNext()) {
            photosArray.add(c.getString(1));
        }
        c.close();
        db.close();
        
        adapter = new PhotoList(MainActivity.this, photosArray);
        final ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(adapter);
        // переход на большую фотографию
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = (String) parent.getItemAtPosition(position);
                //if (DataHolder.getInstance().infos.get(value) != null) {
                    Log.v("MainActivity", "click " + id);
                    Intent intent = new Intent(context, DisplayPhotoActivity.class);
                    Log.v("MainActivity", "click2 " + id);
                    intent.putExtra(EXTRA_MESSAGE, value);
                    startActivity(intent);
                //}

            }
        });
        
        // кнопка "Поиск лиц"
        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                
                // start Service for computing faces
                final ListView listView = (ListView) findViewById(R.id.listView1);

                // TODO всю работу с фоторграфиями переложить на сервис
                List<String> photos = getCameraImages(getApplicationContext());
                
                Intent intent = new Intent(context, FaceFinderService.class);
                NotificationReceiver receiver = new NotificationReceiver(new Handler());
                receiver.setListener(d);
                intent.putExtra("receiver", receiver);
                
                startService(intent);

                // Так нехорошо делать
                //PhotoList adapter = (PhotoList) listView.getAdapter();
                for (String ph : photos) {
                    if (!adapter.web.contains(ph)) {
                        adapter.add(ph);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        
        Button textView1 = (Button) findViewById(R.id.textView1);
        // переход к лицам
        textView1.setOnClickListener(new Button.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayFacesActivity.class);
                Log.v("MainActivity", "click3");
                //intent.putExtra(EXTRA_MESSAGE, value);
                startActivity(intent);
                
            }
        });
        
        TextView textView5 = (TextView) findViewById(R.id.textView111);
        Log.v("MainActivity", "reset button " +textView5);
        textView5.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO clear temporarily photo
                Log.v("MainActivity", "reset button");
                SQLiteDatabase s2 = dbHelper.getWritableDatabase();
                dbHelper.onUpgrade(s2, 2, 2); // временно
                s2.close();
                adapter.web.clear();
                adapter.notifyDataSetChanged();
                for (File f : getFilesDir().listFiles()) {
                    if (f.isFile()) {
                        f.delete();
                    }
                }

            }
        });
        
    }

    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static List<String> getCameraImages(Context context) {
        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
                selectionArgs, null);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        int i = 0;
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                // ограничение до 40 фоток
                if (i < 40) {
                    result.add(data);
                }
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String photo = resultData.getString("photo");
        String message = resultData.getString("message");
        String progressStr = resultData.getString("progress");
        if (photo != null) {
            // не хорошо лезть в чужой view и дублировать логику
            final ListView listView = (ListView) findViewById(R.id.listView1);
            for (int i = 0; i < listView.getChildCount(); i++) {
                View v = listView.getChildAt(i);
                TextView tw = (TextView) v.findViewById(R.id.txt);
                if (photo.equals(tw.getText())) {
                    ImageView imageView = (ImageView) v.findViewById(R.id.img);
                    TextView numFaces = (TextView) v.findViewById(R.id.num_faces);
                    TextView time = (TextView) v.findViewById(R.id.time);
                    InfoPhoto info = dbHelper.getInfoPhotoFull(photo);
                    imageView.setImageBitmap(DataHolder.getInstance().getLittlePhoto(photo));
                    numFaces.setText("" + info.faceCount);
                    time.setText("" + info.timeProccessed);
                    return;
                }
            }
        }
        if (message != null) {
            int progress = 0;
            if (progressStr != null) {
                progress = Integer.valueOf(progressStr);
            }
            TextView text = (TextView) findViewById(R.id.text_message);
            text.setText(message);
            
            ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
            bar.setVisibility(View.VISIBLE);
            bar.setProgress(progress);
        }

    }
}
