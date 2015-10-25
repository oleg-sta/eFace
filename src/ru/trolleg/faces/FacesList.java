package ru.trolleg.faces;

import java.util.List;

import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FacesList extends ArrayAdapter<Integer> {

    public static final int FACES_SIZE = 150;

    private final Activity context;
    private final int personId;
    private final List<Integer> horizs;

    public FacesList(Activity context, List<Integer> verts, int personId, List<Integer> faces) {
        super(context, R.layout.list_fases, verts);
        this.context = context;
        this.personId = personId;
        horizs = verts;
        // this.faces = faces;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("FacesList", "position " + position);
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_fases, null, true);
        LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.list_faces);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        final List<Integer> faces = dbHelper.getAllIdsFacesForPerson(personId);
        for (int i = position * MainActivity.FACES_VERTICAL; i < ((position + 1) * MainActivity.FACES_VERTICAL)
                && i < faces.size(); i++) {
            Log.i("FacesList", "i " + i);
            final int faceId = faces.get(i);
            Face face = dbHelper.getFaceForId(faces.get(i));
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
            db.close();
            ImageView imageView2 = new ImageView(context);
            imageView2.setId(i);
            imageView2.setImageBitmap(bm);
            lay.addView(imageView2);
            imageView2.setLongClickable(true);
            imageView2.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // Нельзя удалить последний элемент
                    if (faces.size() == 1) {
                        return true;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Удалить лицо").setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dbHelper.removeFromPerson(faceId);
                            if (horizs.size() < faces.size() / MainActivity.FACES_VERTICAL) {
                                horizs.remove(0);
                            }
                            notifyDataSetChanged();
                        }
                    }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    // Create the AlertDialog object and return it
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
            imageView2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i("FacesList", "imageView2.setOnClickListener");
                    Intent personIntent = new Intent(context, DisplayCommonPhoto.class);
                    personIntent.putExtra(DisplayPersonPhotos.FACE_ID, faceId);
                    // ((DisplayPersonPhotos)context).startActivity(personIntent);
                    context.startActivity(personIntent);

                }
            });
        }

        return convertView;
    }

}
