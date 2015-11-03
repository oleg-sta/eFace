package ru.trolleg.faces;

import java.util.UUID;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.EditText;

public class DragOverListMen implements OnDragListener{

    final MainActivity act;

    public DragOverListMen(MainActivity act) {
        this.act = act;
    }
    
    @Override
    public boolean onDrag(View v, DragEvent event) {
        Log.i("DragOverListMen", "onDrag");
        switch (event.getAction()) {
        case DragEvent.ACTION_DROP:
            Integer faceId = (Integer) event.getLocalState();
            Log.i("DragOverListMen", "faceId " + faceId);
            final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(act);
            //Integer thrashPersonId = dbHelper.getOrCreatePerson(MainActivity.NO_FACES);
            String personGuid = UUID.randomUUID().toString();
            dbHelper.addPerson(personGuid);
            dbHelper.addFaceToPerson(faceId, personGuid);
            act.adapterFaces.remove(faceId);
            act.adapterFaces.notifyDataSetChanged();
            final Integer personId = dbHelper.getPersonIdByGuid(personGuid);
            act.adapterMans.add(personId);
            act.adapterMans.notifyDataSetChanged();
            
            final String[] name = {"Имя"};
            
            final EditText input = new EditText(act);
            input.setText("");
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setMessage("Введите имя");
            builder.setView(input).setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    name[0] =  input.getText().toString();
                    Log.i("DragOverListMen", "Yes " + name[0]);
                    dbHelper.updatePersonName(personId, name[0]);
                    act.adapterMans.notifyDataSetChanged();
                }
            }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
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
