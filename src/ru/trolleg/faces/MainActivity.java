package ru.trolleg.faces;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Первоначальная страница, здесь в списке показаны лица людей
 * @author sov
 *
 */
public class MainActivity extends Activity implements NotificationReceiver.Listener {

    public static int FACES_VERTICAL;
    public final static String NO_FACES = "Не лица";
    public final static String INPUT_NAME = "Введите имя";

    DictionaryOpenHelper dbHelper;
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
            + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    public static final String EXTRA_MESSAGE = "com.example.test1.MESSAGE";

    public FacesList2 adapterFaces;
    public MenList adapterMans;
    
    public Integer currentMan = null;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume");
        
        final Context context = this;
        final MainActivity d = this;
        adapterFaces.clear();
        //adapterFaces = new FacesList2(this, dbHelper.getAllIdsFacesForPerson(null));
        adapterFaces.addAll(dbHelper.getAllIdsFacesForPerson(currentMan));
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        adapterFaces.notifyDataSetChanged();
        // запускаем поиск лиц
        boolean useCpp = true;
        int cores = 4;
        int coresTh = Runtime.getRuntime().availableProcessors();
        cores = coresTh;
        Log.i("MainActivity", "num cores " + coresTh);
        
        // TODO возможно неверный способ запуска единственной сущности IntentService
        FaceFinderService instance = FaceFinderService.getInstance();
        if (instance == null) {
            Intent intent = new Intent(context, FaceFinderService.class);
            NotificationReceiver receiver = new NotificationReceiver(new Handler());
            receiver.setListener(d);
            intent.putExtra("receiver", receiver);
            intent.putExtra("useCpp", useCpp);
            Log.i("MainActivity", "num thre " + cores);
            intent.putExtra("threads", cores);
            startService(intent);
        } else {
            NotificationReceiver receiver = new NotificationReceiver(new Handler());
            receiver.setListener(d);
            instance.setReceiver(receiver);
            if (instance.b != null) {
                onReceiveResult(0, instance.b);
            }
        }
        // поиск новых фотографий

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MainActivity", "onCreate");
        dbHelper = new DictionaryOpenHelper(this);
        dbHelper.repairBugs();
        // SQLiteDatabase db = dbHelper.getReadableDatabase();
        // dbHelper.onUpgrade(db, 1, 1); // временно

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FACES_VERTICAL = metrics.widthPixels / FacesList.FACES_SIZE;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = this;
        final MainActivity d = this;

        adapterFaces = new FacesList2(this, dbHelper.getAllIdsFacesForPerson(currentMan));
        final GridView listView = (GridView) findViewById(R.id.listFaces);
        listView.setAdapter(adapterFaces);
        adapterFaces.notifyDataSetChanged();
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        
        final LinearLayout la1 = (LinearLayout) findViewById(R.id.mainLay);
        final View vi1 = findViewById(R.id.vie);
        la1.getMeasuredWidth();
        //LinearLayout la2 = (LinearLayout) findViewById(R.id.listFaces2);
        //la2.set
        
