package ru.trolleg.faces;

import ru.trolleg.faces.activities.RecognizeFragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

/**
 * ��� ��� �������� �� MainActivity
 * 
 * @author sov
 * 
 */
public class DragOverManListener implements OnDragListener {

    Integer i;
    RecognizeFragment act;

    public DragOverManListener(Integer integer, RecognizeFragment act) {
        this.i = integer;
        this.act = act;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
        case DragEvent.ACTION_DROP:
            Integer faceId = (Integer) event.getLocalState();
            Log.i("DragOverManListener", "faceId " + faceId + " to " + i);
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(act.getActivity());
            Integer oldPersonId = dbHelper.getPersonIdByFaceId(faceId);
            if (oldPersonId != i) {
                dbHelper.addFaceToPerson(faceId, dbHelper.getPersStrById(i));
                act.adapterFaces.remove(faceId);
                act.adapterFaces.notifyDataSetChanged();
                if (act.adapterFaces.isEmpty()) {
                    dbHelper.removePerson(oldPersonId);
                    act.adapterMans.remove(oldPersonId);
                    act.adapterMans.notifyDataSetChanged();
                }
            }
            break;
        }
        return true;
    }

}
