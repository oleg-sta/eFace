package ru.flightlabs.eface.adapters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView2 extends AdapterView<ListAdapter> {

    public boolean mAlwaysOverrideTouch = true;
    protected ListAdapter mAdapter;
    private int mLeftViewIndex = -1;
    private int mRightViewIndex = 0;
    protected int mCurrentX;
    protected int mNextX;
    private int mMaxX = Integer.MAX_VALUE;
    private int mDisplayOffset = 0;
    protected Scroller mScroller;
    private GestureDetector mGesture;
    private Queue<View> mRemovedViewQueue = new LinkedList<View>();
    private OnItemSelectedListener mOnItemSelected;
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private boolean mDataChanged = false;
    private boolean mMeasureHeightForVisibleOnly = true;


    public HorizontalListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private synchronized void initView() {
        mLeftViewIndex = -1;
        mRightViewIndex = 0;
        mDisplayOffset = 0;
        mCurrentX = 0;
        mNextX = 0;
        mMaxX = Integer.MAX_VALUE;
        mScroller = new Scroller(getContext());
        mGesture = new GestureDetector(getContext(), mOnGesture);
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelected = listener;
    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        mOnItemClicked = listener;
    }

    @Override
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClicked = listener;
    }

    private DataSetObserver mDataObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            synchronized(HorizontalListView2.this){
                mDataChanged = true;
            }
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            reset();
            invalidate();
            requestLayout();
        }

    };

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View getSelectedView() {
        //TODO: implement
        return null;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if(mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataObserver);
        reset();
    }

    /**
* @param visibleOnly - If set to true, then height is calculated
* only using visible views. If set to false then height is
* calculated using _all_ views in adapter. Default is true.
* Be very careful when passing false, as this may result in
* significant performance hit for larger number of views.
*/
    public void setHeightMeasureMode(boolean visibleOnly) {
        if(mMeasureHeightForVisibleOnly != visibleOnly) {
            mMeasureHeightForVisibleOnly = visibleOnly;
            requestLayout();
        }
    }

    private synchronized void reset(){
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    @Override
    public void setSelection(int position) {
        //TODO: implement
    }

    private void addAndMeasureChild(final View child, int viewPos) {
        LayoutParams params = child.getLayoutParams();
        if(params == null) {
            params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int height = 0;

            if(mMeasureHeightForVisibleOnly) {
                int childCount = getChildCount();
                for(int i = 0; i < childCount; i++) {
                    View v = getChildAt(i);
                    v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    if(v.getMeasuredHeight() > height) {
                        height = v.getMeasuredHeight();
                    }
                }
            } else {

                /* Traverses _all_ views! Bypasses view recycler! */

                HashMap<Integer, View> mRecycler = new HashMap<Integer, View>();
                int childCount = getAdapter().getCount();
                for(int i = 0; i < childCount; i++) {
                    int type = getAdapter().getItemViewType(i);
                    View convertView = mRecycler.get(type);
                    View v = getAdapter().getView(i, convertView, this);
                    mRecycler.put(type, v);

                    v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    if(v.getMeasuredHeight() > height) {
                        height = v.getMeasuredHeight();
                    }
                }
            }

            if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
                if(maxHeight < height) {
                    height = maxHeight;
                }
            }

            setMeasuredDimension(getMeasuredWidth(), height);
        }
    }

    @Override
    protected synchronized void onLayout(boolean changed, int left, int top,
                                                          int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if(mAdapter == null){
            return;
        }

        if(mDataChanged){
            int oldCurrentX = mCurrentX;
            initView();
            removeAllViewsInLayout();
            mNextX = oldCurrentX;
            mDataChanged = false;
        }

        if(mScroller.computeScrollOffset()){
            int scrollx = mScroller.getCurrX();
            mNextX = scrollx;
        }

        if(mNextX <= 0){
            mNextX = 0;
            mScroller.forceFinished(true);
        }
        if(mNextX >= mMaxX) {
            mNextX = mMaxX;
            mScroller.forceFinished(true);
        }

        int dx = mCurrentX - mNextX;

        removeNonVisibleItems(dx);
        fillList(dx);
        positionItems(dx);

        mCurrentX = mNextX;

        if(!mScroller.isFinished()){
            post(new Runnable(){
                public void run() {
                    requestLayout();
                }
            });

        }
    }

    private void fillList(final int dx) {
        int edge = 0;
        View child = getChildAt(getChildCount()-1);
        if(child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);

        edge = 0;
        child = getChildAt(0);
        if(child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);


    }

    private void fillListRight(int rightEdge, final int dx) {
        while(rightEdge + dx < getWidth() && mRightViewIndex < mAdapter.getCount()) {

            View child = mAdapter.getView(mRightViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, -1);
            rightEdge += child.getMeasuredWidth();

            if(mRightViewIndex == mAdapter.getCount()-1) {
                mMaxX = mCurrentX + rightEdge - getWidth();
            }

            if (mMaxX < 0) {
                mMaxX = 0;
            }
            mRightViewIndex++;
        }

    }

    private void fillListLeft(int leftEdge, final int dx) {
        while(leftEdge + dx > 0 && mLeftViewIndex >= 0) {
            View child = mAdapter.getView(mLeftViewIndex, mRemovedViewQueue.poll(), this);
            addAndMeasureChild(child, 0);
            leftEdge -= child.getMeasuredWidth();
            mLeftViewIndex--;
            mDisplayOffset -= child.getMeasuredWidth();
        }
    }

    private void removeNonVisibleItems(final int dx) {
        View child = getChildAt(0);
        while(child != null && child.getRight() + dx <= 0) {
            mDisplayOffset += child.getMeasuredWidth();
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mLeftViewIndex++;
            child = getChildAt(0);

        }

        child = getChildAt(getChildCount()-1);
        while(child != null && child.getLeft() + dx >= getWidth()) {
            mRemovedViewQueue.offer(child);
            removeViewInLayout(child);
            mRightViewIndex--;
            child = getChildAt(getChildCount()-1);
        }
    }

    private void positionItems(final int dx) {
        if(getChildCount() > 0){
            mDisplayOffset += dx;
            int left = mDisplayOffset;
            for(int i=0;i<getChildCount();i++){
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                left += childWidth;
            }
        }
    }

    public synchronized void scrollTo(int x) {
        mScroller.startScroll(mNextX, 0, x - mNextX, 0);
        requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGesture.onTouchEvent(ev);
        return handled;
    }

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
        synchronized(HorizontalListView2.this){
            mScroller.fling(mNextX, 0, (int)-velocityX, 0, 0, mMaxX, 0, 0);
        }
        requestLayout();

        return true;
    }

    protected boolean onDown(MotionEvent e) {
        mScroller.forceFinished(true);
        return true;
    }

    private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return HorizontalListView2.this.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return HorizontalListView2.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {

            synchronized(HorizontalListView2.this){
                mNextX += (int)distanceX;
            }
            requestLayout();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Rect viewRect = new Rect();
            for(int i=0;i<getChildCount();i++){
                View child = getChildAt(i);
                int left = child.getLeft();
                int right = child.getRight();
                int top = child.getTop();
                int bottom = child.getBottom();
                viewRect.set(left, top, right, bottom);
                if(viewRect.contains((int)e.getX(), (int)e.getY())){
                    if(mOnItemClicked != null){
                        mOnItemClicked.onItemClick(HorizontalListView2.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ));
                    }
                    if(mOnItemSelected != null){
                        mOnItemSelected.onItemSelected(HorizontalListView2.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ));
                    }
                    break;
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Rect viewRect = new Rect();
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                int left = child.getLeft();
                int right = child.getRight();
                int top = child.getTop();
                int bottom = child.getBottom();
                viewRect.set(left, top, right, bottom);
                if (viewRect.contains((int) e.getX(), (int) e.getY())) {
                    if (mOnItemLongClicked != null) {
                        mOnItemLongClicked.onItemLongClick(HorizontalListView2.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId(mLeftViewIndex + 1 + i));
                    }
                    break;
                }

            }
        }

    };



}