package ru.flightlabs.eface.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import ru.flightlabs.eface.R;

public class AppRater {

    private static final String DONT_SHOW_AGAIN = "dontshowagain";
    private static final String LAUNCH_COUNTER = "launch_count";
    private static final String DATE_FIRST_LAUNCH = "date_firstlaunch";

    private final static int DAYS_UNTIL_PROMPT = 7;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void appLaunched(Context mContext) {
        appLaunched(mContext, false);
    }

    public static void appLaunched(Context mContext, boolean forcible) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", Context.MODE_PRIVATE);
        if (prefs.getBoolean(DONT_SHOW_AGAIN, false) && !forcible) {
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

        if (launch_count >= LAUNCHES_UNTIL_PROMPT || forcible) {
            if (System.currentTimeMillis() >= dateFirstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) || forcible) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_rater_name_dialog);
        builder.setMessage(R.string.app_rater_message);
        builder.setPositiveButton(R.string.apo_rater_rate_now, new DialogInterface.OnClickListener() {
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
        builder.setNeutralButton(R.string.app_rater_remind_later, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                clearSharedPreferences(context, editor);
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
