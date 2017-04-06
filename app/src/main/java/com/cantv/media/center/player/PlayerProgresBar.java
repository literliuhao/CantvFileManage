package com.cantv.media.center.player;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.cantv.media.R;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class PlayerProgresBar extends View {

    private Paint paint;
    private FontMetrics fm;
    private ValueAnimator anim;
    // 数据
    private long mDration;// 总进度数
    private String mCurrTime = "00:00:00";// 当前播放时间
    private String mTotalTime = "00:00:00";// 总播放时间
    private long mCurrProgress;// 当前进度时间
    private long mCurrPx;// 当前进度绘制的像素宽度
    private int mProgressColor;// 进度条颜色
    private int mBorderColor;// 边框颜色
    private int mTextColor;// 文字颜色
    private int mTextSize;// 文字大小
    private int mWidth;// 进度条宽
    private int mHeight;// 进度条高

    /**
     * 长按步长
     */
    private int mStepSize;
    /**
     * 默认步长
     */
    private static int DEFAULT_STEP_SIZE = 3000;
    /**
     * 是否是到达最大速度
     */
    private boolean reachMaxG = false;

    public PlayerProgresBar(Context context) {
        this(context, null);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setTextSize(mTextSize);
    }

    public PlayerProgresBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setTextSize(mTextSize);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PlayerProgressBar);
        mProgressColor = attributes.getColor(R.styleable.PlayerProgressBar_barColor, Color.parseColor("#99019dd4"));
        mBorderColor = attributes.getColor(R.styleable.PlayerProgressBar_bordercolor, Color.parseColor("#77ffffff"));
        mTextColor = attributes.getColor(R.styleable.PlayerProgressBar_textcolor, Color.parseColor("#aaffffff"));
        mTextSize = (int) attributes.getDimension(R.styleable.PlayerProgressBar_textsize, 50);
        attributes.recycle();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHeight = (int) (paint.descent() - paint.ascent() + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
    }

    @SuppressLint({"DrawAllocation", "NewApi"})
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
         * 绘制边框
         */
        paint.setStyle(Style.STROKE);// 空心矩形框
        RectF oval = new RectF(0, 0, getWidth(), getHeight());
        paint.setColor(mBorderColor);
        canvas.drawRoundRect(oval, 0, 0, paint);
        /***
         * 绘制进度值
         */
        paint.setStyle(Style.FILL);
        paint.setColor(mProgressColor);
        if (mDration == 0) {
            mCurrPx = 0;
        } else {
            mCurrPx = mCurrProgress * (getWidth()) / mDration;
        }
        oval = new RectF(0, 0, mCurrPx, getHeight());
        canvas.drawRoundRect(oval, 0, 0, paint);
        /***
         * 绘制文本(当前进度)
         */
        fm = paint.getFontMetrics();
        paint.setColor(mTextColor);

        float textCenterVerticalBaselineY = getHeight() / 2 - fm.descent + (fm.descent - fm.ascent) / 2;

        int mProgressBarWidth = getWidth();
        final int currTimeTvWidth = (int) paint.measureText(this.mCurrTime);

        int timeTvX = 10;
        if (mCurrPx > currTimeTvWidth + 20) {
            timeTvX += mCurrPx - currTimeTvWidth - 20;
        }
        canvas.drawText(this.mCurrTime, timeTvX, textCenterVerticalBaselineY, paint);

        /***
         * 绘制文本(总时间)
         */
        final int currTimeTvWidth1 = (int) paint.measureText(this.mTotalTime);
        int Width = mProgressBarWidth - currTimeTvWidth - currTimeTvWidth1;

        if (Width > timeTvX + 20) {
            canvas.drawText(this.mTotalTime, mProgressBarWidth - currTimeTvWidth1 - 10, textCenterVerticalBaselineY, paint);
        }
    }

    public void initProgress() {
        if (anim != null) {
            anim.cancel();
        }
        mCurrProgress = 0;
        mCurrTime = "00:00:00";
        mTotalTime = "00:00:00";
        invalidate();
    }

    /**
     * 设置最大值
     */
    @SuppressLint("SimpleDateFormat")
    public void setDuration(long duration) {
        this.mDration = duration;
        // 转化为最大时间
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        mTotalTime = formatter.format(duration);
        invalidate();
    }

    /**
     * 设置进度值(秒)
     *
     * @param progress
     */
    @SuppressLint("SimpleDateFormat")
    public void setProgress(long progress) {
        if (mDration <= 0 || progress > mDration) {
            return;
        }
        mCurrProgress = progress;
        invalidate();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        mCurrTime = formatter.format(progress);
    }

    public class ProgressEvaluator implements TypeEvaluator<Long> {
        @Override
        public Long evaluate(float fraction, Long startValue, Long endValue) {
            return (long) (startValue + (endValue - startValue) * fraction);
        }
    }

    public void cancelAnim() {
        if (anim != null) {
            anim.cancel();
        }
    }

    public long getCurrProgress() {
        return mCurrProgress;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mDration <= 1000 * 60 * 10) {
            seekPosition(event);
        } else {
            obtainSeekPosition(event);
        }
        return super.onKeyDown(keyCode, event);
    }

    //视频小于10分钟
    private void seekPosition(KeyEvent event) {
        if (KeyEvent.KEYCODE_DPAD_LEFT == event.getKeyCode()) {
            setProgress(mCurrProgress - 3000);
        } else if (KeyEvent.KEYCODE_DPAD_RIGHT == event.getKeyCode()) {
            setProgress(mCurrProgress + 3000);
        }
    }

    // 视频时间大于10分钟
    private void obtainSeekPosition(KeyEvent event) {
        int repeatCount = event.getRepeatCount();
        if (repeatCount == 0) {
            mStepSize = DEFAULT_STEP_SIZE;
            reachMaxG = false;
        }
        double ss1 = Math.sin(repeatCount * 5 * Math.PI / 180);
        if (!reachMaxG) {
            if (ss1 < 0) {
                reachMaxG = true;
            }
            mStepSize += ss1 * 3000;
        }
        if (KeyEvent.KEYCODE_DPAD_LEFT == event.getKeyCode()) {
            mCurrProgress -= mStepSize;
            if (mCurrProgress <= 0) {
                mCurrProgress = 0;
            }
        } else if (KeyEvent.KEYCODE_DPAD_RIGHT == event.getKeyCode()) {
            mCurrProgress += mStepSize;
            if (mCurrProgress >= mDration) {
                mCurrProgress = mDration;
            }
        }
        setProgress(mCurrProgress);
    }

}
