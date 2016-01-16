package ru.trolleg.faces;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ru.trolleg.faces.activities.MainActivity;
import ru.trolleg.faces.activities.NavigationDrawer;
import ru.trolleg.faces.activities.PeopleFragment;
import ru.trolleg.faces.data.Face;
import ru.trolleg.faces.data.Photo;
import ru.trolleg.faces.jni.Computations;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import detection.Rectangle;

/**
 * 
 * @author sov
 *
 */
public class FaceFinderService extends IntentService {

    private static final String TAG = FaceFinderService.class.getSimpleName();
    private LocalBroadcastManager broadcastManager;
    
    public final static String OPER = "operation"; 
    public enum Operation { FIND_PHOTOS, FIND_FACES, SHOW_BUTTON, HIDE_BUTTON };
    
    private static final int notif_id=1;
    public static boolean buttonStart = false;
    public String lastMessage = null;

    static FaceFinderService instance;
    public final static int PHOTOS_LIMIT = 3000;
    public final static int PHOTOS_SIZE_TO_BE_PROCESSED = 800;
	public final static int PHOTOS_SIZE_TO_BE_CUT = 600;
	
    public FaceFinderService() {
        super("FaceFinderService");
    }

    public FaceFinderService(String name) {
        super(name);
        Logger1.log("FaceFinderService");
        // TODO Auto-generated constructor stub
    }
    
