package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.List;

import com.example.testfaceplus.data.Face;
import com.example.testfaceplus.data.InfoPhoto;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    DictionaryOpenHelper(Context context) {
        super(context, "faces.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos (guid text, path TEXT primary key);");
        db.execSQL("create table faces (guid text primary key, photo_id text, person_id text, height real, width real, centerX real, centerY real);");
        db.execSQL("create table person (person_id text primary key);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS photos");
        db.execSQL("DROP TABLE IF EXISTS faces");
        db.execSQL("DROP TABLE IF EXISTS person");
        onCreate(db);
    }

    /**
     * получение всех необработанных фотографий
     * 
     * @return
     */
    public List<String> getAllPhotosToBeProcessed() {
        List<String> photos = new ArrayList<String>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select path from photos where guid is null", null);
        while (c.moveToNext()) {
            photos.add(c.getString(0));
        }
        c.close();
        s.close();
        return photos;
    }

    /**
     * добавить только новые фото
     * 
     * @param photos
     */
    public int addNewPhotos(List<String> photos) {
        int i = 0;
        for (String photo : photos) {
            Log.v("DictionaryOpenHelper", photo);
            SQLiteDatabase s = getReadableDatabase();
            Cursor c = s.rawQuery("select path from photos where path = '" + photo + "'", null);
            if (!c.moveToNext()) {
                c.close();
                s.close();
                addPhoto(photo);
                i++;
            } else {
                c.close();
                s.close();
            }
        }
        return i;
    }

    /**
     * ƒобавление одног фото
     * 
     * @param photo
     */
    public void addPhoto(String photo) {
        Log.v("DictionaryOpenHelper", "add photo " + photo);
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into photos (path) values ('" + photo + "')");
        s.close();
    }

    /**
     * обновление информации по фото
     * 
     * @param photo
     * @param guid
     */
    public void updatePhoto(String photo, String guid) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update photos set guid = '" + guid + "' where path = '" + photo + "'");
        s.close();
    }

    public void addFace(Face faceCur, String imgId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into faces (guid, photo_id, height, width, centerX, centerY) values ('" + faceCur.guid + "', '" + imgId
                + "', " + faceCur.height + ", " + faceCur.width + ", " + faceCur.centerX + ", " + faceCur.centerY + ")");
        s.close();
    }

    public List<Face> getAllFaces() {
        List<Face> faces = new ArrayList<Face>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY from faces", null);
        while (c.moveToNext()) {
            Face face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getString(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
            faces.add(face);
        }
        c.close();
        s.close();
        return faces;
    }

    public void addPerson(String id) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into person (person_id) values ('" + id + "')");
        s.close();
    }

    public void addFaceToPerson(String faceId, String personId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = '" + personId + "' where guid = '" + faceId + "'");
        s.close();
    }

    public InfoPhoto getInfoPhotoFull(String path) {
        Log.v("DictionaryOpenHelper", path);
        InfoPhoto info = new InfoPhoto();
        info.path = path;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select guid from photos where path = '" + path + "'", null);
        if (c.moveToNext()) {
            info.guid = c.getString(0);
            Log.v("DictionaryOpenHelper", "" + info.guid);
        }
        c.close();
        s.close();
        if (info.guid != null) {
            info.faces = getFacesForPhoto(info.guid).toArray(new Face[0]);
            info.faceCount = info.faces.length;
            
        }
        return info;
    }
    public List<Face> getFacesForPhoto(String guid) {
        List<Face> faces = new ArrayList<Face>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY from faces where photo_id = '"+guid+"'", null);
        while (c.moveToNext()) {
            Face face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getString(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
            faces.add(face);
        }
        c.close();
        s.close();
        return faces;
    }

    public void removeGroups() {
        Log.v("DictionaryOpenHelper", "removeGroups");
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("delete from person");
        s.close();
    }
}