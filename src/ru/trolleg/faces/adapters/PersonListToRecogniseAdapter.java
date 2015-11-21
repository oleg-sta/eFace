package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.DragOverManListener;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.RecognizeFragment;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonListToRecogniseAdapter extends ArrayAdapter<Integer> {

    private final Activity context;
    public final List<Integer> men; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>();
    RecognizeFragment act;

    public PersonListToRecogniseAdapter(Activity context, List<Integer> men, RecognizeFragment act) {
        super(context, R.layout.one_face_and_name, men);
        this.men = men;
        this.context = context;
        this.act = act;
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
        String name = dbHelper.getPersonName(manId);
        text.setText(name);
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(manId);
        if (faces.size() > 0) {
            Face face = dbHelper.getFaceForId(faces.get(0));
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
            db.close();
            view.setImageBitmap(bm);
            view.setOnDragListener(new DragOverManListener(manId, act));
            view.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (act.currentMan != null && act.currentMan == manId) {
                        return;
                    }
                    if (act.adapterFaces.checked.isEmpty()) {
                        act.setCurrentMan(manId);
                    } else {
                        Set<Integer> facesRemove = new HashSet<Integer>();
                        for (int positionid : act.adapterFaces.checked) {
                            int faceId = act.adapterFaces.faces.get(positionid);
                            dbHelper.addFaceToPerson(faceId, manId);
                            facesRemove.add(faceId);
                        }
                        for (int faceId : facesRemove) {
                            act.adapterFaces.remove(faceId);
                        }
                        act.adapterFaces.checked.clear();
                        act.adapterFaces.notifyDataSetChanged();
                        act.adapterMans.notifyDataSetChanged();
                    }
                    
                }
            });
        }
        return convertView;
    }

}
