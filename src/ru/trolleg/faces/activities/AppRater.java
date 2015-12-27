package ru.trolleg.faces.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {

    private static final String DONT_SHOW_AGAIN = "dontshowagain";
    private static final String LAUNCH_COUNTER = "launch_count";
    private static final String DATE_FIRST_LAUNCH = "date_firstlaunch";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void appLaunched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", Context.MODE_PRIVATE);
        if (prefs.getBoolean(DONT_SHOW_AGAIN, false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        long launch_count = prefs.getLong(LAUNCH_COUNTER, 0) + 1;
        editor.putLong(LAUNCH_COUNTER, launch_count);

        Long dateFirstLaunch = prefs.getLong(DATE_FIRST_LAUNCH, 0);
        if (dateFirstLaunch == 0) {
            dateFirstLaunch = System.currentTimeMillis();
            editor.putLong(DATE_FIRST_LAUNCH, dateFirstLaunch);
        }

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= dateFirstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
        String appName = context.getString(context.getApplicationInfo().labelRes);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Оцените приложение");
        builder.setMessage("Если Вам понравилось приложение " + appName
                + ", пожалуйста, найдите время, чтобы оценить его. Спасибо за Вашу поддержку!");
        builder.setPositiveButton("Оценить " + appName, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                        + context.getPackageName()));
                context.startActivity(intent);
                if (editor != null) {
                    editor.putBoolean(DONT_SHOW_AGAIN, true);
                    editor.commit();
                }
            }
        });
        builder.setNeutralButton("Напомнить позже", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                clearSharedPreferences(context, editor);
            }
        });
        builder.setNegativeButton("Нет, спасибо", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (editor != null) {
                    editor.putBoolean(DONT_SHOW_AGAIN, true);
                    editor.commit();
                }
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                clearSharedPreferences(context, editor);
            }
        });
        builder.create().show();
    }

    private static void clearSharedPreferences(Context context, final SharedPreferences.Editor editor) {
        editor.remove(LAUNCH_COUNTER);
        editor.remove(DATE_FIRST_LAUNCH);
        editor.commit();
    }
}
