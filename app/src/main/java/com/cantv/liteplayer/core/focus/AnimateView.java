package com.cantv.liteplayer.core.focus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

public class AnimateView extends View {
    private final String TAG = "smy_AnimateFrameView";
    private float mAlpha = 1.0f;
    private boolean mToShow = true;
    private AlphaAnimation mAnimation;
    private Transformation mDrawingTransform = new Transformation();
    private final int ANIM_DURATION_SHORT = 200;
    private long mDuration = ANIM_DURATION_SHORT;

    // ### 构造函数 ###
    public AnimateView(Context context) {
        this(context, null);
    }

    public AnimateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    public void toShow() {
        if (mToShow == false) {
            mToShow = true;
            mAnimation = new AlphaAnimation(mAlpha, 1);
            if (mDuration != 0) {
                mAnimation.setDuration(Math.round((1 - mAlpha) * mDuration));
            } else {
                mAnimation.setDuration(Math.round((1 - mAlpha) * ANIM_DURATION_SHORT));
            }
            invalidate();
        }
    }

    public void setFocus() {
        if (mToShow == false) {
            mToShow = true;
            mAnimation = new AlphaAnimation(mAlpha, 1);
            mAnimation.setDuration(0);
            invalidate();
        }
    }

    public void toHide() {
        if (mToShow == true) {
            mToShow = false;
            mAnimation = new AlphaAnimation(mAlpha, 0);
            mAnimation.setDuration(ANIM_DURATION_SHORT);
            invalidate();
        }
    }

    public void forceHide() {
        mAlpha = 0;
        mToShow = false;
        mAnimation = null;
        invalidate();
    }

    public void forceShow() {
        mAlpha = 1;
        mToShow = true;
        mAnimation = null;
        invalidate();
    }

    public float getAnimateRate() {
        return mAlpha;
    }

    public boolean needReDraw() {
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (mAnimation != null && mAnimation.hasEnded() == false) {
            if (mAnimation.hasStarted() == false) mAnimation.setStartTime(currentTime);
            mAnimation.getTransformation(currentTime, mDrawingTransform);
            mAlpha = mDrawingTransform.getAlpha();
            return true;
        }
        return false;
    }

    public void setDuration(long durationMillis) {
        mDuration = durationMillis; // change to millis
    }
}
