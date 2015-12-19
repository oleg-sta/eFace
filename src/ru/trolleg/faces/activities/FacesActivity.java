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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
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
public class FacesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faces_activity);
        

        final Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        

        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = getLayoutInflater().inflate(R.layout.custom_action, null);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //layoutParams.gravity = Gravity.RIGHT;
        //v.getLayoutParams().height = actionBar.getHeight();
        getSupportActionBar().setCustomView(v, layoutParams);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        //toolbar.setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setCustomView(arg0)
        
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
