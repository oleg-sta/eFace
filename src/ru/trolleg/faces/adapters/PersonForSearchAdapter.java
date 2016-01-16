package ru.trolleg.faces.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.BitmapWorkerFaceCrop;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter.ViewHolder2;
import ru.trolleg.faces.data.Face;

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
        ViewHolder3 holder;
        if (convertView == null) {
            holder = new ViewHolder3();
            convertView = inflater.inflate(R.layout.face_for_seach, null, true);
            holder.view = (ImageView) convertView.findViewById(R.id.first_face);
            holder.checkBox = (CheckBox)convertView.findViewById(R.id.check_box);
            holder.text = (TextView) convertView.findViewById(R.id.name_man);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder3) convertView.getTag();
        }
        final CheckBox checkBox = holder.checkBox;
        holder.position = position;
        holder.checkBox.setChecked(checked.contains(position));
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    checked.add(position);
                } else {
                    checked.remove(position);
                }
            }
        });
        final int manId = men.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        final String name = dbHelper.getPersonName(manId);
        holder.text.setText(name);
        Integer avaId = dbHelper.getAvaFace(manId);
        if (avaId != null) {
            Face face = dbHelper.getFaceForId(avaId);
            BitmapWorkerFaceCrop.loadImage(face, context, holder, position, true);
        }
        return convertView;
    }

    public void checkAll(boolean isChecked) {
        if (!isChecked) {
            checked.clear();
            return;
        }
        for (int i = 0; i < men.size(); i++) {
            checked.add(i);
        }
    }
    
    public static class ViewHolder3 extends ViewHolder2 {
        CheckBox checkBox;
        TextView text;
    }
}
