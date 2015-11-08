package ru.trolleg.faces.activities;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FirstFacesOnPersonActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

/**
 * Просмотр людей, с первыми фотографиями.
 * 
 * @author sov
 *
 */
public class PeopleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        
        FirstFacesOnPersonActivity adapterMans = new FirstFacesOnPersonActivity(this, dbHelper.getAllIdsPerson());
        final ListView listView2 = (ListView) findViewById(R.id.list_man);
        listView2.setAdapter(adapterMans);
        
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
        case R.id.help:
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        case R.id.recognition:
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
