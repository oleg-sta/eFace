package ru.trolleg.faces;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.activities.MainActivity;
import ru.trolleg.faces.data.Face;
import ru.trolleg.faces.data.InfoPhoto;
import ru.trolleg.faces.data.Photo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DictionaryOpenHelper extends SQLiteOpenHelper {


    public static final String TABLE_PHOTOS = "photos";
    public static final String TABLE_FACES = "faces";
    public static final String TABLE_PERSON = "person";
    public static final String COL_NAME_UPPER = "name_upper";
    
    private static final int DATABASE_VERSION = 2;

    public DictionaryOpenHelper(Context context) {
        super(context, "faces.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, path TEXT, time_processed real, time_photo integer);");
        db.execSQL("create table faces (guid text, photo_id integer, person_id integer, height real, width real, centerX real, centerY real, id integer PRIMARY KEY AUTOINCREMENT NOT NULL, probability real);");
        db.execSQL("create table " + TABLE_PERSON + " (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, name text, name_upper text,deleted INTEGER, ava_id integer);");
        db.execSQL("CREATE INDEX photos_idx ON photos(path)");
    }
    
    public void recreate() {
    	SQLiteDatabase db = getWritableDatabase();
    	db.execSQL("drop table photos;");
    	db.execSQL("drop table faces;");
    	db.execSQL("drop table "+TABLE_PERSON);
    	onCreate(db);
    	db.close();
    }

    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.i("DictionaryOpenHelper", "onUpgrade from " + oldVersion + " " + newVersion);
        if (oldVersion < 2) {
            db.execSQL("CREATE INDEX photos_idx ON photos(path)");
        }
    }

    /**
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

    public int getAllCountPhotosProcessed() {
        int count = 0;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select count(*) from photos", null);
        if (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        s.close();
        return count;
    }
    
    public int getFacesCount() {
        int count = 0;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select count(*) from faces", null);
        if (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        s.close();
        return count;
    }
    
    public int addNewPhotos(List<Photo> photos) {
        int i = 0;
        SQLiteDatabase s = getWritableDatabase();
        for (Photo photo : photos) {
            Cursor c = s.rawQuery("select path from photos where path = '" + encapsulateSql(photo.path) + "'", null);
            if (!c.moveToNext()) {
                s.execSQL("insert into photos (path, time_photo) values ('" + encapsulateSql(photo.path) + "'," + photo.dateTaken.getTime() + ")");
                i++;
            } else {
            }
            c.close();
        }
        s.close();
        return i;
    }

    public void addNewPhoto(Photo photo) {
        SQLiteDatabase s = getWritableDatabase();
        Cursor c = s.rawQuery("select path from photos where path = '" + encapsulateSql(photo.path) + "'", null);
        if (!c.moveToNext()) {
            s.execSQL("insert into photos (path, time_photo) values ('" + encapsulateSql(photo.path) + "'," + photo.dateTaken.getTime() + ")");
        } else {
        }
        c.close();
        s.close();
    }

    private static String encapsulateSql(String str) {
        return str == null? null : str.replaceAll("'", "''");     
    }
    public void updatePhoto(String photo, long time) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update photos set time_processed = " + time + " where path = '" + encapsulateSql(photo) + "'");
        s.close();
    }
    public void updatePersonName(Integer id, String newName) {
        SQLiteDatabase s = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("name_upper", newName.toUpperCase());
        //s.execSQL("update "+TABLE_PERSON+" set name = '"+newName+"', name_upper = '"+newName.toUpperCase()+"' where id = " + id + "");
        s.update(TABLE_PERSON, values, "id = ?", new String[]{String.valueOf(id)});
        s.close();
    }

    public void addFace(Face faceCur, int photoId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into faces (guid, photo_id, height, width, centerX, centerY, probability) values ('" + faceCur.guid + "', " + photoId
                + ", " + faceCur.height + ", " + faceCur.width + ", " + faceCur.centerX + ", " + faceCur.centerY + "," + faceCur.probability + ")");
        s.close();
    }

    public List<Face> getAllFaces() {
        List<Face> faces = new ArrayList<Face>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY, probability from faces", null);
        while (c.moveToNext()) {
            Face face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getInt(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
            face.probability = c.getFloat(7);
            faces.add(face);
        }
        c.close();
        s.close();
        return faces;
    }

    public int addPerson(String name) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("insert into "+TABLE_PERSON+" (name, "+COL_NAME_UPPER+") values ('"+encapsulateSql(name)+"', '"+encapsulateSql(name.toUpperCase())+"')");
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
        Cursor c = s.rawQuery("select id, time_processed from photos where path = '" + encapsulateSql(path) + "'", null);
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
        Cursor c = s.rawQuery("select name from "+TABLE_PERSON+" where id = " + id, null);
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
        Cursor c = s.rawQuery("select guid, photo_id, person_id, height, width, centerX, centerY, probability from faces where id = " + id, null);
        if (c.moveToNext()) {
        	face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getInt(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
            face.probability = c.getFloat(7);
        }
        c.close();
        s.close();
        return face;
    }
    // исключаем не лица
    public List<Face> getFacesForPhoto(int id) {
        List<Face> faces = new ArrayList<Face>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.guid, f.photo_id, f.person_id, f.height, f.width, f.centerX, f.centerY, f.probability from faces f where f.photo_id = "+id + " and (f.person_id not in (select id from "+TABLE_PERSON+" where name = '"+MainActivity.NO_FACES+"') or f.person_id is null)", null);
        while (c.moveToNext()) {
            Face face = new Face();
            face.guid = c.getString(0);
            face.photoId = c.getInt(1);
            face.height = c.getDouble(3);
            face.width = c.getDouble(4);
            face.centerX = c.getDouble(5);
            face.centerY = c.getDouble(6);
            face.probability = c.getFloat(7);
            faces.add(face);
        }
        c.close();
        s.close();
        return faces;
    }
    
    public List<Integer> getIdsFacesForPhoto(String photo) {
    	List<Integer> faces = new ArrayList<Integer>();
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.id from faces f inner join photos p on p.id = f.photo_id where p.path = '"+encapsulateSql(photo)+"'", null);
        while (c.moveToNext()) {
            faces.add(c.getInt(0));
        }
        c.close();
        s.close();
        return faces;
    }

    /**
     *  0 - по имени
     *  1 - по количеству
     * @param sortMode
     * @return
     */
    public List<Integer> getAllIdsPerson(int sortMode, boolean sortAsc) {
        return getAllIdsPerson(sortMode, sortAsc, null);
    }
    public List<Integer> getAllIdsPerson(int sortMode, boolean sortAsc, String textNew) {
        String addWhere = "";
        if (textNew != null) {
            addWhere = " and p." + COL_NAME_UPPER + " like '%" + encapsulateSql(textNew.toUpperCase()) + "%' ";
        }
        String sortOrder = " order by p.name " + (sortAsc? "" : "DESC") + ", p.id";
        List<Integer> faces = new ArrayList<Integer>();
        String sql = "select p.id from " + TABLE_PERSON + " p where p.name <> '"+MainActivity.NO_FACES+"' " + addWhere + " order by p.name "+(sortAsc? "" : "DESC") +", p.id";
        if (sortMode != 0) {
            sql = "select p.id, count(*) c from " + TABLE_PERSON + " p, faces f where f.person_id = p.id and p.name <> '"+MainActivity.NO_FACES+"' " + addWhere + " group by p.id order by c "+(sortAsc? "DESC" : "ASC") +", p.id";
        }
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery(sql, null);
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
        s.execSQL("delete from " + TABLE_PERSON + "");
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
    	Cursor c = s.rawQuery("select id from "+TABLE_PERSON+" where name = '" + name + "'", null);
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

    public void removePerson(Integer oldPersonId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("delete from "+TABLE_PERSON+" where id = " + oldPersonId);
        s.close();
    }

    public void facesToNullPeople() {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update faces set person_id = null");
        s.execSQL("delete from " + TABLE_PERSON);
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
        Cursor c = s.rawQuery("select id from photos where path = '" + encapsulateSql(photo) + "'", null);
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

    public int getCountNewPhotos() {
        int count = 0;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select count(*) from photos where time_processed is null", null);
        if (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        s.close();
        return count;
    }

    public Integer getAvaFace(int personId) {
        Integer avaId = null;
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery("select f.ava_id from person f where f.id = " + personId, null);
        if (c.moveToNext()) {
            avaId = c.getInt(0);
            if (c.isNull(0)) {
                avaId = null;
            }
        }
        c.close();
        if (avaId == null) {
            c = s.rawQuery("select f.id from faces f where f.person_id = " + personId + " order by f.id", null);
            if (c.moveToNext()) {
                avaId = c.getInt(0);
            }
            c.close();
        }
        s.close();
        return avaId;
    }
    
    public void setAvaId(int personId, int faceId) {
        SQLiteDatabase s = getWritableDatabase();
        s.execSQL("update person set ava_id = " + faceId + " where id = " + personId);
        s.close();
    }

    private static void nullTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
    // TODO выдавать результат с указанием сколько людей на фотке нашлось и впорядке уменьшения количества наденных
    public List<String> getPhotoIds(Set<Integer> filterMan, Date startDate2, Date endDate2) {
        Date startDate;
        Date endDate;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate2);
        nullTime(cal);
        startDate = cal.getTime();
        cal.setTime(endDate2);
        nullTime(cal);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        endDate = cal.getTime();
        List<String> photos = new ArrayList<String>();
        String where = " where 1 = 1 ";
        String query = "select ph.path, count(*) c from photos ph inner join faces f on f.photo_id = ph.id join person p on p.id = f.person_id ";
        if (!filterMan.isEmpty()) {
            where += " and p.name <> '"+MainActivity.NO_FACES+"' ";
            String inds = "(-666";
            for (Integer idMan : filterMan) {
                inds += "," + idMan;
            }
            inds = inds + ")";
            where += " and p.id in " + inds;
        } else {
            query = "select ph.path, 1 c from photos ph ";
        }
        if (startDate != null) {
            where += " and time_photo >= " + startDate.getTime(); 
        }
        if (endDate != null) {
            where += " and time_photo < " + endDate.getTime();
        }
        String fullQuery = query + where + " group by ph.path order by c desc";
        Log.i("DictionaryOpenHelper", "query " + fullQuery);
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s.rawQuery(fullQuery, null);
        while (c.moveToNext()) {
            int count = c.getInt(1);
            if (filterMan.size() > 0) {
                if (count == filterMan.size()) {
                    photos.add(c.getString(0));
                }
            } else {
                photos.add(c.getString(0));
            }
        }
        return photos;
    }
    
    public void getMaxMin(Date startDate, Date endDate) {
        SQLiteDatabase s = getReadableDatabase();
        Cursor c = s
                .rawQuery("select max(time_photo), min(time_photo) c from photos", null);
        if (c.moveToNext()) {
            startDate.setTime(c.getLong(1));
            endDate.setTime(c.getLong(0));
        }
    }

    public void removeCascadePhotos(List<String> photoProcessed) {
        SQLiteDatabase s = getWritableDatabase();
        for (String photo : photoProcessed) {
            Integer idPhoto = null;
            Cursor c = s.rawQuery("select id from photos where path = '" + photo + "'", null);
            if (c.moveToNext()) {
                idPhoto = c.getInt(0);
            }
            c.close();
            if (idPhoto != null) {
                s.execSQL("delete from faces where photo_id = " + idPhoto);
                s.execSQL("delete from photos where id = " + idPhoto);
            }
        }
        s.close();
    }
}