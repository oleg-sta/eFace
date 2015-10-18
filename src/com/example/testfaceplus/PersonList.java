package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.List;

import com.example.testfaceplus.data.Face;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Адаптер для просмотра сгруппированых лиц, т.е. персон
 * 
 * @author sov
 *
 */
public class PersonList extends ArrayAdapter<Integer> {

    private final Activity context;
    private final List<Integer> persons;
    
    public PersonList(Activity context, List<Integer> persons) {
        super(context, R.layout.show_faces, persons);
        this.persons = persons;
        this.context = context;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.my_list_item, null, true);
        final TextView txtTitle = (TextView) rowView.findViewById(R.id.person_name);
        ImageView personImg = (ImageView) rowView.findViewById(R.id.person_img);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        
        txtTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final EditText input = new EditText(context);
				input.setText(txtTitle.getText());
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
		        builder.setView(input)
		               .setPositiveButton("Да", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                       dbHelper.updateFaceName(persons.get(position), input.getText().toString());
		                       txtTitle.setText(input.getText().toString());
		                   }
		               })
		               .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                       // User cancelled the dialog
		                   }
		               });
		        // Create the AlertDialog object and return it
		        AlertDialog alertDialog = builder.create();
		        alertDialog.show();
				
			}
		});
        
        
        
        
        Face face = dbHelper.getFaceForId(persons.get(position));
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
        db.close();
        
        txtTitle.setText(face.name);
        personImg.setImageBitmap(bm);
       
        return rowView;
    }  
    

}
