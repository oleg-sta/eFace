package ru.flightlabs.eface.activities;

import ru.flightlabs.eface.DataHolder;
import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.adapters.FacesGridAdapter;
import ru.flightlabs.eface.adapters.FacesGridShow;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.text.InputType;
import ru.flightlabs.eface.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Просмотр всех фотографий человека.
 * 
 * @author sov
 *
 */
public class FacesActivity extends AppCompatActivity {

    private LocalBroadcastManager broadcastManager;
    TextView namePerson;
    DictionaryOpenHelper dbHelper;
    public Integer personId;
    ImageView iv;
    FacesGridShow facesGrid;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faces_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        broadcastManager = LocalBroadcastManager.getInstance(getApplication());
        final Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        View v = getLayoutInflater().inflate(R.layout.custom_action, null);
        final ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        layoutParams.gravity = Gravity.RIGHT;
        getSupportActionBar().setCustomView(v, layoutParams);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        
        personId = getIntent().getIntExtra(DataHolder.PERSON_ID, 0);
        dbHelper = new DictionaryOpenHelper(this);
        
        namePerson = (TextView) v.findViewById(R.id.text_action);
        TextView namePerson2 = (TextView) v.findViewById(R.id.text_action2);
        namePerson.setText(dbHelper.getPersonName(personId));
        
        
        iv = (ImageView) v.findViewById(R.id.img_action);
        Integer avaId = dbHelper.getAvaFace(personId);
        String photo = dbHelper.getFaceForId(avaId).guid;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        iv.setImageBitmap(DataHolder.getInstance().getLittleFaceInCirle(db, photo, this));
        db.close();
        
        facesGrid = new FacesGridShow(this, dbHelper.getAllIdsFacesForPerson(personId));
        namePerson2.setText(getResources().getQuantityString(R.plurals.numberOfPhoto, facesGrid.faces.size(), facesGrid.faces.size()));
        final GridView gridFaces = (GridView) findViewById(R.id.grid_faces);
        gridFaces.setNumColumns(FacesGridAdapter.WIDTH_NUM_PICS);
        gridFaces.setAdapter(facesGrid);

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.change_name:
                final EditText input = new EditText(this);
                input.setTextColor(Color.BLACK);
                input.setHint(R.string.enter_name);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                input.setText(namePerson.getText());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(input).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newName = input.getText().toString();
                        dbHelper.updatePersonName(personId, newName);
                        namePerson.setText(newName);
                        Log.v("FacesActivity", "onOptionsItemSelected updatePersonName");
                        Intent intent = new Intent(PeopleFragment.UPDATE_PEOPLE);
                        boolean result = broadcastManager.sendBroadcast(intent);
                        Log.v("FacesActivity", "onOptionsItemSelected updatePersonName///" + result);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alertDialog.show();
                return true;
            case R.id.set_ava:
                facesGrid.setAva = true;
                Toast.makeText(this, R.string.click_photo, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.man_menu, menu);
        return true;
    }

    public void updateAva() {
        Integer avaId = dbHelper.getAvaFace(personId);
        String photo = dbHelper.getFaceForId(avaId).guid;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        iv.setImageBitmap(DataHolder.getInstance().getLittleFaceInCirle(db, photo, this));
        db.close();
    }
    
}
