package ru.trolleg.faces;

import ru.trolleg.faces.activities.MainActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

public class DragOnTrashListener implements OnDragListener {

    MainActivity act;

    public DragOnTrashListener(MainActivity act) {

        this.act = act;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
        case DragEvent.ACTION_DROP:
            Integer faceId = (Integer) event.getLocalState();
            Log.i("ItemOnDragListener", "faceId " + faceId);
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(act);
            Integer thrashPersonId = dbHelper.getOrCreatePerson(MainActivity.NO_FACES);
            //if (thrashPersonId != faceId) {
            dbHelper.addFaceToPerson(faceId, dbHelper.getPersStrById(thrashPersonId));
            act.adapterFaces.remove(faceId);
            act.adapterFaces.notifyDataSetChanged();
            break;
        }
        return true;
    }

}
