package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridShow;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Просмотр всех фотографий человека.
 * 
 * @author sov
 *
 */
public class FacesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faces_activity);
        

        ActionBar actionBar = getActionBar();
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_action, null);
        

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.RIGHT;
        //v.getLayoutParams().height = actionBar.getHeight();
        actionBar.setCustomView(v, layoutParams);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        Integer personId = getIntent().getIntExtra(DataHolder.PERSON_ID, 0);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        
        TextView namePerson = (TextView) v.findViewById(R.id.text_action);
        TextView namePerson2 = (TextView) v.findViewById(R.id.text_action2);
        namePerson.setText(dbHelper.getPersonName(personId));
        
        
        ImageView iv = (ImageView) v.findViewById(R.id.img_action);
        String photo = dbHelper.getFaceForId(dbHelper.getAllIdsFacesForPerson(personId).get(0)).guid;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.i("PeopleFragment", "photo " + photo);
        iv.setImageBitmap(DataHolder.getInstance().getLittleFaceInCirle(db, photo, this));
        db.close();
        
        FacesGridShow facesGrid = new FacesGridShow(this, dbHelper.getAllIdsFacesForPerson(personId));
        namePerson2.setText(facesGrid.getCount() + " фотографий");
        final GridView gridFaces = (GridView) findViewById(R.id.grid_faces);
        gridFaces.setAdapter(facesGrid);

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    
    
}
