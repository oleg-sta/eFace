package ru.trolleg.faces;

import java.util.UUID;

import ru.trolleg.faces.activities.RecognizeFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.EditText;

public class DragOverListMen implements OnDragListener{

    final RecognizeFragment act;

    public DragOverListMen(RecognizeFragment act) {
        this.act = act;
    }
    
    @Override
    public boolean onDrag(View v, DragEvent event) {
        Log.i("DragOverListMen", "onDrag");
        switch (event.getAction()) {
        case DragEvent.ACTION_DROP:
            Integer faceId = (Integer) event.getLocalState();
            Log.i("DragOverListMen", "faceId " + faceId);
            final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(act.getActivity());
            final int personId = dbHelper.addPerson();
            dbHelper.addFaceToPerson(faceId, personId);
            act.adapterFaces.remove(faceId);
            act.adapterFaces.notifyDataSetChanged();
            act.adapterMans.add(personId);
            act.adapterMans.notifyDataSetChanged();
            
            final String[] name = {"Имя"};
            
            final EditText input = new EditText(act.getActivity());
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            input.setText("");
            AlertDialog.Builder builder = new AlertDialog.Builder(act.getActivity());
            builder.setMessage("Введите имя");
            builder.setView(input).setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    name[0] =  input.getText().toString();
                    Log.i("DragOverListMen", "Yes " + name[0]);
                    dbHelper.updatePersonName(personId, name[0]);
                    act.adapterMans.notifyDataSetChanged();
                }
            }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.i("DragOverListMen", "No");
                }
            });
            // Create the AlertDialog object and return it
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Log.i("DragOverListMen", "name " + name[0]);
            
            break;
        }
        return true;
    }

}
