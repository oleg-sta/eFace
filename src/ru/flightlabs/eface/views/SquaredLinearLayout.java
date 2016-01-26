package ru.flightlabs.eface.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquaredLinearLayout extends FrameLayout {

    public SquaredLinearLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public SquaredLinearLayout(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquaredLinearLayout(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
