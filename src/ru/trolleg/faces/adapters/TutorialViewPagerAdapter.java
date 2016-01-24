package ru.trolleg.faces.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.trolleg.faces.activities.TutorialFragment;

/**
 * Created by sov on 24.01.2016.
 */
public class TutorialViewPagerAdapter  extends FragmentStatePagerAdapter {

    public TutorialViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position) {
        return TutorialFragment.newInstance(position);
    }

    public int getCount() {
        return 6;
    }


}