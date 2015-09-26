package com.example.testfaceplus;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix.ScaleToFit;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Адаптер для просмотра сгруппированых лиц, т.е. персон
 * 
 * @author sov
 *
 */
public class PersonList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] person;
    
    public PersonList(Activity context, String[] person) {
        super(context, R.layout.show_faces, person);
        this.context = context;
        this.person = person;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single_person, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.person_name);
        txtTitle.setText(person[position]);

        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
       
        LinearLayout layout = (LinearLayout) rowView.findViewById(R.id.facesLinear);
        
        Cursor c2 = db.rawQuery("select guid from faces where person_id = '" + person[position] + "'"  , null);
        int i = 0;
        while (c2.moveToNext()) {
            String faceId = c2.getString(0);
            Log.w("PersonList", "face to linear " + i + " " + faceId);
            //Bitmap bm = DataHolder.getInstance().photos.get(faceId).littleFace;
            Bitmap bm = DataHolder.getInstance().getLittleFace(db, faceId);
            ImageView imageView2 = new ImageView(context);
            imageView2.setId(i);
            imageView2.setImageBitmap(bm);
            //imageView2.setScaleType(ScaleType.FIT_XY);
            layout.addView(imageView2);
            i++;

        }
        c2.close();
        db.close();
        
        return rowView;
    }
    
    

}
