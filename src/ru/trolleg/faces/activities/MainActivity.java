package ru.trolleg.faces.activities;

import java.util.ArrayList;
import java.util.List;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

/**
 * 
 * 
 * @author sov
 *
 */
public class MainActivity {

    public final static String NO_FACES = "Не лица";
    public final static String INPUT_NAME = "Введите имя";

    DictionaryOpenHelper dbHelper;
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()
            + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    public static final String EXTRA_MESSAGE = "com.example.test1.MESSAGE";


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

}
