package ru.trolleg.faces;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Отображение всех лиц человека.
 * @author sov
 *
 */
public class DisplayPersonPhotos extends Activity {

    public static final String FACE_ID = "faceId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
        setContentView(R.layout.activity_person);
        Integer faceId = (Integer) getIntent().getIntExtra(FACE_ID, 0);
        Log.i("DisplayPersonPhotos", faceId + "");
        // String pathPhoto = dbHelper.getPhotoPathByFaceId(faceId);
        TextView namePerson = (TextView) findViewById(R.id.name_person);
        ListView facesLayout = (ListView) findViewById(R.id.vertical_faces);
        Integer personId = dbHelper.getPersonIdByFaceId(faceId);
        namePerson.setText(dbHelper.getPersonName(personId));
        List<Integer> faces = dbHelper.getAllIdsFacesForPerson(personId);
        Log.i("DisplayPersonPhotos", faces.size() + "");
        Integer[] heightTableDummy = new Integer[(faces.size() + MainActivity.FACES_VERTICAL - 1)
                / MainActivity.FACES_VERTICAL];
        Log.i("DisplayPersonPhotos", heightTableDummy.length + " d");
        FacesList facesList = new FacesList(this, Arrays.asList(heightTableDummy), personId, faces);
        facesLayout.setAdapter(facesList);

    }

}
