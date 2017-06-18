package com.night3210.datasource.recyclerview;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class RefreshLayout extends SwipeRefreshLayout {

    private final int mTouchSlop;
    private RecyclerView mListView;
    private OnLoadListener mOnLoadListener;

    private float firstTouchY;
    private float firstTouchX;
    private int mTotalDragDistanceImp;

    private boolean isLoading = false;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mTotalDragDistanceImp = (int)(64 * metrics.density);
    }

    //set the child view of RefreshLayout,ListView
    public void setChildView(RecyclerView mListView) {
        this.mListView = mListView;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                firstTouchY = event.getRawY();
                firstTouchX = event.getRawX();
                break;

            case MotionEvent.ACTION_UP: {
                float lastTouchY = event.getRawY();
                float lastTouchX = event.getRawX();
                float diffX = Math.abs(firstTouchX - lastTouchX);
                // We expect user to move from bottom to top,
                // So last Y will be smaller that first
                float diffY = firstTouchY - lastTouchY;

                boolean yMoveMore = diffY > diffX;
                boolean yMovementMeetsThreshold = diffY >= mTotalDragDistanceImp;

                if (canLoadMore() && yMoveMore && yMovementMeetsThreshold) {
                    loadData();
                }
            }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean canLoadMore() {
        return isBottom() && !isLoading;
    }

    private boolean isBottom() {
        if (mListView.getAdapter().getItemCount() > 0) {
            return !mListView.canScrollVertically(1);
        }
        return false;
    }

    private void loadData() {
        if (mOnLoadListener != null) {
            setLoading(true);
        }
    }
    public void setLoading(boolean loading) {
        if (mListView == null) return;
        isLoading = loading;
        if (loading) {
            if (isRefreshing()) {
                setRefreshing(false);
            }
            //mListView.setSelection(mListView.getAdapter().getItemCount() - 1);
            mOnLoadListener.onLoad();
        } else {
            firstTouchY = 0;
        }
    }

    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    public interface OnLoadListener {
        public void onLoad();
    }

    @Override
    public void setDistanceToTriggerSync(int distance) {
        super.setDistanceToTriggerSync(distance);
        mTotalDragDistanceImp = distance;
    }
}