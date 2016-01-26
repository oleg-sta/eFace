package ru.flightlabs.eface.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquaredImageView extends ImageView {

    public SquaredImageView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public SquaredImageView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SquaredImageView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
