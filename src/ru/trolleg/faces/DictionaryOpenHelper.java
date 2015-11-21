package ru.trolleg.faces;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ru.trolleg.faces.activities.MainActivity;
import ru.trolleg.faces.data.Face;
import ru.trolleg.faces.data.InfoPhoto;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;

    public DictionaryOpenHelper(Context context) {
        super(context, "faces.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, path TEXT, time_processed real);");
        db.execSQL("create table faces (guid text, photo_id integer, person_id text, height real, width real, centerX real, centerY real, id integer PRIMARY KEY AUTOINCREMENT NOT NULL);");
        db.execSQL("create table person (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, name text, deleted INTEGER);");
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
     * ��������� ���� �������������� ����������
     * 
     * @return
     */
    public List<String> getAllPhotosToBeProcessed() {
        List<String> photos = new ArrayList<String>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select path from photos where time_processed is null", null);
        while (c.moveToNext()) {
            photos.add(c.getString(0));
        }
        c.close();
        s.close();
        return photos;
    }

    /**
     * �������� ������ ����� ����
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
     * ���������� ����� ����
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

    public void addFace(Face faceCur, int photoId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into faces (guid, photo_id, height, width, centerX, centerY) values ('" + faceCur.guid + "', " + photoId
                + ", " + faceCur.height + ", " + faceCur.width + ", " + faceCur.centerX + ", " + faceCur.centerY + ")");
        s.close();
    }

    public List<Face> getAllFaces() {
        List<Face> faces = new ArrayList<Face>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY from faces", null);
        while (c.moveToNext()) {
            Face face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getInt(1);
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

    public int addPerson() {
        return addPerson(MainActivity.INPUT_NAME);
    }

    public int addPerson(String name) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into person (name) values ('"+name+"')");
        int id = getLastId(s);
        s.close();
        return id;
    }

    
    public void addFaceToPerson(String faceId, int personId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = " + personId + " where guid = '" + faceId + "'");
        s.close();
    }
    
    public void addFaceToPerson(Integer faceId, int personId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = " + personId + " where id = '" + faceId + "'");
        s.close();
    }

    public InfoPhoto getInfoPhotoFull(String path) {
        Log.v("DictionaryOpenHelper", path);
        InfoPhoto info = new InfoPhoto();
        info.path = path;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select id, time_processed from photos where path = '" + path + "'", null);
        if (c.moveToNext()) {
            info.id = c.getInt(0);
            info.timeProccessed = c.getLong(1);
            if (c.isNull(1)) {
                info.timeProccessed = null;
            }
        }
        c.close();
        s.close();
        if (info.timeProccessed != null) {
            info.faces = getFacesForPhoto(info.id).toArray(new Face[0]);
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
            face.photoId = c.getInt(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
        }
        c.close();
        s.close();
        return face;
    }
    // исключаем не лица
    public List<Face> getFacesForPhoto(int id) {
        List<Face> faces = new ArrayList<Face>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.guid, f.photo_id, f.person_id, f.height, f.width, f.centerX, f.centerY from faces f where f.photo_id = "+id + " and (f.person_id not in (select id from person where name = '"+MainActivity.NO_FACES+"') or f.person_id is null)", null);
        while (c.moveToNext()) {
            Face face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getInt(1);
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
    
    public List<Integer> getIdsFacesForPhoto(String photo) {
    	List<Integer> faces = new ArrayList<Integer>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.id from faces f inner join photos p on p.id = f.photo_id where p.path = '"+photo+"'", null);
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
        Cursor c = s.rawQuery("select p.id from person p where p.name <> '"+MainActivity.NO_FACES+"' order by p.name, p.id", null);
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
        String sql = "select f.id from faces f where f.person_id = " + personId + " order by f.id";
        if (personId == null) {
            sql = "select f.id from faces f where f.person_id is null order by f.id";
        }
        Cursor c = s.rawQuery(sql, null);
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

    public Integer getOrCreatePerson(String name) {
    	Integer res = getPersonByName(name);
    	if (res != null) {
    		return res;
    	}
        return addPerson(name);
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
     * ������� ���� �� ����� ������� � ������
     * 
     * @param toPersonId
     * @param fromPersonId
     */
	public void updatePersonsFacesToNew(Integer toPersonId, Integer fromPersonId) {
		SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = " + toPersonId + " where person_id = " + fromPersonId + "");
        s.execSQL("delete from person where person_id = '" + fromPersonId + "'");
        s.close();
	}
	public void addFaceToPersonId(Integer faceId, int personId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = " + personId + " where id = " + faceId + "");
        s.close();
    }

	public String getPhotoPathByFaceId(Integer faceId) {
		String res = null;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select path from photos p inner join faces f on f.photo_id = p.id where f.id = " + faceId, null);
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
        Cursor c = s.rawQuery("select f.person_id from faces f where f.id = " + faceId, null);
        while (c.moveToNext()) {
            res = c.getInt(0);
        }
        c.close();
        s.close();
        return res;
	}

	public void removeFromPerson(int faceId) {
		//String guidNewPerson = UUID.randomUUID().toString();
		int newPerosn = addPerson();
		Face face = getFaceForId(faceId);
		addFaceToPerson(face.guid, newPerosn);
	}

	
	/**
	 * ���� �� ��� ���������� ����
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

    public void removePerson(Integer oldPersonId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("delete from person where id = " + oldPersonId);
        s.close();
    }

    public void facesToNullPeople() {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = null");
        s.execSQL("delete from person");
        s.close();
    }

    public List<String> getAllPhotos() {
        List<String> photos = new ArrayList<String>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select path from photos", null);
        while (c.moveToNext()) {
            photos.add(c.getString(0));
        }
        c.close();
        s.close();
        return photos;
    }
    
    public List<String> getAllPhotosForPersonAndFacePosition(Integer personId) {
        List<String> photos = new ArrayList<String>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select ph.path from photos ph", null);
        while (c.moveToNext()) {
            photos.add(c.getString(0));
        }
        c.close();
        s.close();
        return photos;
    }

    public int getPhotoIdByPath(String photo) {
        int res = 0;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select id from photos where path = '" + photo + "'", null);
        if (c.moveToNext()) {
            res = c.getInt(0);
        }
        c.close();
        s.close();
        return res;
    }
    
    private int getLastId(SQLiteDatabase s) {
        int id = 0;
        Cursor c = s.rawQuery("select last_insert_rowid()", null);
        if (c.moveToNext()) {
            id = c.getInt(0);
        }
        c.close();
        return id;
    }
}