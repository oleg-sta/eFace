package com.example.testfaceplus;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DictionaryOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    DictionaryOpenHelper(Context context) {
        super(context, "FACES", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos (guid text, path TEXT);");
        db.execSQL("create table faces (guid text, photo_id text, person_id text);");
        db.execSQL("create table person (person_id text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS photos");
        db.execSQL("DROP TABLE IF EXISTS faces");
        db.execSQL("DROP TABLE IF EXISTS person");
        onCreate(db);
        
    }
}