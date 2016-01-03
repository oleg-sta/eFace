package ru.trolleg.faces.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.FaceFinderService;
import ru.trolleg.faces.data.Album;
import ru.trolleg.faces.data.Photo;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

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

    public static List<Photo> getCameraPhotos(Context context) {
        final String[] projection = { MediaStore.Images.Media.DATA, Images.Media.BUCKET_DISPLAY_NAME, Images.Media._ID, Images.Media.DATE_TAKEN};
        String selection = null;
        String[] selectionArgs = null;
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, Images.Media._ID + " DESC");
        ArrayList<Photo> result = new ArrayList<Photo>(cursor.getCount());
        int i = 0;
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(Images.Media.DATA);
            final int imageDateTaken = cursor.getColumnIndexOrThrow(Images.Media.DATE_TAKEN);
            do {
                final String data = cursor.getString(dataColumn);
                final Date date = new Date(cursor.getLong(imageDateTaken));
                Photo photo = new Photo();
                photo.path = data;
                photo.dateTaken = date;
                result.add(photo);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
    
    public static List<String> getCameraImages(Context context) {
        return getCameraImages(context, null);
    }
    
    public static String getAlbumName(Context context, final String albumId) {
        String albumName = "";
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, new String[]{Images.Media.BUCKET_DISPLAY_NAME},
                MediaStore.Images.Media.BUCKET_ID + " = ?", new String[]{ albumId }, null);
        if (cursor.moveToFirst()) {
            albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
        }
        return albumName;
    }
    public static List<String> getCameraImages(Context context, final String albumId) {
        Log.i("MainActivity", "getCameraImages");
        final String[] projection = { MediaStore.Images.Media.DATA, Images.Media.BUCKET_DISPLAY_NAME, Images.Media._ID};
        String selection = null;
        String[] selectionArgs = null;
        if (albumId != null) {
            selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
            selectionArgs = new String[]{ albumId };
        }
        //final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        //final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, Images.Media._ID + " DESC");
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        int i = 0;
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            final int imageBucket = cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME);
            do {
                final String data = cursor.getString(dataColumn);
                final String album = cursor.getString(imageBucket);
                long di = cursor.getLong(cursor.getColumnIndex(Images.Media._ID));
                result.add(data);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static List<Album> getBucketImages(Context context) {
        Log.i("MainActivity", "getCameraImages");
        final String[] projection = {Images.Media.BUCKET_ID, Images.Media.BUCKET_DISPLAY_NAME, Images.Media.DATA, Images.Media.DATE_TAKEN};
        final Cursor cursor = context.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, Images.Media.BUCKET_ID + " ASC," + Images.Media._ID + " DESC");
        Map<String, Album> albumsId = new HashMap<String, Album>();
        //List<Album> albums = new ArrayList<Album>();
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            final int imageBucketid = cursor.getColumnIndexOrThrow(Images.Media.BUCKET_ID);
            final int imageBucket = cursor.getColumnIndexOrThrow(Images.Media.BUCKET_DISPLAY_NAME);
            do {
                final String albumId = cursor.getString(imageBucketid);
                if (!albumsId.containsKey(albumId)) {
                    Log.i("MainActivity", "albumId " + albumId);
                    final String albumName = cursor.getString(imageBucket);
                    Album alb = new Album();
                    alb.id = albumId;
                    alb.name = albumName;
                    alb.count = 1;
                    alb.firstImage = cursor.getString(dataColumn);
                    albumsId.put(albumId, alb);
                } else {
                    albumsId.get(albumId).count++;
                }
            } while (cursor.moveToNext());
        }
        ValueComparator bvc = new ValueComparator(albumsId);
        TreeMap sorted_map = new TreeMap(bvc);
        sorted_map.putAll(albumsId);
        cursor.close();
        return new ArrayList<Album>(sorted_map.values());
    }
    
    static class ValueComparator implements Comparator<String> {
        Map<String, Album> base;

        public ValueComparator(Map<String, Album> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(String a, String b) {
            if (base.get(a).count >= base.get(b).count) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }

    }
}
