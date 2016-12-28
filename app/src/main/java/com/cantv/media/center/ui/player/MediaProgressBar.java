package com.cantv.media.center.ui.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.app.core.ui.RoundRectDrawable;
import com.app.core.utils.UiUtils;
import com.cantv.media.R;

public class MediaProgressBar extends FrameLayout {
    private final Paint mPaint;
    private final Rect mTempRect;
    private final int mProgressHeight;
    private final int[] mProgressColor;
    private final Drawable mCursorDrawable;
    private final Drawable mSecondProgressDrawable;
    private final TextView mProgressTextView;
    private MediaProgressBarDragListener mListener;

    private long mMaxProgress = 1;
    private long mCurProgress = 0;
    private long mDragProgress = 0;
    private int mDragDrawWidth = 0;
    private boolean mInDragMode = false;

    public interface MediaProgressBarDragListener {
        void onProgressDragged(long current, long max);
    }

    public MediaProgressBar(Context context) {
        this(context, null);
    }

    public MediaProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mTempRect = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mProgressColor = new int[]{Color.parseColor("#40ffffff"), Color.parseColor("#e43f15"), Color.parseColor("#cc000000")};
        mCursorDrawable = getResources().getDrawable(R.drawable.media__progress__cursor);
        mSecondProgressDrawable = getResources().getDrawable(R.drawable.media__progress__second);
        mProgressHeight = mSecondProgressDrawable.getIntrinsicHeight();
        setPadding(0, 0, 0, mCursorDrawable.getIntrinsicHeight() >> 1);
        inflate(getContext(), R.layout.media__progress_bar, this);
        mProgressTextView = (TextView) findViewById(R.id.media__progress__bar__text);
        int raidous = UiUtils.dip2px(context, 5);
        RoundRectDrawable drawable = new RoundRectDrawable(raidous, raidous, mProgressColor[2]);
        mProgressTextView.setBackgroundDrawable(drawable);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mTempRect.set(0, getHeight() - mProgressHeight, getWidth(), getHeight());
        mPaint.setColor(mProgressColor[0]);
        canvas.drawRect(mTempRect, mPaint);
        int progress = mInDragMode ? mDragDrawWidth : Math.round(1.0f * mCurProgress / mMaxProgress * getWidth());
        mTempRect.set(0, getHeight() - mProgressHeight, progress, getHeight());
        mPaint.setColor(mProgressColor[1]);
        canvas.drawRect(mTempRect, mPaint);
        mSecondProgressDrawable.setBounds(progress - mSecondProgressDrawable.getIntrinsicWidth(), getHeight() - mProgressHeight, progress, getHeight());
        mSecondProgressDrawable.draw(canvas);
        if (isFocused()) {
            Path path = new Path();
            path.moveTo(progress, mProgressTextView.getBottom() + 10);
            int sharpWidth = mCursorDrawable.getIntrinsicWidth() / 6;
            int left = Math.max(0, progress - sharpWidth);
            left = (left + sharpWidth) > getWidth() ? getWidth() - 2 * sharpWidth : left;
            path.lineTo(left, mProgressTextView.getBottom());
            path.lineTo(left + 2 * sharpWidth, mProgressTextView.getBottom());
            path.close();
            mPaint.setColor(mProgressColor[2]);
            canvas.drawPath(path, mPaint);

            sharpWidth = mCursorDrawable.getIntrinsicWidth() / 2;
            left = progress - sharpWidth;
            mCursorDrawable.setBounds(left, getHeight() - mCursorDrawable.getIntrinsicHeight() / 2, left + 2 * sharpWidth, getHeight() + mCursorDrawable.getIntrinsicHeight() / 2);
            mCursorDrawable.draw(canvas);
            mDragProgress = Math.round(1.0f * mDragDrawWidth / getWidth() * mMaxProgress);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        int offsetX = Math.max(0, mDragDrawWidth - mProgressTextView.getWidth() / 2);
        offsetX = (offsetX + mProgressTextView.getWidth()) > getWidth() ? getWidth() - mProgressTextView.getWidth() : offsetX;
        canvas.translate(offsetX, 0);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        refreshProgressTextView();
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (onKeyEvent(event)) return true;
        return super.dispatchKeyEvent(event);
    }

    public void setProgress(long current, long max) {
        mCurProgress = current;
        mMaxProgress = Math.max(1, max);
        refreshProgressTextView();
        invalidate();
    }

    public void setMediaProgressBarDragListener(MediaProgressBarDragListener listener) {
        mListener = listener;
    }

    public long getMaxProgress() {
        return mMaxProgress;
    }

    public long getCurrentProgress() {
        return mCurProgress;
    }

    private boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            return onDraging(event.getKeyCode());
        } else if (event.getAction() == KeyEvent.ACTION_UP && mInDragMode) {
            mInDragMode = false;
            if (mListener != null) {
                mListener.onProgressDragged(mDragProgress, mMaxProgress);
                mDragProgress = 0;
            }
            return true;
        }
        return false;
    }

    private boolean onDraging(int keyCode) {
        int direction = 0;
        direction = (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ? -1 : 0;
        direction = (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) ? 1 : direction;
        if (direction == 0) return false;
        if (mInDragMode == false) {
            mInDragMode = true;
            mDragDrawWidth += direction * 3;
        } else {
            mDragDrawWidth += direction * 3;
        }
        mDragDrawWidth = mDragDrawWidth < 0 ? 0 : mDragDrawWidth;
        mDragDrawWidth = mDragDrawWidth > getWidth() ? getWidth() : mDragDrawWidth;
        invalidate();
        refreshProgressTextView();
        return true;
    }

    private void refreshProgressTextView() {
        if (hasFocus() == false) {
            mProgressTextView.setVisibility(View.INVISIBLE);
            return;
        }
        if (mInDragMode == false) {
            mDragDrawWidth = Math.round(1.0f * mCurProgress / mMaxProgress * getWidth());
        }
        mProgressTextView.setVisibility(View.VISIBLE);
        mProgressTextView.setText(currentProgressText());
    }

    private Spanned currentProgressText() {
        long current = mInDragMode ? Math.round(1.0f * mDragDrawWidth / getWidth() * mMaxProgress) : mCurProgress;
        return Html.fromHtml("<p><font color=\"#1998d5\">" + formatTime(current) + "</p>" + " / " + formatTime(mMaxProgress));
    }

    private String formatTime(long time) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long second = seconds % 60;
        String minPrev = minutes < 10 ? "0" : "";
        String secPrev = second < 10 ? "0" : "";
        return minPrev + minutes + ":" + secPrev + second;
    }
}
