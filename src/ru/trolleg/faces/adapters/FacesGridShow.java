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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
    public int getCount() {
        return faces.size() + FacesGridAdapter.WIDTH_NUM_PICS;
    }
    
    @Override
    public int getItemViewType(int position) {
        return (position < FacesGridAdapter.WIDTH_NUM_PICS ) ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public Integer getItem(int position) {
        return (position < FacesGridAdapter.WIDTH_NUM_PICS) ?
                null : faces.get(position - FacesGridAdapter.WIDTH_NUM_PICS);
    }
    
    @Override
    public View getView(final int pos, View convertView, ViewGroup parent) {
        final int position = pos - FacesGridAdapter.WIDTH_NUM_PICS;
        if (position < 0 || position >= faces.size()) {
            if (convertView == null) {
                convertView = new View(context);
            }
            convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 0));
            return convertView;
        }
        
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face, null, true);
        }
        ImageView view = (ImageView)convertView.findViewById(R.id.one_face1);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
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
                context.startActivity(personIntent);

            }
        });
       
        return convertView;
    }

}