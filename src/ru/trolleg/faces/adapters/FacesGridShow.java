package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import ru.trolleg.faces.activities.FacesActivity;
import ru.trolleg.faces.data.Face;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Лица для просмотра
 * @author sov
 *
 */
public class FacesGridShow extends ArrayAdapter<Integer> {

    private final FacesActivity context;
    public final List<Integer> faces; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>();

    public FacesGridShow(FacesActivity context, List<Integer> faces) {
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
        ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        //final int personId = dbHelper.getPersonIdByFaceId(faceId);
        Face face = dbHelper.getFaceForId(faceId);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
        db.close();
        view.setImageBitmap(bm);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personIntent = new Intent(context, DisplayCommonPhoto.class);
                personIntent.putExtra(DataHolder.FACE_ID, faceId);
                // ((DisplayPersonPhotos)context).startActivity(personIntent);
                context.startActivity(personIntent);

            }
        });
        return convertView;
    }

}