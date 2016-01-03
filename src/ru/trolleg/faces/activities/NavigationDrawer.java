package ru.trolleg.faces.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import ru.trolleg.faces.DataHolder;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import ru.trolleg.materialtabs.MaterialTab;
import ru.trolleg.materialtabs.MaterialTabHost;
import ru.trolleg.materialtabs.MaterialTabListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    
    public void onCreate(Bundle savedInstanceState) {
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
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
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
        AppRater.appLaunched(this);
    }
    
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new PeopleFragment();
                case 1:
                    return new RecognizeFragment();
                default:
                    return new AlbumGridFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
            case 0:
                return "ЛЮДИ";
            case 1:
                return "РАСПОЗНАВАНИЕ";
            default:
                return "ГАЛЕРЕЯ";    
            }
        }
    }
    
    
    @Override
    public void onBackPressed() {
        Log.i("NavigationDrawer", "onBackPressed()");
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("ND", "w");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void resetCache() {
        DataHolder.mMemoryCache.evictAll();
        File file = getApplication().getFilesDir();
        String[] children = file.list();
        for (int i = 0; i < children.length; i++)
        {
           new File(file, children[i]).delete();
        }
        
    }

    /**
     * только для разработки
     */
    private void copyDb() {
        Log.i("NavigationDrawer", "copyDb...");
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
