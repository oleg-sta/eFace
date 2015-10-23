package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Адаптер для просмотра сгруппированых лиц, т.е. персон
 * 
 * @author sov
 *
 */
public class PersonList extends ArrayAdapter<Integer> {

    private final Activity context;
    public final List<Integer> persons;
    public final Set<Integer> checked = new HashSet<Integer>();
    
    public PersonList(Activity context, List<Integer> persons) {
        super(context, R.layout.my_list_item, persons);
        this.persons = persons;
        this.context = context;
    }


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View rowView = convertView;

		LayoutInflater inflater = context.getLayoutInflater();
		rowView = inflater.inflate(R.layout.my_list_item, null, true);
		final TextView txtTitle = (TextView) rowView.findViewById(R.id.person_name);
		// ImageView personImg = (ImageView)
		// rowView.findViewById(R.id.person_img);
		CheckBox box = (CheckBox)rowView.findViewById(R.id.person_check);
		box.setChecked(checked.contains(position));
		box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					checked.add(position);
				} else {
					checked.remove(position);
				}
				
			}
		});
		
		final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
		LinearLayout facesLayout = (LinearLayout) rowView.findViewById(R.id.facesLinear);

		// TODO check box should be saved
		txtTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final EditText input = new EditText(context);
				input.setText(txtTitle.getText());
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setView(input).setPositiveButton("Да", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dbHelper.updatePersonName(persons.get(position), input.getText().toString());
						txtTitle.setText(input.getText().toString());
					}
				}).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
				// Create the AlertDialog object and return it
				AlertDialog alertDialog = builder.create();
				alertDialog.show();

			}
		});

		String personName = dbHelper.getPersonName(persons.get(position));
		List<Integer> faceIds = dbHelper.getAllIdsFacesForPerson(persons.get(position));
		Log.i("3333", "ed " + faceIds.size());
        int i = 0;
		for (Integer d : faceIds) {
			Face face = dbHelper.getFaceForId(d);
			SQLiteDatabase db = dbHelper.getReadableDatabase();
            Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
            db.close();
            ImageView imageView2 = new ImageView(context);
            imageView2.setId(i);
            imageView2.setImageBitmap(bm);
            //imageView2.setScaleType(ScaleType.FIT_XY);
            facesLayout.addView(imageView2);
            i++;

		}
		
		// SQLiteDatabase db = dbHelper.getReadableDatabase();
		// Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid,
		// getContext());
		// db.close();

		//dbHelper.getAllIdsFacesForPerson(personId)
		
		txtTitle.setText(personName);
		//FacesList adapter = new FacesList(context, face);
		//faces.setAdapter(adapter);
		
		// personImg.setImageBitmap(bm);

		return rowView;
	}

}
