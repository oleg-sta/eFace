package ru.trolleg.faces.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.trolleg.faces.BitmapWorkerFaceCrop;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.adapters.FacesGridAdapter.ViewHolder2;
import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class FacesCommonAdapter extends ArrayAdapter<Integer> {

    private final Activity context;
    public final List<Integer> faces; // �������������� ������
    public int selected = -1;
    
    public Map<Integer, ViewHolder> forUpdate = new HashMap<Integer, ViewHolder>();

    public FacesCommonAdapter(Activity context, List<Integer> faces) {
        super(context, R.layout.one_face2, faces);
        this.faces = faces;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face2, null, true);
            holder = new ViewHolder();
            holder.view = (ImageView)convertView.findViewById(R.id.one_face1);
            holder.view2 = (ImageView)convertView.findViewById(R.id.one_face2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.position = position;
        forUpdate.put(position, holder);
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        BitmapWorkerFaceCrop.loadImage(face, context, holder, position);
        if (position != selected) {
            holder.view2.setVisibility(View.INVISIBLE);
        } else {
            holder.view2.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
    
    public static class ViewHolder extends ViewHolder2 {
    }
}
