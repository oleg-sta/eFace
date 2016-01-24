package ru.trolleg.faces.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import ru.trolleg.faces.Log;
import ru.trolleg.faces.R;

/**
 * Created by sov on 24.01.2016.
 */
public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.about);
        final Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void rateUs(View view) {
        Log.i("SettingsActivity", "rateUs");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                + getPackageName()));
        startActivity(intent);
    }

    public void mailTo(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",getString(R.string.mail_to), null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "eFace");
        startActivity(Intent.createChooser(intent, getString(R.string.send_mail_with)));
    }
}
