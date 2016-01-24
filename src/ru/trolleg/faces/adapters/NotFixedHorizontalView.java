package ru.trolleg.faces.adapters;

import android.content.Context;
import android.util.AttributeSet;
import ru.trolleg.faces.Log;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class NotFixedHorizontalView extends HorizontalScrollView {
    
    boolean was = false;
    public LinearLayout ll;
    
    public NotFixedHorizontalView(Context context) {
        super(context);
    }

    public NotFixedHorizontalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public NotFixedHorizontalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // TODO Auto-generated method stub
        Log.i("Scrolling", "X from ["+oldl+"] to ["+l+"]" + ((ViewGroup)ll).getChildCount());
        if (l > 119 && !was) {
            Log.i("scroll", "remove ");
            //ll.removeViewAt(0);
            ll.removeViewAt(((ViewGroup)ll).getChildCount() - 1);
            was = true;
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
