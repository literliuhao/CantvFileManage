package com.cantv.media.center.audio;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.cantv.media.R;
import com.cantv.media.center.utils.BitmapUtils;

/**
 * Created by yibh on 2016/12/16.
 * 通过旋转动画会有很明显的噪点
 */

public class CDView extends View {
    private Bitmap mDiscBitmap; //光盘
    private Bitmap mDefaultCoverBitmap; //默认封面
    private Bitmap mCoverBitmap;
    private Point mDiscCenter = new Point();   //光盘中点坐标
    private Point mCoverCenter = new Point(); //封面中点坐标
    private Point mDiscPoint = new Point();
    private Point mCoverPoint = new Point();
    private Matrix mDiscMatrix = new Matrix();
    private Matrix mCoverMatrix = new Matrix();
    private float mRotation = 0f;
    private boolean isPlaying = false;
    private Handler mHandler = new Handler();
    private static final long TIME_UPDATE = 50L;
    private static final float DISC_ROTATION_INCREASE = 0.5f;
    private Paint mPaint;

    public CDView(Context context) {
        this(context, null);
    }

    public CDView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CDView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDiscBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.musicbj);
        mDefaultCoverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_music);
        mCoverBitmap = mDefaultCoverBitmap;
        mPaint = new Paint();
        //通过屏幕取色,取光盘与封面交接处的颜色
        mPaint.setColor(Color.rgb(11, 10, 10));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setAntiAlias(true);
    }

    /**
     * 确定子View的位置
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initViewLayout();
        Log.w("onLayout", "");
    }

    private void initViewLayout() {
        int mdiscW = mDiscBitmap.getWidth();
        if (getWidth() < mdiscW) {  //view宽高小于光盘时
            mDiscBitmap = BitmapUtils.resizeImage(mDiscBitmap, getWidth(), getHeight());
        }

        //0.4575 根据测量光盘是400x400,里面圈是183x183,所以是0.4575
        mCoverBitmap = BitmapUtils.resizeImage(mCoverBitmap, (int) (mDiscBitmap.getWidth() * 0.4575), (int) (mDiscBitmap.getHeight() * 0.4575));
        mCoverPoint.x = (int) ((getWidth() - mDiscBitmap.getWidth() * 0.4575) / 2);
        mCoverPoint.y = (int) ((getHeight() - mDiscBitmap.getHeight() * 0.4575) / 2);
        mDiscPoint.x = (getWidth() - mDiscBitmap.getWidth()) / 2;
        mDiscPoint.y = (getHeight() - mDiscBitmap.getHeight()) / 2;
        mDiscCenter.x = getWidth() / 2;
        mDiscCenter.y = getHeight() / 2;
        mCoverCenter.x = mDiscCenter.x;
        mCoverCenter.y = mDiscCenter.y;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //设置旋转角度
        mDiscMatrix.setRotate(mRotation, mDiscCenter.x, mDiscCenter.y);
        //设置旋转中心
        mDiscMatrix.preTranslate(mDiscPoint.x, mDiscPoint.y);
        canvas.drawBitmap(mDiscBitmap, mDiscMatrix, null);

        mCoverMatrix.setRotate(mRotation, mCoverCenter.x, mCoverCenter.y);
        mCoverMatrix.preTranslate(mCoverPoint.x, mCoverPoint.y);
        canvas.drawBitmap(mCoverBitmap, mCoverMatrix, null);

        //画个圆圈,为了遮盖住封面与光盘交界处的噪点
        canvas.drawCircle(mCoverCenter.x, mCoverCenter.y, mCoverBitmap.getWidth() / 2, mPaint);
    }


    private Runnable mRotationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                mRotation += DISC_ROTATION_INCREASE;
                if (mRotation >= 360) {
                    mRotation = 0;
                }
                invalidate();
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    /**
     * 设置封面
     *
     * @param bitmap
     */
    public void setCoverBitmap(Bitmap bitmap) {
        if (null != bitmap) {
            mCoverBitmap = BitmapUtils.createCircleImage(bitmap);
        } else {
            mCoverBitmap = BitmapUtils.createCircleImage(mDefaultCoverBitmap);
        }
        mCoverBitmap = BitmapUtils.resizeImage(mCoverBitmap, (int) (mDiscBitmap.getWidth() * 0.4575), (int) (mDiscBitmap.getHeight() * 0.4575));
        mRotation = 0f;
        invalidate();
    }

    /**
     * 开始旋转
     */
    public void start() {
        if (!isPlaying) {
            isPlaying = true;
            mHandler.post(mRotationRunnable);
        }
    }

    /**
     * 暂停旋转
     */
    public void pause() {
        if (isPlaying) {
            isPlaying = false;
            mHandler.removeCallbacks(mRotationRunnable);
        }
    }

}
