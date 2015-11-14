package ru.trolleg.faces.adapters;

import java.util.List;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.data.Face;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter  {
    private Context context;
    List<Integer> faces;
    public GalleryAdapter(Context c, List<Integer> faces) {
        this.faces = faces;
        context = c;
    }

    @Override
    public int getCount() {
        return faces.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        
        final int faceId = faces.get(position);
        final DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(context);
        Face face = dbHelper.getFaceForId(faceId);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Bitmap bm = DataHolder.getInstance().getLittleFace(db, face.guid, context);
        db.close();
        imageView.setImageBitmap(bm);
        

        return imageView;
    }

}
