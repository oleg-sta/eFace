package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.R.id;
import ru.trolleg.faces.R.layout;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import ru.trolleg.faces.activities.DisplayPersonPhotos;
import ru.trolleg.faces.activities.MainActivity;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ������� ��� ��������� �������������� ���, �.�. �����.
 * 
 * @author sov
 * 
 */
public class FacesList2 extends ArrayAdapter<Integer> {

    private final MainActivity context;
    public final List<Integer> faces; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>();

    public FacesList2(MainActivity context, List<Integer> faces) {
        super(context, R.layout.one_face, faces);
        this.faces = faces;
        this.context = context;
    }

    static class ViewHolder {

        private CheckBox box;
        private LinearLayout linearLayout;
        private TextView txtTitle;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face, null, true);
        }
        ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        //final int personId = dbHelper.getPersonIdByFaceId(faceId);
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
                view.startDrag(data, shadowBuilder, faceId, 0);
                return true;
            }
        });
//        view.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                Intent personIntent = new Intent(context, DisplayCommonPhoto.class);
//                personIntent.putExtra(DisplayPersonPhotos.FACE_ID, faceId);
//                // ((DisplayPersonPhotos)context).startActivity(personIntent);
//                context.startActivity(personIntent);
//                
//            }
//        });
        return convertView;
    }

}