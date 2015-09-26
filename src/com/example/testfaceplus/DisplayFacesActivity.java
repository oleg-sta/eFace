package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DisplayFacesActivity extends Activity {
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_faces);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        SQLiteDatabase s = dbHelper.getReadableDatabase();
        
        final Context context = this;
        TextView textView1 = (TextView) findViewById(R.id.textView2);
        textView1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                Log.v("MainActivity", "click3");
                //intent.putExtra(EXTRA_MESSAGE, value);
                startActivity(intent);
                
            }
        });
        //final ArrayList<String> catnames = new ArrayList<String>();
        List<String> person = new ArrayList<String>();
        Cursor c = s.rawQuery("select person_id from person", null);
        while(c.moveToNext()) {
            person.add(c.getString(0));
        }
        c.close();
        s.close();
        
        
        PersonList personList = new PersonList(this, person.toArray(new String[0]));
        
        
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, DataHolder.getInstance().catnames);
        final ListView listView = (ListView) findViewById(R.id.listView2);
        listView.setAdapter(personList);
        
    }

}
