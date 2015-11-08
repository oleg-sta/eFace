package ru.trolleg.faces;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import ru.trolleg.faces.activities.MainActivity;
import ru.trolleg.faces.data.Face;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import detection.Detector;
import detection.Rectangle;

/**
 * ������ ������ ��� �� ����������� � �� �����������. 
 * TODO �������� �� wifi ����������
 * 
 * @author sov
 *
 */
public class FaceFinderService extends IntentService {

    private static final int notif_id=1;
    public static boolean buttonStart = true; 

    static FaceFinderService instance;
    public final static int PHOTOS_LIMIT = 3000; // ������������ ���������� ���������� ��� ���������
	public final static int PHOTOS_SIZE_TO_BE_PROCESSED = 300; // ������ ���� � �������� ��� ���������
	
	ResultReceiver rec = null;
	public Bundle b; // last Bundle
	
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        return new Notification.Builder(this).setContentTitle(title).setContentText(text)
                .setSmallIcon(R.drawable.stat_notify_chat).setContentIntent(contentIntent).getNotification();
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Logger1.log("onHandleIntent");
        Log.d("FaceFinderService", "onHandleIntent " + intent);
        //Bundle bundle = null;
        //ResultReceiver rec = null;
        WakeLock wakeLock = null;
        try {
            PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
            wakeLock.acquire();
            
            Notification note = new Notification(R.drawable.stat_notify_chat, "Обработка фотографий запущена",
                    System.currentTimeMillis());
            Intent i2 = new Intent(this, MainActivity.class);
            i2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i2, 0);
            note.setLatestEventInfo(this, "Обработка фотографий", "Подождите...", pi);
            note.flags |= Notification.FLAG_NO_CLEAR;
            startForeground(notif_id, note);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setContentTitle(getText(R.string.app_name)).setContentText("Обработка начата").setSmallIcon(R.drawable.stat_notify_chat);
            mBuilder.setContentIntent(contentIntent);
            
            
            // ������ ��������� ���������� �������1
            DictionaryOpenHelper dbHelper = new DictionaryOpenHelper(this);
            
            DataHolder dataHolder = DataHolder.getInstance();
            if (dataHolder.processPhotos) {
                Log.d("FaceFinderService", "onHandleIntent is process now");
                return;
            }
            dataHolder.processPhotos = true;
            Runtime info = Runtime.getRuntime();
            int threadsNum = info.availableProcessors();
            if (intent != null) {
                //bundle = intent.getExtras();
                rec = (ResultReceiver) intent.getParcelableExtra("receiver");
            } else {
                // ��� ����������, ������� ������ � true
                buttonStart = true;
            }
            
            Log.d("FaceFinderService", "onHandleIntent threads " + threadsNum);
            Logger1.log("onHandleIntent threads " + threadsNum);
            List<String> photos = MainActivity.getCameraImages(getApplicationContext());
            // �������� ����� � ��
            int newFaces = dbHelper.addNewPhotos(photos);
            
            
            // find faces on photos
            photos = dbHelper.getAllPhotosToBeProcessed();
            if (photos.size() == 0) {
                Log.d("FaceFinderService", "zero photos ");
            	return;
            }
            b = new Bundle();
            b.putString("progress", "0");
            b.putString("message", "Найдено " + photos.size() + " фотографий.");
            if (rec != null) {
                rec.send(0, b);
            }
            if (!buttonStart) {
                Log.d("FaceFinderService", "button stop ");
                return;
            }
            Log.d("FaceFinderService", "loading casade...");
            Logger1.log("loading casade...");
            InputStream inputHaas = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
            Detector detector = Detector.create(inputHaas);
            Log.d("FaceFinderService", "casade loaded");
            Logger1.log("casade loaded");
            inputHaas.close();
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
                    
                    b = new Bundle();
                    b.putString("progress", ((iPh * 100) / photos.size()) + "");
                    b.putString("message", iPh + " из " + photos.size() + " обработано");
                    if (rec != null) {
                        rec.send(0, b);
                    }
                    iPh++;
                    Log.d("FaceFinderService", "photo" + photo);
                    Logger1.log("photo " + photo + " " + iPh + " " + photos.size());

