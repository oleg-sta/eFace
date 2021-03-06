package ru.flightlabs.eface.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import ru.flightlabs.eface.DataHolder;
import ru.flightlabs.eface.DictionaryOpenHelper;
import ru.flightlabs.eface.R;
import ru.flightlabs.materialtabs.MaterialTab;
import ru.flightlabs.materialtabs.MaterialTabHost;
import ru.flightlabs.materialtabs.MaterialTabListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import ru.flightlabs.eface.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

public class NavigationDrawer extends AppCompatActivity  implements MaterialTabListener {
    AppSectionsPagerAdapter mAppSectionsPagerAdapter; 
    ViewPager mViewPager;
    DictionaryOpenHelper dbHelper;
    MaterialTabHost tabHost;
    static RecognizeFragment fr;
    public static final boolean DEVELOPER_MODE = true;
    
    public void onCreate(Bundle savedInstanceState) {
        if (DEVELOPER_MODE && false) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
                    .detectNetwork().penaltyLog().build());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        Log.i("NavigationDrawer", "width " + getResources().getDisplayMetrics().widthPixels);
        Log.i("NavigationDrawer", "height " + getResources().getDisplayMetrics().heightPixels);
        
        final Toolbar toolbar = (android.support.v7.widget.Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        dbHelper = new DictionaryOpenHelper(this);
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), getResources().getStringArray(R.array.tab_array));
        tabHost = (MaterialTabHost) this.findViewById(R.id.tabHost);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabHost.setSelectedNavigationItem(position);
                Object obj = mAppSectionsPagerAdapter.instantiateItem(mViewPager, position);
            }
        });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            tabHost.addTab(
                    tabHost.newTab().setText(mAppSectionsPagerAdapter.getPageTitle(i)).setTabListener(this)
                            );
        }

        // первый старт и показ туториала
        SharedPreferences prefs = getSharedPreferences("first_start", Context.MODE_PRIVATE);
        if (prefs.getBoolean("first", true)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("first", false);
            editor.commit();
            startActivity(new Intent(this, TutorialActivity.class));
        }

        AppRater.appLaunched(this, this);
    }
    
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        final String[] tabNames;
        public AppSectionsPagerAdapter(FragmentManager fm, String[] tabnames)
        {
            super(fm);
            tabNames = tabnames;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new PeopleFragment();
                case 1:
                    fr = new RecognizeFragment();
                    return fr;
                default:
                    return new AlbumGridFragment();
            }
        }

        @Override
        public int getCount() {
            return tabNames.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabNames[position];
        }
    }
    
    
    @Override
    public void onBackPressed() {
        Log.i("NavigationDrawer", "onBackPressed");
        // TODO дать делать категорически нельзя
        Log.i("NavigationDrawer", "onBackPressed " + fr + " " + mViewPager.getCurrentItem());
        if (mViewPager.getCurrentItem() == 1) {
            Log.i("NavigationDrawer", "onBackPressed");
            if (fr.backPress()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent searchIntent = new Intent(this, SettingsActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.tutorial:
                startActivity(new Intent(this, TutorialActivity.class));
                return true;
            case R.id.reset:
                dbHelper.recreate();
                return true;
            case R.id.reset_people:
                dbHelper.facesToNullPeople();
                return true;
            case R.id.copy_db:
                copyDb();
                return true;
            case R.id.reser_cache:
                resetCache();
                return true;
            case R.id.debug_mode:
                DataHolder.debugMode = !DataHolder.debugMode;
                return true;
            case R.id.rate_dialog:
                AppRater.appLaunched(this, this, true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetCache() {
        DataHolder.getInstance().mMemoryCache.evictAll();
        File file = getApplication().getFilesDir();
        String[] children = file.list();
        for (int i = 0; i < children.length; i++)
        {
           new File(file, children[i]).delete();
        }
        file = getApplication().getCacheDir();
        children = file.list();
        for (int i = 0; i < children.length; i++)
        {
           new File(file, children[i]).delete();
        }
        
    }

    /**
     * только для разработки
     */
    private void copyDb() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

                String currentDBPath = "/data/data/" + getPackageName() + "/databases/faces.db";
                //String backupDBPath = "/storage/emulated/0/Download/backupname.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File("/storage/emulated/0/download/faces_back.db.txt");

                if (currentDB.exists()) {
                    Log.i("NavigationDrawer", "copyDb");
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.i("NavigationDrawer", "copyDbied");
                }
        } catch (Exception e) {
            Log.i("NavigationDrawer", "copyDb some error " + e.getMessage());
        }
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
        
    }

    @Override
    public void onTabReselected(MaterialTab tab) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabUnselected(MaterialTab tab) {
        // TODO Auto-generated method stub
        
    }

}
