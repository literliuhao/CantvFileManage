package com.cantv.media.center.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.cantv.media.R;

public class CDView extends ImageView {

    private Bitmap mCDBmp;
    private Bitmap mDefaultIconBtmp;
    private Bitmap mIconBitmap;

    private Paint mPaint;
    private Rect mCDRect;
    private RectF mIconDrawRect;
    private RectF mDefaultIconRect;
    private int mCenterX;
    private int mCenterY;
    private Path mIconClipPath;
    private int mIconRadius;// 中心圆形icon半径
    private int mIconLoopRadius;// 中心icon上圆环"半径"
    private int mIconLoopColor;// 中心圆环颜色

    private ValueAnimator mRotateAnim;
    private boolean isRotate;
    private boolean isShowDefIcon;
    private float mRotateDegree;// 旋转角度
    private int mPaintAlpha;

    public CDView(Context context) {
        this(context, null);
    }

    public CDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mCDBmp = BitmapFactory.decodeResource(getResources(), R.drawable.musicbj);
        mDefaultIconBtmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_music);
        mIconLoopColor = Color.parseColor("#50FFFFFF");

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mIconLoopColor);

        mCDRect = new Rect();
        mIconDrawRect = new RectF();
        mDefaultIconRect = new RectF();
        mIconClipPath = new Path();

        mRotateAnim = ValueAnimator.ofFloat(0, 360);
        mRotateAnim.setRepeatMode(ObjectAnimator.RESTART);
        mRotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
        mRotateAnim.setInterpolator(new LinearInterpolator());
        mRotateAnim.setDuration(18000);
        mRotateAnim.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotateDegree = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
        int width = resolveSize(viewSize, widthMeasureSpec);
        int height = resolveSize(viewSize, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mIconRadius = (int) Math.ceil(w * 9f / 40);
        int iconLoopWidth = (int) Math.ceil(w * 1f / 40);
        mPaint.setStrokeWidth(iconLoopWidth);
        mIconLoopRadius = mIconRadius - iconLoopWidth / 2;
        // 非通用控件，不考虑padding了
        mCDRect.right = w;
        mCDRect.bottom = h;

        mCenterX = mCDRect.centerX();
        mCenterY = mCDRect.centerY();

        mIconDrawRect.left = mCenterX - mIconRadius;
        mIconDrawRect.top = mCenterY - mIconRadius;
        mIconDrawRect.right = mCenterX + mIconRadius;
        mIconDrawRect.bottom = mCenterY + mIconRadius;

        mDefaultIconRect.left = mIconDrawRect.left + 15;
        mDefaultIconRect.top = mIconDrawRect.top + 15;
        mDefaultIconRect.right = mIconDrawRect.right - 15;
        mDefaultIconRect.bottom = mIconDrawRect.bottom - 15;

        mIconClipPath.reset();
        mIconClipPath.addCircle(mCenterX, mCenterY, mIconRadius, Direction.CW);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (isRotate) {
            canvas.rotate(mRotateDegree, mCenterX, mCenterY);
        }

        // draw icon
        mPaint.setAlpha(mPaintAlpha);
        if (mIconBitmap != null) {
            // draw specified icon
            isShowDefIcon = false;
            canvas.save();
            canvas.clipPath(mIconClipPath);
            canvas.drawBitmap(mIconBitmap, null, mIconDrawRect, mPaint);
            canvas.restore();
        } else {
            // draw default icon
            isShowDefIcon = true;
            canvas.drawBitmap(mDefaultIconBtmp, null, mDefaultIconRect, mPaint);
        }

        // draw CD
        mPaint.setAlpha(255);
        canvas.drawBitmap(mCDBmp, null, mCDRect, mPaint);

        // draw ring
        mPaint.setColor(mIconLoopColor);
        canvas.drawCircle(mCenterX, mCenterY, mIconLoopRadius, mPaint);

        if (mPaintAlpha < 255) {
            if (mRotateAnim.isStarted()) {
                mPaintAlpha += 3;
            } else {
                mPaintAlpha += 12;
                if (mPaintAlpha > 255) {
                    mPaintAlpha = 255;
                }
                postInvalidateDelayed(40);
            }
        }
    }

    public void startRotate() {
        isRotate = true;
        if (mRotateAnim.isPaused()) {
            mRotateAnim.resume();
            return;
        }
        if (mRotateAnim.isStarted()) {
            return;
        }
        mRotateAnim.start();
    }

    public void pauseRotate() {
        isRotate = false;
        mRotateAnim.pause();
    }

    public void stopRotate() {
        isRotate = false;
        mRotateAnim.cancel();
        mRotateAnim.removeAllUpdateListeners();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null && isShowDefIcon) {
            return;
        }
        mIconBitmap = bm;
        mPaintAlpha = 0;
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageResource(int resId) {
        if (resId == 0 && isShowDefIcon) {
            return;
        }
        mIconBitmap = BitmapFactory.decodeResource(getResources(), resId);
        mPaintAlpha = 0;
        super.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable == null && isShowDefIcon) {
            return;
        }
        if (drawable instanceof BitmapDrawable) {
            mIconBitmap = ((BitmapDrawable) drawable).getBitmap();
            mPaintAlpha = 0;
        }
        super.setImageDrawable(drawable);
    }
}

