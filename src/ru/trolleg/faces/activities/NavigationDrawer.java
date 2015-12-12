package ru.trolleg.faces.activities;

import ru.trolleg.faces.DictionaryOpenHelper;
import ru.trolleg.faces.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class NavigationDrawer extends FragmentActivity implements ActionBar.TabListener {
    AppSectionsPagerAdapter mAppSectionsPagerAdapter; 
    ViewPager mViewPager;
    DictionaryOpenHelper dbHelper;
    static RecognizeFragment rf;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        dbHelper = new DictionaryOpenHelper(this);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_color)));
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            View v = new View(this);
            v.setBackgroundColor(getResources().getColor(R.color.action_button_color));
            Tab tab = actionBar.newTab();
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        } 
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
                    return new PhotoGridFragment();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("ND", "w");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_start:
            Fragment ft = rf;
            if (ft != null && ft instanceof RecognizeFragment) {
                ((RecognizeFragment)ft).buttonStart(this);
            }
            return true;
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
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

}
