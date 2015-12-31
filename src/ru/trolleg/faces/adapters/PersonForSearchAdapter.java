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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Адаптер людей для поиска фотографий в галерее
 * @author sov
 *
 */
public class PersonForSearchAdapter extends ArrayAdapter<Integer>{
    private final Activity context;
    public final List<Integer> men;
    public final Set<Integer> checked = new HashSet<Integer>();

    public PersonForSearchAdapter(Activity context, List<Integer> men) {
        super(context, R.layout.face_for_seach, men);
        this.men = men;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.face_for_seach, null, true);
        }
        final TextView text = (TextView) convertView.findViewById(R.id.name_man);
        final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.check_box);
        checkBox.setChecked(checked.contains(position));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checked.add(position);
                } else {
                    checked.remove(position);
                }
            }
        });
        final int manId = men.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        final String name = dbHelper.getPersonName(manId);
        text.setText(name);
        Integer avaId = dbHelper.getAvaFace(manId);
        ImageView l1 = (ImageView) convertView.findViewById(R.id.first_face);
        if (avaId != null) {
            Face face = dbHelper.getFaceForId(avaId);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Bitmap bm = DataHolder.getInstance().getLittleFaceInCirle(db, face.guid, getContext());
            db.close();
            l1.setImageBitmap(bm);


        }
        return convertView;
    }
}
