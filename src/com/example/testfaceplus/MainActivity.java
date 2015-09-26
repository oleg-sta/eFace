package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.List;

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
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements NotificationReceiver.Listener {
    
    DictionaryOpenHelper dbHelper;
    // use cache only for photos
    //private LruCache<String, InfoPhoto> mMemoryCache;
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    public static final String EXTRA_MESSAGE = "com.example.test1.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        Log.v("MainActivity", "onCreate");
        dbHelper = new DictionaryOpenHelper(this);
        
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        

        //final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        //final int cacheSize = maxMemory / 8;

//        mMemoryCache = new LruCache<String, InfoPhoto>(cacheSize) {
//            @Override
//            protected int sizeOf(String key, InfoPhoto bitmap) {
//                // The cache size will be measured in kilobytes rather than
//                // number of items.
//                return bitmap.littlePhoto.getByteCount() / 1024;
//            }
//        };

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.start);
        final Context context = this;
        final MainActivity d = this;
        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                
                // start Service for computing faces
                final ListView listView = (ListView) findViewById(R.id.listView1);

                List<String> photos = getCameraImages(getApplicationContext());
                //dbHelper.getWritableDatabase().
                Log.v("service222", "should be started");
                //Intent sI = new Intent(this, FaceFinderService.class);
                
                Intent intent = new Intent(context, FaceFinderService.class);
                NotificationReceiver receiver = new NotificationReceiver(new Handler());
                receiver.setListener(d);
                intent.putExtra("receiver", receiver);
                
                startService(intent);
                Log.v("service222", "ended");
                //photos.add("Заглушка");

                // R.drawable.ic_launcher;
                PhotoList adapter = new PhotoList(MainActivity.this, photos.toArray(new String[0]));

                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String value = (String) parent.getItemAtPosition(position);
                        if (DataHolder.getInstance().infos.get(value) != null) {
                            Log.v("MainActivity", "click " + id);
                            Intent intent = new Intent(context, DisplayPhotoActivity.class);
                            Log.v("MainActivity", "click2 " + id);
                            intent.putExtra(EXTRA_MESSAGE, value);
                            startActivity(intent);
                        }

                    }
                });

                
            }
        });
        
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayFacesActivity.class);
                Log.v("MainActivity", "click3");
                //intent.putExtra(EXTRA_MESSAGE, value);
                startActivity(intent);
                
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
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String photo = resultData.getString("photo");
        // не хорошо лезть в чужой view и дублировать логику
        final ListView listView = (ListView) findViewById(R.id.listView1);
        for (int i = 0; i < listView.getChildCount(); i++) {
            View v = listView.getChildAt(i);
            TextView tw = (TextView) v.findViewById(R.id.txt);
            if (photo.equals(tw.getText())) {
                ImageView imageView = (ImageView) v.findViewById(R.id.img);
                TextView numFaces = (TextView) v.findViewById(R.id.num_faces);
                imageView.setImageBitmap(DataHolder.getInstance().infos.get(photo).littlePhoto);
                numFaces.setText("" + DataHolder.getInstance().infos.get(photo).faceCount);
                return;
            }
        }
        
        
    }
}
