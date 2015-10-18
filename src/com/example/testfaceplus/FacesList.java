package com.example.testfaceplus;

import java.util.List;

import com.example.testfaceplus.data.Face;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class FacesList extends ArrayAdapter<Integer>  {
    private final Activity context;
    private final List<Integer> faces;
	public FacesList(Activity context, List<Integer> faces) {
        super(context, R.layout.faces, faces);
        this.context = context;
        this.faces = faces;
    }
	
    public View getView(int position, View convertView, ViewGroup parent) {
        //LayoutInflater inflater = context.getLayoutInflater();
        ImageView imageView2 = new ImageView(context);
        
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
       
        Face face = dbHelper.getFaceForId(faces.get(position));
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
        db.close();
        imageView2.setImageBitmap(bm);
        
        return imageView2;
    }

}
