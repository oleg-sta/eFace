package com.example.testfaceplus;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testfaceplus.data.InfoPhoto;

/**
 * Адаптер для просмотра фотографий с количеством найденых лиц
 * 
 * @author sov
 *
 */
public class PhotoList extends ArrayAdapter<String> {

    // TODO use only for photos
    //private LruCache<String, InfoPhoto> mMemoryCache;
    
    private final Activity context;
    public final List<String> web;

    // private final Integer[] imageId;

    public PhotoList(Activity context, List<String> web) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        
        Log.v("blah", "start");
        //this.mMemoryCache =mMemoryCache;

    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //System.out.println("pos"+position);
        Log.v("blah", "pos"+position);
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        txtTitle.setText(web.get(position));

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        TextView numFaces = (TextView) rowView.findViewById(R.id.num_faces);
        

        File f = new File(web.get(position));
        if (f.exists()) {
            InfoPhoto infoPhoto = DataHolder.getInstance().infos.get(web.get(position));
            // Bitmap myBitmap = this.mMemoryCache.get(web[position]);
            if (infoPhoto != null) {
                if (infoPhoto.littlePhoto != null) {
                imageView.setImageBitmap(infoPhoto.littlePhoto);
                numFaces.setText("" + infoPhoto.faceCount);
                }
                //imageView.setImageBitmap(infoPhoto.littlePhoto);
            } else {
                //BitmapWorkerTask task = new BitmapWorkerTask(imageView, numFaces);
                //task.execute(web[position]);
            }
            /*
            Bitmap myBitmap = this.mMemoryCache.get(web[position]);
            if (myBitmap != null) {
                imageView.setImageBitmap(myBitmap);
            } else {
            // Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            myBitmap = shrinkBitmap(f.getAbsolutePath(), 50, 50);
            if (myBitmap != null) {
                
                imageView.setImageBitmap(myBitmap);
                mMemoryCache.put(web[position], myBitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher);
            }
            }
            */
        } else {
            imageView.setImageResource(R.drawable.ic_launcher);
        }
        return rowView;
    }

    Bitmap shrinkBitmap(String file, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }
}
