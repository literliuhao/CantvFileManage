package com.cantv.media.center.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class FocusFrame extends AnimateView {

    enum State {
        MOVE, STAY
    }

    public enum Direction {
        HORIZONTAL, VERTICAL
    }

    private final String TAG = "new_setting_FocusFrame";

    private Drawable mFocusFrameDrawable;
    private State mState = State.MOVE;
    private int mWidth = 0;
    private int mHeight = 0;

    private int mFromXDelta = 0;
    private int mToXDelta = 0;
    private int mFromYDelta = 0;
    private int mToYDelta = 0;
    private int mLastFromXDelta = 0;
    private int mLastToXDelta = 0;
    private int mLastFromYDelta = 0;
    private int mLastToYDelta = 0;

    private int mStayX = 0;
    private int mStayY = 0;

    private Direction mDirection = Direction.VERTICAL;

    public FocusFrame(Context context) {
        super(context);
        forceHide();
    }

    public void setFocusFrameImageResourceAndMeasure(int resId) {
        mFocusFrameDrawable = getResources().getDrawable(resId);
        BitmapFactory.Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, opts);
        mWidth = opts.outWidth;
        mHeight = opts.outHeight;
        invalidate();
    }

    public void setDirection(Direction d) {
        mDirection = d;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int l = 0;
        int t = 0;
        int r = 0;
        int b = 0;
        float progress = getAnimateRate();
        if (mState == State.MOVE) {
            switch (mDirection) {
                case HORIZONTAL:
                    int toX = Math.round(progress * (mToXDelta - mFromXDelta) + mFromXDelta);
                    l = toX;
                    t = mFromYDelta;
                    r = toX + mWidth;
                    b = mFromYDelta + mHeight;
                    break;
                case VERTICAL:
                default:
                    int toY = Math.round(progress * (mToYDelta - mFromYDelta) + mFromYDelta);
                    l = mFromXDelta;
                    t = toY;
                    r = mFromXDelta + mWidth;
                    b = toY + mHeight;
                    break;
            }
        } else {
            l = mStayX;
            t = mStayY;
            r = mStayX + mWidth;
            b = mStayY + mHeight;
        }
        mFocusFrameDrawable.setBounds(l, t, r, b);
        mFocusFrameDrawable.draw(canvas);
        if (needReDraw()) {
            invalidate();
        }
    }

    public void move(int fromCenterX, int fromCenterY, int toCenterX, int toCenterY) {
        if (mLastFromYDelta == fromCenterY && mLastToYDelta == toCenterY && mLastFromXDelta == fromCenterX && mLastToXDelta == toCenterX) return;
        forceHide();
        mState = State.MOVE;
        mFromXDelta = fromCenterX - mWidth / 2;
        mFromYDelta = fromCenterY - mHeight / 2;
        mToXDelta = toCenterX - mWidth / 2;
        mToYDelta = toCenterY - mHeight / 2;
        if (mFromYDelta != mToYDelta || mFromXDelta != mToXDelta) {
            post(new Runnable() {
                @Override
                public void run() {
                    toShow();
                }
            });
        }
        mLastFromXDelta = fromCenterX;
        mLastToXDelta = toCenterX;
        mLastFromYDelta = fromCenterY;
        mLastToYDelta = toCenterY;
    }

    public void stay(int centerX, int centerY) {
        mStayX = centerX - mWidth / 2;
        mStayY = centerY - mHeight / 2;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

}
