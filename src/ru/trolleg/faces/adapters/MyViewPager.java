package ru.trolleg.faces.adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MyViewPager extends ViewPager {

    public MyViewPager(Context context) {
        super(context);
    }


    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        Log.i("MyViewPagerMans", "canScroll2 " + v + " " + getAdapter().getCount());
        if (v != this && v instanceof ViewPager && v instanceof MyViewPagerMans) {
            if (((ViewPager) v).getAdapter().getCount() < FacesGridAdapter.WIDTH_NUM_PICS) {
                return false;
            }
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}
