package ru.flightlabs.eface.adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import ru.flightlabs.eface.Log;
import android.view.View;

public class MyViewPagerMans extends ViewPager {

    public MyViewPagerMans(Context context) {
        super(context);
    }

    public MyViewPagerMans(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        Log.i("MyViewPagerMans", "canScroll" + v);
        return super.canScroll(v, checkV, dx, x, y);
    }
}
