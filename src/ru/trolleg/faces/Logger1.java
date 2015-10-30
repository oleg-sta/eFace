package ru.trolleg.faces;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger1 {
    public final static DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    static OutputStream out;

    public static void log(String message) {
        try {
            if (out == null) {
                String path = "/storage/sdcard/logs.txt";
                if (!FaceFinderService.isAndroidEmulator()) {
                    path = "/storage/extSdCard/logs.txt";
                }
                out = new FileOutputStream(path, true);
            }
            out.write((format.format(new Date()) + " " + message + "\r\n").getBytes());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   
}
