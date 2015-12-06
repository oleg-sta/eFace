package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FacesCommonAdapter extends ArrayAdapter<Integer> {

    private final Activity context;
    public final List<Integer> faces; // �������������� ������
    public int selected = 0;
    
    int lastPosition;
    public View  imLast;

    public FacesCommonAdapter(Activity context, List<Integer> faces) {
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
        // TODO images get in background
        ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        ImageView view2 = (ImageView)convertView.findViewById(R.id.one_face2);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
        db.close();
        view.setImageBitmap(bm);
        if (position != selected) {
            view2.setVisibility(View.GONE);
        } else {
            view2.setVisibility(View.VISIBLE);
            imLast = convertView;
        }
        return convertView;
    }
    
}
