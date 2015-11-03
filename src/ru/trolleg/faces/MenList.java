package ru.trolleg.faces;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.data.Face;

import android.content.ClipData;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenList extends ArrayAdapter<Integer> {

    private final MainActivity context;
    public final List<Integer> men; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>();

    public MenList(MainActivity context, List<Integer> men) {
        super(context, R.layout.one_face_and_name, men);
        this.men = men;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face_and_name, null, true);
        }
        ImageView view = (ImageView) convertView.findViewById(R.id.one_face1);
        TextView text = (TextView) convertView.findViewById(R.id.name_face);
        final int manId = men.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        // final int personId = dbHelper.getPersonIdByFaceId(faceId);
        String name = dbHelper.getPersonName(manId);
        text.setText(name);
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(manId);
        if (faces.size() > 0) {
            Face face = dbHelper.getFaceForId(faces.get(0));
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
            db.close();
            view.setImageBitmap(bm);
            view.setOnDragListener(new DragOverManListener(manId, context));
            view.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    context.setCurrentMan(manId);
                    
                }
            });
            // view.setOnLongClickListener(new OnLongClickListener() {
            //
            // @Override
            // public boolean onLongClick(View view) {
            // ClipData data = ClipData.newPlainText("", "");
            // DragShadowBuilder shadowBuilder = new
            // View.DragShadowBuilder(view);
            // view.startDrag(data, shadowBuilder, faceId, 0);
            // return true;
            // }
            // });
        }
        return convertView;
    }

}