    private Notification getMyActivityNotification(String text) {
        // The PendingIntent to launch our activity if the user selects
        // this notification
        CharSequence title = getText(R.string.app_name);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NavigationDrawer.class), 0);

        return new Notification.Builder(this).setContentTitle(title).setContentText(text)
                .setSmallIcon(R.drawable.stat_notify_chat).setContentIntent(contentIntent).getNotification();
    }
    
    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte [] buffer = new byte[256];
        int bytesRead = 0;
        while((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
    
    // запуск для подсчета фотографий и обработки
    @Override
    protected void onHandleIntent(Intent intent) {
        Logger1.log("onHandleIntent");
        Log.d(TAG, "onHandleIntent " + intent);
        //Bundle bundle = null;
        //ResultReceiver rec = null;
        WakeLock wakeLock = null;
        try {
            
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
            Operation oper = null;
            if (intent != null) {
                oper = (Operation) intent.getSerializableExtra(OPER);
            } else {
                buttonStart = true;
            }
            
            if (oper == Operation.FIND_PHOTOS) {
                List<Photo> allPhotos = MainActivity.getCameraPhotos(getApplicationContext());
                DataHolder.photoCount = allPhotos.size();
                Intent intent22 = new Intent(PeopleFragment.UPDATE_FACES);
                broadcastManager.sendBroadcast(intent22);
                Log.d(TAG, "onHandleIntent photos " + allPhotos.size());
                // удаление лиц с удаленными фотографиями
                dbHelper.addNewPhotos(allPhotos);
                int newPhotos = dbHelper.getCountNewPhotos();
                Log.d(TAG, "onHandleIntent newFaces " + newPhotos);
                buttonStart = false;
                DataHolder.photoCount = allPhotos.size();
                DataHolder.photoProcessedCount = dbHelper.getAllCountPhotosProcessed();
                DataHolder.facesCount = dbHelper.getFacesCount();
                broadcastManager.sendBroadcast(intent22);
                return;
            }
            
            lastMessage = "";
            PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
            wakeLock.acquire();
            
            Notification note = new Notification(R.drawable.stat_notify_chat, "Обработка фотографий запущена",
                    System.currentTimeMillis());
            Intent i2 = new Intent(this, NavigationDrawer.class);
            i2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i2, 0);
            note.setLatestEventInfo(this, "Обработка фотографий", "Подождите...", pi);
            note.flags |= Notification.FLAG_NO_CLEAR;
            startForeground(notif_id, note);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NavigationDrawer.class), 0);
            NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setContentTitle(getText(R.string.app_name)).setContentText("Обработка начата").setSmallIcon(R.drawable.stat_notify_chat);
            mBuilder.setContentIntent(contentIntent);
            
            DataHolder dataHolder = DataHolder.getInstance();
            Runtime info = Runtime.getRuntime();
            int threadsNum = info.availableProcessors();
                        
            Log.d(TAG, "onHandleIntent threads " + threadsNum);
            Logger1.log("onHandleIntent threads " + threadsNum);
            
            // find faces on photos
            List<String> photos = dbHelper.getAllPhotosToBeProcessed();
            Log.d(TAG, "onHandleIntent photos " + photos.size());
            if (photos.size() == 0) {
                Log.d(TAG, "zero photos ");
                buttonStart = false;
            	return;
            }
            if (!buttonStart) {
                Log.d(TAG, "button stop ");
                return;
            }
            Log.d(TAG, "loading casade...");
            Logger1.log("loading casade...");
            //InputStream inputHaas = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            //Detector detector = Detector.create(inputHaas);
            Log.i(TAG, getFilesDir().getAbsolutePath());
            String detecrtorName = getFilesDir() + File.separator + "detector.xml";
            rawResourceToFile(R.raw.my_detector, detecrtorName);
            
            String secondDetectorName = getFilesDir() + File.separator + "detector_second.xml";
            rawResourceToFile(R.raw.my_detector_pr_2, secondDetectorName);
            // just test
            //detecrtorName = secondDetectorName; 
 
            Log.d(TAG, "casade loaded");
            Logger1.log("casade loaded");
            int iPh = 0;
            for (String photo : photos) {
                try {
                    if (!buttonStart) {
                        break;
                    }
                    mBuilder.setContentText(iPh + " из " + photos.size() + " обработано");
                    mBuilder.setProgress(photos.size(), iPh, false);
                    Notification not = mBuilder.build();
                    //not.flags = not.flags | Notification.FLAG_INSISTENT;
                    not.flags = not.flags | Notification.FLAG_ONGOING_EVENT;
                    mNotifyManager.notify(notif_id, not);
                    
                    iPh++;
                    Log.d(TAG, "photo" + photo);
                    Logger1.log("photo " + photo + " " + iPh + " " + photos.size());
                    if (!new File(photo).exists()) {
                        Log.d(TAG, "photo " + photo + " doesn't exist");
                        continue;
                    }

                    
                    ExifInterface exif = new ExifInterface(photo);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int orient = getOrient(orientation);
                    Log.i(TAG, "orientation " + orientation);
                    
                    
                    BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
                    bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;

                    final BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(photo, options2);
                    int height = options2.outHeight;
                    int width = options2.outWidth;
                    Log.i(TAG, "original " + width + " " + height);
                    //double koef  = calculateInSampleSize(options2, PHOTOS_SIZE_TO_BE_CUT, PHOTOS_SIZE_TO_BE_CUT);
                    //height = (int)(height / koef);
                    //width = (int) (width / koef);
                    if (orient % 2 == 1) {
                        int w1 = height;
                        height = width;
                        width = w1;
                    }
                    double koef = Math.min((double)height / PHOTOS_SIZE_TO_BE_PROCESSED, (double)width / PHOTOS_SIZE_TO_BE_PROCESSED);
                    if (koef < 1) {
                        koef = 1;
                    }
                    height = (int)(height / koef);
                    width = (int)(width / koef);
                    Log.i(TAG, "koef " + koef + " " + width + " " + height);
                    
                    long time = System.currentTimeMillis();
                    Computations comp = new Computations();
                    
                    List<Rectangle> res = Arrays.asList(comp.findFaces2(detecrtorName, secondDetectorName, photo, 1 / koef, orient));
                    time = (System.currentTimeMillis() - time) / 1000;
                    Logger1.log("find in " + time);
                    Log.i(TAG, "foune " + res.size() + " faces");

                    Face[] faces = new Face[res.size()];
                    if (res.size() == 0) {
                        dbHelper.updatePhoto(photo, time);
                    }
                    int photoId = dbHelper.getPhotoIdByPath(photo);
                    for (int i = 0; i < res.size(); ++i) {
                        if (i == 0) {
                            dbHelper.updatePhoto(photo, time);
                        }
                        // FaceppResult face = result.get("face").get(i);
                        Rectangle face = res.get(i);
                        // FaceppResult position = face.get("position");
                        Face faceCur = new Face();
                        faces[i] = faceCur;
                        faceCur.height = 100 * face.height / (double) height;
                        faceCur.width = 100 * face.width / (double) width;
                        faceCur.centerY = 100 * (face.y + face.height / 2) / (double) height;
                        faceCur.centerX = 100 * (face.x + face.width / 2) / (double) width;
                        faceCur.guid = UUID.randomUUID().toString();
                        faceCur.probability = face.probability;
                        dbHelper.addFace(faceCur, photoId);
                    }
                    Log.d(TAG, "send processed photo " + photo);
                    Intent intent22 = new Intent(PeopleFragment.UPDATE_FACES);
                    DataHolder.photoProcessedCount = dbHelper.getAllCountPhotosProcessed();
                    DataHolder.facesCount = dbHelper.getFacesCount();
                    intent22.putExtra("photo", photo);
                    broadcastManager.sendBroadcast(intent22);
                 } catch (Exception e) {
                    Log.d(TAG, "error" + e.getMessage());
                    Logger1.log("error" + e.getMessage());
                    e.printStackTrace();
                    dbHelper.updatePhoto(photo, -1);
                }
            }
            if (iPh == photos.size()) {
                buttonStart = false;
                
                Intent intent22 = new Intent(PeopleFragment.UPDATE_FACES);
                DataHolder.photoProcessedCount = dbHelper.getAllCountPhotosProcessed();
                DataHolder.facesCount = dbHelper.getFacesCount();
                broadcastManager.sendBroadcast(intent22);
            }
            mBuilder.setContentText("Обработка завершена");
            mBuilder.setProgress(iPh, iPh, false);
            Notification not = mBuilder.build();
            not.defaults |= Notification.DEFAULT_SOUND;
            mNotifyManager.notify(notif_id, not);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "error" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (wakeLock != null) {
                wakeLock.release();
            }
        }
    }
    
    private Rectangle rectFromFace(android.media.FaceDetector.Face face, int i, int j)
    {
        PointF pointf = new PointF();
        face.getMidPoint(pointf);
        int k = Math.max(0, (int)(pointf.x - face.eyesDistance() * 2.0F - 0.5F));
        i = Math.min(i, (int)(pointf.x + face.eyesDistance() * 2.0F + 0.5F));
        float f = face.eyesDistance() * 2.0F * 3F;
        return new Rectangle(k, Math.max(0, (int)(pointf.y - f / 2.0F - 0.5F)), i, Math.min(j, (int)(pointf.y + f / 2.0F + 0.5F)), 1);
    }


    private void rawResourceToFile(int idResource, String fileName) throws IOException {
        InputStream inputHaas = getResources().openRawResource(idResource);
        OutputStream out = new FileOutputStream(fileName);
        copy(inputHaas, out);
        out.close();
        inputHaas.close();
    }
    public static Bitmap decodeSampledBitmapFromResource(String photo, int reqWidth, int reqHeight, Options options, boolean orientFlag) {

        // First decode with inJustDecodeBounds=true to check dimensions
        // final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap res = BitmapFactory.decodeFile(photo, options);
        if (orientFlag && res != null) {
            
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photo);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int orient = getOrient(orientation);
            
            Matrix matrix = new Matrix();
            matrix.postRotate(orient * 90);
            res = Bitmap.createBitmap(res, 0, 0, res.getWidth(),
                    res.getHeight(), matrix, true);
        }
        return res;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
    }
    
    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand22");
        super.onStartCommand(intent, flags, startId);
        Logger1.log("onStartCommand22");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate22");
        Logger1.log("onCreate22");
        super.onCreate();
        instance = this;
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    public static FaceFinderService getInstance() {
        return instance;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "onStart22");
        Logger1.log("onStart22");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy22");
        Logger1.log("onDestroy22");
        instance = null;
        if (!buttonStart) {
            Intent intent22 = new Intent(PeopleFragment.UPDATE_FACES);
            intent22.putExtra("ended", true);
            broadcastManager.sendBroadcast(intent22);
        }
        // Запустить сервис снова, если стоит start
        if (lastMessage!= null) {
        }
        if (buttonStart) {
            Intent intent = new Intent(getApplicationContext(), FaceFinderService.class);
            getApplicationContext().startService(intent);
        }
        super.onDestroy();
    }

    public static int getOrient(int orientation) {
        int orient = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            orient = 1;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            orient = 2;
        } if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            orient = 3;
        }
        return orient;
    }
    /**
     * ���� �� wifi ����������
     * @return
     */
    public boolean isWifiOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo != null && networkInfo.isConnected());
    }
    public static boolean isAndroidEmulator() {
        String model = Build.MODEL;
        //Log.d(TAG, "model=" + model);
        String product = Build.PRODUCT;
        //Log.d(TAG, "product=" + product);
        boolean isEmulator = false;
        if (product != null) {
            isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
        }
        //Log.d(TAG, "isEmulator=" + isEmulator);
        return isEmulator;
    }

}
