package ru.trolleg.faces;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.data.Face;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenListOnPeopleActivity extends ArrayAdapter<Integer>{
    private final PeopleActivity context;
    public final List<Integer> men; // идентификаторы персон
    public final Set<Integer> checked = new HashSet<Integer>();

    public MenListOnPeopleActivity(PeopleActivity context, List<Integer> men) {
        super(context, R.layout.name_faces, men);
        this.men = men;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.name_faces, null, true);
        }
        ImageView view = (ImageView) convertView.findViewById(R.id.face_man);
        TextView text = (TextView) convertView.findViewById(R.id.name_man);
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
        }
        return convertView;
    }

}