        int num = la1.getMeasuredWidth() / FacesList.FACES_SIZE - 1;
        Log.v("MainActivity", "size " + num + " " + la1.getMeasuredWidth());
        Log.v("MainActivity", "size " + la1.getWidth());
        la1.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                    int oldBottom) {
                Log.i("MainActivity", "size2 " + la1.getMeasuredWidth());
                int num = la1.getMeasuredWidth() / FacesList.FACES_SIZE - 1;
                listView.getLayoutParams().width = num * FacesList.FACES_SIZE;
                listView.setNumColumns(num);
                vi1.getLayoutParams().width = la1.getMeasuredWidth() % FacesList.FACES_SIZE; 
                
                //((LinearLayout)listView.getParent()).set
                
            }
        });
        //listView.setStretchMode(GridView.NO_STRETCH);
        //listView.getLayoutParams().width = num * FacesList.FACES_SIZE;
        //listView.setNumColumns(num);
        
        adapterMans = new MenList(this, dbHelper.getAllIdsPerson());
        final ListView listView2 = (ListView) findViewById(R.id.listOfMan);
        listView2.setAdapter(adapterMans);
        adapterMans.notifyDataSetChanged();
        
        
        //LinearLayout t = (LinearLayout)findViewById(R.id.listOfManLayout);
        //t.setOnDragListener(new DragOverListMen(this));
        final Button button = (Button) findViewById(R.id.start_stop);
        button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                FaceFinderService.buttonStart = !FaceFinderService.buttonStart;
                if (!FaceFinderService.buttonStart) {
                    button.setText("Запустить");
                } else {
                    button.setText("Остановить");
                    FaceFinderService instance = FaceFinderService.getInstance();
                    Intent intent = new Intent(context, FaceFinderService.class);
                    NotificationReceiver receiver = new NotificationReceiver(new Handler());
                    receiver.setListener(d);
                    startService(intent);
                }
            }
        });
        ImageView im2 = (ImageView) findViewById(R.id.add_face2);
        im2.setImageResource(R.drawable.add_face);
        im2.setOnDragListener(new DragOverListMen(this));
        im2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                d.setCurrentMan(null);
            }
        });
        
        ImageView im = (ImageView) findViewById(R.id.first_face);
        im.setImageResource(R.drawable.full_trash);
        im.setOnDragListener(new DragOnTrashListener(this));
        
    }

    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static List<String> getCameraImages(Context context) {
        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, null);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        int i = 0;
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                // ограничение до 40 фоток
                if (i < FaceFinderService.PHOTOS_LIMIT) {
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
            adapterFaces.addAll(dbHelper.getIdsFacesForPhoto(photo));
            adapterFaces.notifyDataSetChanged();
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
        adapterFaces.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.merge:
            combine();
            return true;
        case R.id.delete:
            delete();
            return true;
        case R.id.reset_filter:
            adapterFaces.checked.clear();
            adapterFaces.notifyDataSetChanged();
            return true;
        case R.id.reset:
            adapterFaces.clear();
            dbHelper.recreate();
            adapterFaces.notifyDataSetChanged();
            return true;
        case R.id.help:
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        case R.id.reset_people:
            resetPeople();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void resetPeople() {
        dbHelper.facesToNullPeople();
        
    }

    /**
     * Объединение выделенных персон в одну персону
     */
    private void combine() {
        Integer toPersonId = null;
        boolean changed = false;
        String newName = null;
        for (Integer i : adapterFaces.checked) {
            if (toPersonId == null) {
                toPersonId = i;
                newName = dbHelper.getPersonName(i);
            } else {
                if (INPUT_NAME.equals(newName)) {
                    newName = dbHelper.getPersonName(i);
                }
                dbHelper.updatePersonsFacesToNew(toPersonId, i);
                adapterFaces.remove(i);
                changed = true;
            }
        }
        dbHelper.updatePersonName(toPersonId, newName);
        releaseFirstFace();
        adapterFaces.checked.clear();
        if (changed) {
            adapterFaces.notifyDataSetChanged();
        }
    }

    /**
     * "удаление" лиц, лица добавляются в персону с именем "Не лица"
     */
    private void delete() {
        Integer toPersonId = dbHelper.getOrCreatePerson(NO_FACES);
        boolean changed = false;
        if (!adapterFaces.faces.contains(toPersonId)) {
            adapterFaces.add(toPersonId);
            changed = true;
        }
        for (Integer i : adapterFaces.checked) {
            Integer old = i;
            if (old != toPersonId) {
                dbHelper.updatePersonsFacesToNew(toPersonId, old);
                adapterFaces.remove(old);
                changed = true;
            }
        }
        releaseFirstFace();
        adapterFaces.checked.clear();
        if (changed) {
            adapterFaces.notifyDataSetChanged();
        }
    }

    public void setFistFace(Integer integer) {
//        List<Integer> faceIds = dbHelper.getAllIdsFacesForPerson(integer);
//        Face face = dbHelper.getFaceForId(faceIds.get(0));
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, this);
//        db.close();
//        ImageView im = (ImageView) findViewById(R.id.first_face);
//        im.setImageResource(R.drawable.full_trash);
        //im.setImageBitmap(bm);
//        im.setOnDragListener(new OnDragListener() {
//            
//            @Override
//            public boolean onDrag(View v, DragEvent event) {
//                Log.i("MainActivity", "onDrag222");
//
//                return true;
//            }
//        });
        
    }
    public void releaseFirstFace() {
        ImageView im = (ImageView) findViewById(R.id.first_face);
        im.setImageBitmap(null);
        
    }

    public void setFacesOfManList(int personId) {
        Log.i("MainActivity", "setFacesOfManList " + personId);
        List<Integer> faceIds = dbHelper.getAllIdsFacesForPerson(personId);
        ListView facesOfMan = (ListView) findViewById(R.id.listOfMan);
        FacesOfManList facesOfManList = new FacesOfManList(this, faceIds);
        facesOfMan.setAdapter(facesOfManList);
        facesOfManList.notifyDataSetChanged();
    }

    public void setCurrentMan(Integer manId) {
        currentMan = manId;
        adapterFaces.clear();
        //adapterFaces = new FacesList2(this, dbHelper.getAllIdsFacesForPerson(null));
        adapterFaces.addAll(dbHelper.getAllIdsFacesForPerson(currentMan));
        Log.i("MainActivity", "size persons " + adapterFaces.faces.size());
        adapterFaces.notifyDataSetChanged();
    }
}
