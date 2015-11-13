package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.FacesActivity;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FirstFacesOnPersonActivity extends ArrayAdapter<Integer>{
    private final Activity context;
    public final List<Integer> men; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>();

    public FirstFacesOnPersonActivity(Activity context, List<Integer> men) {
        super(context, R.layout.name_faces, men);
        this.men = men;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.name_faces, null, true);
        }
        //ImageView view = (ImageView) convertView.findViewById(R.id.face_man);
        TextView text = (TextView) convertView.findViewById(R.id.name_man);
        final int manId = men.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        String name = dbHelper.getPersonName(manId);
        text.setText(name);
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(manId);
        LinearLayout l1 = (LinearLayout) convertView.findViewById(R.id.faces);
        l1.removeAllViews();
        if (faces.size() > 0) {
            for (int i = 0; (i < faces.size()) && (i < 3); i++) {
                Face face = dbHelper.getFaceForId(faces.get(i));
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
                db.close();
                ImageView imageView2 = new ImageView(context);
                imageView2.setId(i);
                imageView2.setImageBitmap(bm);
                l1.addView(imageView2);
                imageView2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent personIntent = new Intent(context, FacesActivity.class);
                        Log.i("MenListOnPeopleActivity", "manId " + manId);
                        personIntent.putExtra(DataHolder.PERSON_ID, manId);
                        context.startActivity(personIntent);

                    }
                });

            }
        }
        return convertView;
    }

}