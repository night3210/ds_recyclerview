package com.pproduct.datasource.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.night3210.datasource.core.LogUtils;

/**
 * Created by Developer on 2/15/2016.
 */
public class ProgressView extends ViewGroup {
    protected ProgressDrawable mProgress;
    protected CircleImageView mCircleView;
    private View mTarget;
    protected int mCurrentTargetOffsetTop = 0;
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private static final int CIRCLE_DIAMETER = 40;
    private static final int CIRCLE_DIAMETER_LARGE = 56;
    private int mCircleViewIndex = -1;
    private int mCircleWidth;

    private int mCircleHeight;
    // Whether the client has set a custom starting position;
    private boolean mUsingCustomStart;
    private boolean mOriginalOffsetCalculated = false;
    protected int mOriginalOffsetTop;
    private Animation mScaleDownAnimation;
    private static final int SCALE_DOWN_DURATION = 250;

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }
    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        setWillNotDraw(false);
        createProgressView();
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        int circleWidth = mCircleView.getMeasuredWidth()-20;
        int circleHeight = mCircleView.getMeasuredHeight()-20;
        mCircleView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }
    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER/2);
        mProgress = new ProgressDrawable(getContext(), this);
        mProgress.updateSizes(ProgressDrawable.DEFAULT);
        mProgress.setBackgroundColor(Color.TRANSPARENT);
        mProgress.setAlpha(255);
        mProgress.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC));
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        addView(mCircleView, lp);
        mCircleView.setImageDrawable(mProgress);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCircleView.measure(widthMeasureSpec, heightMeasureSpec);
    }
    boolean mStarted = false;
    public void start() {
        mCircleView.setMinimumHeight(getHeight());
        mCircleView.setMinimumWidth(getWidth());
        requestLayout();
        mProgress.start();
        mStarted=true;
        mCircleView.setVisibility(VISIBLE);
        requestLayout();
    }
    public void stop() {
        mProgress.stop();
        LogUtils.logi("stopin");
        startScaleDownAnimation(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LogUtils.logi("onanimend");

                mStarted = false;
                mCircleView.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mProgress.setBounds(0, 0, w, h);
    }
    private void startScaleDownAnimation(Animation.AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownAnimation);
    }
    private void setAnimationProgress(float progress) {
        ViewCompat.setScaleX(mCircleView, progress);
        ViewCompat.setScaleY(mCircleView, progress);
    }
}
