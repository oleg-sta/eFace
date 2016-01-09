package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.BitmapWorkerFaceCrop;
import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.FacesActivity;
import ru.trolleg.faces.adapters.FacesGridAdapter.ViewHolder2;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FirstFacesOnPersonActivity extends ArrayAdapter<Integer>{
    private final Activity context;
    public final List<Integer> men;
    public final Set<Integer> checked = new HashSet<Integer>();

    public FirstFacesOnPersonActivity(Activity context, List<Integer> men) {
        super(context, R.layout.name_faces, men);
        this.men = men;
        this.context = context;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        ViewHolder3 holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.name_faces, null, true);
            holder = new ViewHolder3();
            holder.view = (ImageView)convertView.findViewById(R.id.first_face);
            holder.text = (TextView) convertView.findViewById(R.id.name_man);
            holder.countText = (TextView) convertView.findViewById(R.id.faces_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder3) convertView.getTag();
        }
        holder.position = position;
        final int manId = men.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        final String name = dbHelper.getPersonName(manId);
        holder.text.setText(name);
        List<Integer> faces2 = dbHelper.getAllIdsFacesForPerson(manId);
        Integer avaId = dbHelper.getAvaFace(manId);
        Resources res = getContext().getResources();
        String photoCount = res.getQuantityString(R.plurals.numberOfPhoto, faces2.size(), faces2.size());
        holder.countText.setText(photoCount);
        if (avaId != null) {
            Face face = dbHelper.getFaceForId(avaId);
            
            BitmapWorkerFaceCrop.loadImage(face, context, holder, position, true);
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent personIntent = new Intent(context, FacesActivity.class);
                    Log.i("MenListOnPeopleActivity", "manId " + manId);
                    personIntent.putExtra(DataHolder.PERSON_ID, manId);
                    context.startActivity(personIntent);

                }
            });

        }
        return convertView;
    }
    
    public static class ViewHolder3 extends ViewHolder2 {
        TextView text;
        TextView countText;
    }
}
