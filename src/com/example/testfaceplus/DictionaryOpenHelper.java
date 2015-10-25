package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.testfaceplus.data.Face;
import com.example.testfaceplus.data.InfoPhoto;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;

    DictionaryOpenHelper(Context context) {
        super(context, "faces.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos (guid text, path TEXT primary key, time_processed real);");
        db.execSQL("create table faces (guid text, photo_id text, person_id text, height real, width real, centerX real, centerY real, id integer PRIMARY KEY AUTOINCREMENT NOT NULL);");
        db.execSQL("create table person (person_id text, id integer PRIMARY KEY AUTOINCREMENT NOT NULL, name text, deleted INTEGER);");
    }
    
    public void recreate() {
    	SQLiteDatabase db = getWritableDatabase();
    	db.execSQL("drop table photos;");
    	db.execSQL("drop table faces;");
    	db.execSQL("drop table person;");
    	onCreate(db);
    	db.close();
    }

    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.i("DictionaryOpenHelper", "onUpgrade from " + oldVersion + " " + newVersion);
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
     * Добавление одног фото
     * 
     * @param photo
     */
    public void addPhoto(String photo) {
        Log.v("DictionaryOpenHelper", "add photo " + photo);
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into photos (path) values ('" + photo + "')");
        s.close();
    }

    public void updatePhoto(String photo, long time) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update photos set time_processed = "+time+" where path = '" + photo + "'");
        s.close();
    }
    public void updatePersonName(Integer id, String newName) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update person set name = '"+newName+"' where id = " + id + "");
        s.close();
    }
    /**
     * обновление информации по фото
     * 
     * @param photo
     * @param guid
     */
    public void updatePhoto(String photo, String guid, long time) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update photos set guid = '" + guid + "', time_processed = "+time+" where path = '" + photo + "'");
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
        addPerson(id, "Введите имя");
    }

    public void addPerson(String id, String name) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into person (person_id, name) values ('" + id + "', '"+name+"')");
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
        Cursor c = s.rawQuery("select guid, time_processed from photos where path = '" + path + "'", null);
        if (c.moveToNext()) {
            info.guid = c.getString(0);
            info.timeProccessed = c.getLong(1);
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
    public String getPersonName(Integer id) {
    	String name = null;
    	SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select name from person where id = " + id, null);
        if (c.moveToNext()) {
            name = c.getString(0);
        }
        c.close();
        s.close();
        return name;
    }
    public Face getFaceForId(Integer id) {
        Face face = null;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY from faces where id = " + id, null);
        if (c.moveToNext()) {
        	face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getString(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
        }
        c.close();
        s.close();
        return face;
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
    
    public List<Integer> getIdsFacesForPhoto(String guid) {
    	List<Integer> faces = new ArrayList<Integer>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.id from faces f inner join photos p on p.guid = f.photo_id where p.path = '"+guid+"'", null);
        while (c.moveToNext()) {
            faces.add(c.getInt(0));
        }
        c.close();
        s.close();
        return faces;
    }

    public List<Integer> getAllIdsPerson() {
    	List<Integer> faces = new ArrayList<Integer>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.id from person f", null);
        while (c.moveToNext()) {
            faces.add(c.getInt(0));
        }
        c.close();
        s.close();
        return faces;
    }
    public List<Integer> getAllIdsFaces() {
    	List<Integer> faces = new ArrayList<Integer>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.id from faces f", null);
        while (c.moveToNext()) {
            faces.add(c.getInt(0));
        }
        c.close();
        s.close();
        return faces;
    }
    public List<Integer> getAllIdsFacesForPerson(Integer personId) {
    	List<Integer> faces = new ArrayList<Integer>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.id from faces f inner join person p on p.person_id = f.person_id where p.id = " + personId, null);
        while (c.moveToNext()) {
            faces.add(c.getInt(0));
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

    public String getPersStrById(Integer id) {
    	String res = null;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select person_id from person where id = " + id, null);
        while (c.moveToNext()) {
            res = c.getString(0);
        }
        c.close();
        s.close();
        return res;
    }
    public Integer getOrCreatePerson(String name) {
    	Integer res = getPersonByName(name);
    	if (res != null) {
    		return res;
    	}
        addPerson(UUID.randomUUID().toString(), name);
        return getPersonByName(name);
    }
    
    public Integer getPersonByName(String name) {
    	Integer res = null;
    	SQLiteDatabase s = getReadableDatabase();
    	Cursor c = s.rawQuery("select id from person where name = '" + name + "'", null);
        if (c.moveToNext()) {
            res = c.getInt(0);
        }
        c.close();
        s.close();
        return res;
    }
    /**
     * Перенос лица из одной персоны в другую
     * 
     * @param toPersonId
     * @param fromPersonId
     */
	public void updatePersonsFacesToNew(Integer toPersonId, Integer fromPersonId) {
		String toPerStr = getPersStrById(toPersonId);
		String personStr = getPersStrById(fromPersonId);
		SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = '" + toPerStr + "' where person_id = '" + personStr + "'");
        s.execSQL("delete from person where person_id = '" + personStr + "'");
        s.close();
	}
	public void addFaceToPersonId(Integer faceId, String personId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = '" + personId + "' where id = " + faceId + "");
        s.close();
    }

	public String getPhotoPathByFaceId(Integer faceId) {
		String res = null;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select path from photos p inner join faces f on f.photo_id = p.guid where f.id = " + faceId, null);
        while (c.moveToNext()) {
            res = c.getString(0);
        }
        c.close();
        s.close();
        return res;
	}

	public Integer getPersonIdByFaceId(Integer faceId) {
		Integer res = null;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select p.id from person p inner join faces f on f.person_id = p.person_id where f.id = " + faceId, null);
        while (c.moveToNext()) {
            res = c.getInt(0);
        }
        c.close();
        s.close();
        return res;
	}

	public void removeFromPerson(int faceId) {
		String guidNewPerson = UUID.randomUUID().toString();
		addPerson(guidNewPerson);
		Face face = getFaceForId(faceId);
		addFaceToPerson(face.guid, guidNewPerson);
	}

	
	/**
	 * Было ли уже обработано фото
	 * @param photo
	 * @return
	 */
	public boolean photoProcessed(String photo) {
		boolean res = false;
		SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select time_processed from photos where path = '" + photo + "'", null);
        while (c.moveToNext()) {
            res = !c.isNull(0);
        }
        c.close();
        s.close();
        return res;
	}

	/**
	 * Вы лица не привязанные к персоне будут привязаны к "Не лица"
	 */
	public void repairBugs() {
		Integer personId = getOrCreatePerson(MainActivity.NO_FACES);
		String personGuid = getPersStrById(personId);
		SQLiteDatabase s = getWritableDatabase();
		s.execSQL("update faces set person_id = '" + personGuid + "' where person_id is null or person_id not in (select person_id from person)");
		s.close();
	}
}