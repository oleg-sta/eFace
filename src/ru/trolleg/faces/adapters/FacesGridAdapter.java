package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import ru.trolleg.faces.activities.ShowCommonPhoto;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Лица для распознавания
 * 
 * @author sov
 * 
 */
public class FacesGridAdapter extends ArrayAdapter<Integer> {

    private final Activity context;
    public final List<Integer> faces; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>(); // TODO лучше faces id хранить, так независимо будет от единичного убирания

    public FacesGridAdapter(Activity context, List<Integer> faces) {
        super(context, R.layout.one_face, faces);
        this.faces = faces;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face, null, true);
        }
        final ImageView view2 = (ImageView)convertView.findViewById(R.id.checked);
        view2.setVisibility(checked.contains(position)? View.VISIBLE : View.INVISIBLE);
        ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
        db.close();
        view.setImageBitmap(bm);
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String photo = dbHelper.getPhotoPathByFaceId(faceId);
                Intent personIntent = new Intent(context, ShowCommonPhoto.class);
                personIntent.putExtra("photo", photo);
                context.startActivity(personIntent);
                return true;
            }
        });
        view.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())  {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView) v;
                    // argb
                    view.setPadding(5, 5, 5, 5);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    view.setPadding(0, 0, 0, 0);
                    view.invalidate();
                    break;
                }
            }
            return false;
            }

        });
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                view2.setVisibility(view2.getVisibility() == View.VISIBLE? View.INVISIBLE : View.VISIBLE);
                if (view2.getVisibility() == View.VISIBLE) {
                    checked.add(position);
                } else {
                    checked.remove(position);
                }
                
            }
        });
        return convertView;
    }

}