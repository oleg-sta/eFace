package ru.trolleg.faces.activities;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;
import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import android.os.Bundle;
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
    static RecognizeFragment rf;
    static Fragment oldFr;
    static Fragment newFr;

    MaterialTabHost tabHost;

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
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
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                tabHost.setSelectedNavigationItem(position);
                // TODO change menu
                // mAppSectionsPagerAdapter.getItem(position).ionResume();
                Object obj = mAppSectionsPagerAdapter.instantiateItem(mViewPager, position);
                if (obj != null && obj instanceof YourFragmentInterface) {
                    ((YourFragmentInterface)obj).fragmentBecameVisible();
                }
                
            }
        });

        //actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_color)));
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            //View v = new View(this);
            //v.setBackgroundColor(getResources().getColor(R.color.action_button_color));
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
                    // The first section of the app is the most interesting -- it offers
                    // a launchpad into the other demonstrations in this example application.
                    return new PeopleFragment();
                case 1:
                    rf = new RecognizeFragment();
                    return rf;
                default:
                    return new FragmentAlbumManager();
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
        default:
            return super.onOptionsItemSelected(item);
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
