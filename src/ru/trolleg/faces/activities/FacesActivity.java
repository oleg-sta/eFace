package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter;
import ru.trolleg.faces.adapters.FacesGridShow;
import ru.trolleg.faces.adapters.PersonListToRecogniseAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Просмотр всех фотографий человека.
 * 
 * @author sov
 *
 */
public class FacesActivity extends AppCompatActivity {

    TextView namePerson;
    DictionaryOpenHelper dbHelper;
    Integer personId;
    
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
        //LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //layoutParams.gravity = Gravity.RIGHT;
        //layoutParams.gravity = Gravity.RIGHT;
        //v.getLayoutParams().height = actionBar.getHeight();
        final ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        layoutParams.gravity = Gravity.RIGHT;
        //v.setLayoutParams(layoutParams);
        //getSupportActionBar().setCustomView(v);
        getSupportActionBar().setCustomView(v, layoutParams);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        //toolbar.setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setCustomView(arg0)
        
        personId = getIntent().getIntExtra(DataHolder.PERSON_ID, 0);
        dbHelper = new DictionaryOpenHelper(this);
        
        namePerson = (TextView) v.findViewById(R.id.text_action);
        TextView namePerson2 = (TextView) v.findViewById(R.id.text_action2);
        namePerson.setText(dbHelper.getPersonName(personId));
        
        
        ImageView iv = (ImageView) v.findViewById(R.id.img_action);
        String photo = dbHelper.getFaceForId(dbHelper.getAllIdsFacesForPerson(personId).get(0)).guid;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.i("PeopleFragment", "photo " + photo);
        iv.setImageBitmap(DataHolder.getInstance().getLittleFaceInCirle(db, photo, this));
        db.close();
        
        FacesGridShow facesGrid = new FacesGridShow(this, dbHelper.getAllIdsFacesForPerson(personId));
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
                input.setHint("Введите имя");
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                input.setText(namePerson.getText());
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(input).setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newName =  input.getText().toString();
                        dbHelper.updatePersonName(personId, newName);
                        namePerson.setText(newName);
                    }
                }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alertDialog.show();
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

    
    
}
