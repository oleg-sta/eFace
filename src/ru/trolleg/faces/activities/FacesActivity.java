package ru.trolleg.faces.activities;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridShow;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.GridView;
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
        Integer personId = getIntent().getIntExtra(DataHolder.PERSON_ID, 0);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        
        TextView namePerson = (TextView) findViewById(R.id.name_man);
        namePerson.setText(dbHelper.getPersonName(personId));
        
        FacesGridShow facesGrid = new FacesGridShow(this, dbHelper.getAllIdsFacesForPerson(personId));
        final GridView gridFaces = (GridView) findViewById(R.id.grid_faces);
        gridFaces.setAdapter(facesGrid);
        final FacesActivity d = this;
        
        final LinearLayout la1 = (LinearLayout)findViewById(R.id.lay);
//        la1.addOnLayoutChangeListener(new OnLayoutChangeListener() {
//            
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
//                    int oldBottom) {
//                //TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, la1.getMeasuredWidth(), metrics)
//                Log.i("MainActivity", "size2 " + la1.getMeasuredWidth());
//                int num = DataHolder.px2Dp(la1.getMeasuredWidth(), d) / (80 + 2);
//                gridFaces.getLayoutParams().width = num * DataHolder.dp2Px(80 + 2, d);
//                gridFaces.setNumColumns(num);
//            }
//        });
    }
}
