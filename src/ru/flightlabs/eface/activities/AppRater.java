package ru.flightlabs.eface.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;

import ru.flightlabs.eface.R;

public class AppRater {

    private static final String DONT_SHOW_AGAIN = "dontshowagain";
    private static final String LAUNCH_COUNTER = "launch_count";
    private static final String DATE_FIRST_LAUNCH = "date_firstlaunch";

    private final static int DAYS_UNTIL_PROMPT = 7;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void appLaunched(Activity act, Context mContext) {
        appLaunched(act, mContext, false);
    }

    public static void appLaunched(Activity act, Context mContext, boolean forcible) {
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
                showRateDialog(act, mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(Activity act, final Context context, final SharedPreferences.Editor editor) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View customView = act.getLayoutInflater().inflate(R.layout.custom_dialog_rater, null);
        builder.setView(customView);
        builder.setTitle(R.string.app_rater_name_dialog);
        builder.setMessage(R.string.app_rater_message);

        final AlertDialog alertDialog = builder.create();
        customView.findViewById(R.id.rate_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                        + context.getPackageName()));
                context.startActivity(intent);
                if (editor != null) {
                    editor.putBoolean(DONT_SHOW_AGAIN, true);
                    editor.commit();
                }
            }
        });
        customView.findViewById(R.id.rate_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                clearSharedPreferences(context, editor);
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                clearSharedPreferences(context, editor);
            }
        });
        alertDialog.show();
    }

    private static void clearSharedPreferences(Context context, final SharedPreferences.Editor editor) {
        editor.remove(LAUNCH_COUNTER);
        editor.remove(DATE_FIRST_LAUNCH);
        editor.commit();
    }

}
