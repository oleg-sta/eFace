package ru.trolleg.faces;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.trolleg.faces.data.Face;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Адаптер для просмотра сгруппированых лиц, т.е. персон
 * 
 * @author sov
 * 
 */
public class PersonList extends ArrayAdapter<Integer> {

    private final Activity context;
    public final List<Integer> personsId; // идентификаторы персон
    public final Set<Integer> checked = new HashSet<Integer>();

    public PersonList(Activity context, List<Integer> persons) {
        super(context, R.layout.my_list_item, persons);
        this.personsId = persons;
        this.context = context;
    }

    static class ViewHolder {

        private CheckBox box;
        private LinearLayout linearLayout;
        private TextView txtTitle;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        ViewHolder mViewHolder = null;

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.my_list_item, null, true);
            mViewHolder.txtTitle = (TextView) convertView.findViewById(R.id.person_name);
            mViewHolder.box = (CheckBox) convertView.findViewById(R.id.person_check);
            mViewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.facesLinear);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
            mViewHolder.linearLayout.removeAllViews();
        }

        final TextView txtTitle = mViewHolder.txtTitle;
        CheckBox box = mViewHolder.box;
        box.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checked.add(personsId.get(position));
                } else {
                    checked.remove(personsId.get(position));
                }

            }
        });
        box.setChecked(checked.contains(personsId.get(position)));

        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        LinearLayout facesLayout = mViewHolder.linearLayout;

        txtTitle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText input = new EditText(context);
                input.setText(txtTitle.getText());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(input).setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHelper.updatePersonName(personsId.get(position), input.getText().toString());
                        txtTitle.setText(input.getText().toString());
                    }
                }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // Create the AlertDialog object and return it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        String personName = dbHelper.getPersonName(personsId.get(position));
        List<Integer> faceIds = dbHelper.getAllIdsFacesForPerson(personsId.get(position));
        int i = 0;
        for (final Integer d : faceIds) {
            Face face = dbHelper.getFaceForId(d);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, getContext());
            db.close();
            ImageView imageView2 = new ImageView(context);
            imageView2.setId(i);
            imageView2.setImageBitmap(bm);
            facesLayout.addView(imageView2);
            imageView2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent personIntent = new Intent(context, DisplayPersonPhotos.class);
                    personIntent.putExtra(DisplayPersonPhotos.FACE_ID, d);
                    ((MainActivity) context).startActivity(personIntent);
                }
            });
            i++;
            if (i > (MainActivity.FACES_VERTICAL + 1)) {
                break;
            }
        }
        txtTitle.setText(personName);
        return convertView;
    }

}