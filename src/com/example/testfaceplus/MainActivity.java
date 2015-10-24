package com.example.testfaceplus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.Computations;
import com.example.testfaceplus.data.InfoPhoto;

import detection.Stage;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements NotificationReceiver.Listener {

	public static int FACES_VERTICAL;
	
	DictionaryOpenHelper dbHelper;
	public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
			+ "/DCIM/Camera";
	public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
	public static final String EXTRA_MESSAGE = "com.example.test1.MESSAGE";

	private PersonList adapter;

	@Override
	protected void onResume() {
		super.onResume();
		adapter.clear();
		adapter.addAll(dbHelper.getAllIdsPerson());
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("MainActivity", "onCreate");
		dbHelper = new DictionaryOpenHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		dbHelper.onUpgrade(db, 1, 1); // временно
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		FACES_VERTICAL = metrics.widthPixels / FacesList.FACES_SIZE;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Context context = this;
		final MainActivity d = this;

		adapter = new PersonList(MainActivity.this, dbHelper.getAllIdsPerson());
		final ListView listView = (ListView) findViewById(R.id.listFaces);
		listView.setAdapter(adapter);

		// запускаем поиск лиц
		boolean useCpp = true;
		int cores = 4;
		int coresTh = Runtime.getRuntime().availableProcessors();
		Log.i("MainActivity", "num cores " + coresTh);

		Intent intent = new Intent(context, FaceFinderService.class);
		NotificationReceiver receiver = new NotificationReceiver(new Handler());
		receiver.setListener(d);
		intent.putExtra("receiver", receiver);
		intent.putExtra("useCpp", useCpp);
		Log.i("MainActivity", "num thre " + cores);
		intent.putExtra("threads", cores);
		startService(intent);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Объединение выделенных персон в одну персону
	 */
	private void combine() {
		Integer toPersonId = null;
		boolean changed = false;
		for (Integer i : adapter.checked) {
			if (toPersonId == null) {
				toPersonId = i;
			} else {
				Integer old = i;
				dbHelper.updatePersonsFacesToNew(toPersonId, old);
				adapter.remove(old);
				changed = true;
			}
		}
		adapter.checked.clear();
		if (changed) {
			adapter.notifyDataSetChanged();
		}
	}

}
