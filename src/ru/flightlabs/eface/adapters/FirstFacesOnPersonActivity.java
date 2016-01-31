package ru.flightlabs.eface.adapters;

import java.util.List;

import ru.flightlabs.eface.BitmapWorkerFaceCrop;
import ru.flightlabs.eface.DataHolder;
import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.R;
import ru.flightlabs.eface.activities.FacesActivity;
import ru.flightlabs.eface.adapters.FacesGridAdapter.ViewHolder2;
import ru.flightlabs.eface.data.Face;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import ru.flightlabs.eface.Log;
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
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personIntent = new Intent(context, FacesActivity.class);
                Log.i("MenListOnPeopleActivity", "manId " + manId);
                personIntent.putExtra(DataHolder.PERSON_ID, manId);
                context.startActivity(personIntent);

            }
        });
        if (avaId != null) {
            Face face = dbHelper.getFaceForId(avaId);
            if (face != null) {
                BitmapWorkerFaceCrop.loadImage(face, context, holder, position, true);
            } else {
                holder.view.setImageBitmap(null);
            }
        }
        return convertView;
    }
    
    public static class ViewHolder3 extends ViewHolder2 {
        TextView text;
        TextView countText;
    }
}
