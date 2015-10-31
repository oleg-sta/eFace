package ru.trolleg.faces;

import java.util.ArrayList;
import java.util.List;

import ru.trolleg.faces.data.Face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * �������������� ��������, ����� � ������ �������� ���� �����
 * @author sov
 *
 */
public class MainActivity extends Activity implements NotificationReceiver.Listener {

    public static int FACES_VERTICAL;
    public final static String NO_FACES = "�� ����";
    public final static String INPUT_NAME = "������� ���";

    DictionaryOpenHelper dbHelper;
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
            + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    public static final String EXTRA_MESSAGE = "com.example.test1.MESSAGE";

    private PersonList adapter;

    @Override
    protected void onResume() {
        super.onResume();
        final Context context = this;
        final MainActivity d = this;
        adapter.clear();
        adapter.addAll(dbHelper.getAllIdsPerson());
        adapter.notifyDataSetChanged();
        // ��������� ����� ���
        boolean useCpp = true;
        int cores = 4;
        int coresTh = Runtime.getRuntime().availableProcessors();
        cores = coresTh;
        Log.i("MainActivity", "num cores " + coresTh);
        
        // TODO �������� �������� ������ ������� ������������ �������� IntentService
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
        // ����� ����� ����������

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("MainActivity", "onCreate");
        dbHelper = new DictionaryOpenHelper(this);
        dbHelper.repairBugs();
        // SQLiteDatabase db = dbHelper.getReadableDatabase();
        // dbHelper.onUpgrade(db, 1, 1); // ��������

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        FACES_VERTICAL = metrics.widthPixels / FacesList.FACES_SIZE;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = this;
        final MainActivity d = this;

        adapter = new PersonList(this, dbHelper.getAllIdsPerson());
        final ListView listView = (ListView) findViewById(R.id.listFaces);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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
                // ����������� �� 40 �����
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
            adapter.addAll(dbHelper.getIdsFacesForPhoto(photo));
            adapter.notifyDataSetChanged();
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
        adapter.notifyDataSetChanged();
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
            adapter.checked.clear();
            adapter.notifyDataSetChanged();
            return true;
//        case R.id.reset:
//            adapter.clear();
//            dbHelper.recreate();
//            adapter.notifyDataSetChanged();
//            return true;
        case R.id.help:
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ����������� ���������� ������ � ���� �������
     */
    private void combine() {
        Integer toPersonId = null;
        boolean changed = false;
        String newName = null;
        for (Integer i : adapter.checked) {
            if (toPersonId == null) {
                toPersonId = i;
                newName = dbHelper.getPersonName(i);
            } else {
                if (INPUT_NAME.equals(newName)) {
                    newName = dbHelper.getPersonName(i);
                }
                dbHelper.updatePersonsFacesToNew(toPersonId, i);
                adapter.remove(i);
                changed = true;
            }
        }
        dbHelper.updatePersonName(toPersonId, newName);
        releaseFirstFace();
        adapter.checked.clear();
        if (changed) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * "��������" ���, ���� ����������� � ������� � ������ "�� ����"
     */
    private void delete() {
        Integer toPersonId = dbHelper.getOrCreatePerson(NO_FACES);
        boolean changed = false;
        if (!adapter.personsId.contains(toPersonId)) {
            adapter.add(toPersonId);
            changed = true;
        }
        for (Integer i : adapter.checked) {
            Integer old = i;
            if (old != toPersonId) {
                dbHelper.updatePersonsFacesToNew(toPersonId, old);
                adapter.remove(old);
                changed = true;
            }
        }
        releaseFirstFace();
        adapter.checked.clear();
        if (changed) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setFistFace(Integer integer) {
        List<Integer> faceIds = dbHelper.getAllIdsFacesForPerson(integer);
        Face face = dbHelper.getFaceForId(faceIds.get(0));
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, this);
        db.close();
        ImageView im = (ImageView) findViewById(R.id.first_face);
        im.setImageBitmap(bm);
        
    }
    public void releaseFirstFace() {
        ImageView im = (ImageView) findViewById(R.id.first_face);
        im.setImageBitmap(null);
        
    }
}
