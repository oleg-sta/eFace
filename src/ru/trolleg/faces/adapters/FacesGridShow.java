package ru.trolleg.faces.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.BitmapWorkerFaceCrop;
import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.faces.activities.DisplayCommonPhoto;
import ru.trolleg.faces.activities.FacesActivity;
import ru.trolleg.faces.activities.PeopleFragment;
import ru.trolleg.faces.adapters.FacesGridAdapter.ViewHolder2;
import ru.trolleg.faces.data.Face;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import ru.trolleg.faces.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Лица для просмотра
 * @author sov
 *
 */
public class FacesGridShow extends ArrayAdapter<Integer> {
    
    private LocalBroadcastManager broadcastManager;
    
    private final FacesActivity context;
    public final List<Integer> faces; // �������������� ������
    public final Set<Integer> checked = new HashSet<Integer>();
    public boolean setAva = false;

    public FacesGridShow(FacesActivity context, List<Integer> faces) {
        super(context, R.layout.one_face, faces);
        this.faces = faces;
        this.context = context;
        broadcastManager = LocalBroadcastManager.getInstance(context);
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
        ViewHolder2 holder;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.one_face, null, true);
            holder = new ViewHolder2();
            holder.view = (ImageView)convertView.findViewById(R.id.one_face1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder2) convertView.getTag();
        }
        holder.position = position;
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        BitmapWorkerFaceCrop.loadImage(face, context, holder, position);
        holder.view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setAva) {
                    setAva = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(R.string.make_photo_ava);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dbHelper.setAvaId(context.personId, faceId);
                            // TODO так нехорошо делать
                            context.updateAva();

                            Intent intent = new Intent(PeopleFragment.UPDATE_PEOPLE);
                            broadcastManager.sendBroadcast(intent);
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("DragOverListMen", "No");
                        }
                    });
                    // Create the AlertDialog object and return it
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }
                Intent personIntent = new Intent(context, DisplayCommonPhoto.class);
                personIntent.putExtra(DataHolder.FACE_ID, faceId);
                context.startActivity(personIntent);

            }
        });
        holder.view.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.make_photo_ava);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHelper.setAvaId(context.personId, faceId);
                        // TODO так нехорошо делать
                        context.updateAva();

                        Intent intent = new Intent(PeopleFragment.UPDATE_PEOPLE);
                        broadcastManager.sendBroadcast(intent);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("DragOverListMen", "No");
                    }
                });
                // Create the AlertDialog object and return it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
        return convertView;
    }


    

}