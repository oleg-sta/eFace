package com.example.testfaceplus;

import java.util.List;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

public class FaceFinderService extends IntentService  {


    public FaceFinderService() {
        super("FaceFinderService");
    }
    public FaceFinderService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("service222", "onStartCommand");
        final ResultReceiver  rec = (ResultReceiver) intent.getParcelableExtra("receiver");
        List<String> photos = MainActivity.getCameraImages(getApplicationContext());
        DataHolder dataHolder = DataHolder.getInstance();
        for (String photo : photos) {
            if (!dataHolder.infos.containsKey(photo)) {
                Log.d("service222", "photo" + photo);
                dataHolder.infos.put(photo, new InfoPhoto());
                Bitmap littleBit = BitmapWorkerTask.shrinkBitmap(photo, 50, 50);
                
                BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
                bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
                //Bitmap background_image = BitmapFactory.decodeFile(photo, bitmap_options);
                
                final BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap background_image = decodeSampledBitmapFromResource(photo, 500, 500, options);
                
                // use different methods
                FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(), 10);
                FaceDetector.Face[] faces = new FaceDetector.Face[BitmapWorkerTask.MAX_FACES];
                int faceCount = face_detector.findFaces(background_image, faces);
                
                                
                InfoPhoto infoPhoto = new InfoPhoto();
                infoPhoto.scaleFactor = options.inSampleSize;
                infoPhoto.littlePhoto = littleBit;
                infoPhoto.faces = faces;
                infoPhoto.faceCount = faceCount;
                dataHolder.infos.put(photo, infoPhoto);
                
                // сообщения для UI о готовности фото
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    //Messenger messenger = (Messenger) bundle.get("receiver");
                    //Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putString("photo", photo);
                    rec.send(0, b);
                    //messenger.send(msg);
                }
            }
        }
        
    }
    
    public static Bitmap decodeSampledBitmapFromResource(String photo, 
            int reqWidth, int reqHeight, Options options) {

        // First decode with inJustDecodeBounds=true to check dimensions
        //final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(photo, options);
    }
    
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}

}
