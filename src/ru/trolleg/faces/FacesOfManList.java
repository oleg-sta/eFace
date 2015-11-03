package ru.trolleg.faces;

import java.util.List;

import ru.trolleg.faces.FacesList2.ViewHolder;
import ru.trolleg.faces.data.Face;

import android.content.ClipData;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class FacesOfManList extends ArrayAdapter<Integer> {

    private final MainActivity context;
    public final List<Integer> faces;

    public FacesOfManList(MainActivity context, List<Integer> faces) {
        super(context, R.layout.one_face, faces);
        this.faces = faces;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("FacesOfManList", "getView " + position);
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(R.layout.one_face, null, true);
        ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        final int personId = dbHelper.getPersonIdByFaceId(faceId);
        Face face = dbHelper.getFaceForId(faceId);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
        db.close();
        view.setImageBitmap(bm);
        view.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                // view.setVisibility(View.INVISIBLE);
                return true;
            }
        });
        //view.setOnDragListener(new DragOverManListener(personId, context));

//        view.setOnTouchListener(new OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    ClipData data = ClipData.newPlainText("", "");
//                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
//                    view.startDrag(data, shadowBuilder, view, 0);
//                    //view.setVisibility(View.INVISIBLE);
//                    return true;
//                  } else {
//                  return false;
//                  }
//            }
//            
//        });
        //convertView.setOnDragListener(new OnIt());
        return convertView;
    }
    

}