                    BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
                    bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;

                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    Bitmap background_image = decodeSampledBitmapFromResource(photo, PHOTOS_SIZE_TO_BE_PROCESSED,
                            PHOTOS_SIZE_TO_BE_PROCESSED, options);

                    Log.i("FaceFinderService",
                            "size " + background_image.getWidth() + " " + background_image.getHeight());
                    long time = System.currentTimeMillis();
                    List<Rectangle> res = detector.getFaces(background_image, 1.2f, 1.1f, .05f, 2, true, true,
                            threadsNum);
                    time = (System.currentTimeMillis() - time) / 1000;
                    Logger1.log("find in " + time);
                    Log.i("FaceFinderService", "foune " + res.size() + " faces");

                    String imgId = UUID.randomUUID().toString();
                    Face[] faces = new Face[res.size()];
                    if (res.size() == 0) {
                        dbHelper.updatePhoto(photo, imgId, time);
                    }
                    for (int i = 0; i < res.size(); ++i) {
                        if (i == 0) {
                            dbHelper.updatePhoto(photo, imgId, time);
                        }
                        // FaceppResult face = result.get("face").get(i);
                        Rectangle face = res.get(i);
                        // FaceppResult position = face.get("position");
                        Face faceCur = new Face();
                        faces[i] = faceCur;
                        faceCur.height = 100 * face.height / (double) background_image.getHeight();
                        faceCur.width = 100 * face.width / (double) background_image.getWidth();
                        faceCur.centerY = 100 * (face.y + face.height / 2) / (double) background_image.getHeight();
                        faceCur.centerX = 100 * (face.x + face.width / 2) / (double) background_image.getWidth();
                        faceCur.guid = UUID.randomUUID().toString();
                        dbHelper.addFace(faceCur, imgId);
                        //String personGuid = UUID.randomUUID().toString();
                        //dbHelper.addPerson(personGuid);
                        //dbHelper.addFaceToPerson(faceCur.guid, personGuid);
                        // ��������� ����������
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        // �������� ���� ����
                        dataHolder.getLittleFace(db, faceCur.guid, getApplicationContext());
                        db.close();
                    }
                    // ��������� ��� UI � ���������� ���� � ������
                    b = new Bundle();
                    b.putString("photo", photo);
                    if (rec != null) {
                        rec.send(0, b);
                    }
                } catch (Exception e) {
                    Log.d("FaceFinderService", "error" + e.getMessage());
                    Logger1.log("error" + e.getMessage());
                    e.printStackTrace();
                    // �������� ����� ��� ������������
                    // TODO ������ ���� � ������ ������
                    dbHelper.updatePhoto(photo, UUID.randomUUID().toString(), -1);
                }
            }
            mBuilder.setContentText("Обработка завершена");
            mBuilder.setProgress(iPh, iPh, false);
            Notification not = mBuilder.build();
            not.defaults |= Notification.DEFAULT_SOUND;
            mNotifyManager.notify(notif_id, not);
            
            dataHolder.processPhotos = false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("FaceFinderService", "error" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (wakeLock != null) {
                wakeLock.release();
            }
            DataHolder.getInstance().processPhotos = false;

            Bundle b = new Bundle();
            b.putString("progress", "100");
            b.putString("message", "Завершенно");
            if (rec != null) {
                rec.send(0, b);
            }
            // TODO send status message
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String photo, int reqWidth, int reqHeight, Options options) {

        // First decode with inJustDecodeBounds=true to check dimensions
        // final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photo, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(photo, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
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
        Log.i("FaceFinderService", "onStartCommand22");
        super.onStartCommand(intent, flags, startId);
        Logger1.log("onStartCommand22");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i("FaceFinderService", "onCreate22");
        Logger1.log("onCreate22");
        super.onCreate();
        instance = this;
    }

    public static FaceFinderService getInstance() {
        return instance;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        Log.i("FaceFinderService", "onStart22");
        Logger1.log("onStart22");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        // TODO �������� � MainActivity �� ���������
        DataHolder.getInstance().processPhotos = false;
        Log.i("FaceFinderService", "onDestroy22");
        Logger1.log("onDestroy22");
        instance = null;
        super.onDestroy();
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

    public void setReceiver(NotificationReceiver receiver) {
        rec = receiver;
        
    }

}
