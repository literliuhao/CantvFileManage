package com.cantv.media.center.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.cantv.media.R;

public class ThumbnailView extends ImageView {

    private Bitmap mSrc;
    private Bitmap mThumbnailBmp;
    private int mCornerRadius;

    private Paint mPaint;
    private RectF mDrawRectF;

    public ThumbnailView(Context context) {
        this(context, null, 0);
    }

    public ThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ThumbnailView);
        mCornerRadius = ta.getDimensionPixelSize(R.styleable.ThumbnailView_cornerRadius, 5);
        ta.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        mDrawRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int drawWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int drawHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        mDrawRectF.left = drawWidth * .1f;
        mDrawRectF.top = drawHeight * 5f / 17;
        mDrawRectF.right = drawWidth * .9f;
        mDrawRectF.bottom = drawHeight * 14f / 17;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mThumbnailBmp == null) {
            return;
        }
        canvas.drawBitmap(mThumbnailBmp, null, mDrawRectF, mPaint);
    }

    private Bitmap getPreviewBitmap(Bitmap bitmap, int cornerRadius) {
        if (bitmap == null) {
            return null;
        }
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        int squareWidth = 0, squareHeight = 0;
        int x = 0, y = 0;
        // clip src size to 16 : 9
        int val1 = bmpWidth * 9;
        int val2 = bmpHeight * 16;
        if (val1 > val2) {
            // need crop width;
            squareHeight = bmpHeight;
            squareWidth = val2 / 9;
            x = (bmpWidth - squareWidth) / 2;
        } else if (val1 < val2) {
            // need crop height
            squareWidth = bmpWidth;
            squareHeight = val1 / 16;
            y = (bmpHeight - squareHeight) / 2;
        }
        Bitmap output = Bitmap.createBitmap(squareWidth, squareHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(0, 0, squareWidth, squareHeight);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawRoundRect(new RectF(rect), cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, new Rect(x, y, squareWidth, squareHeight), rect, paint);
        return output;
    }

    /**
     * set thumbnail
     *
     * @param bmp
     */
    public void setPreviewBitmap(Bitmap bmp) {
        mSrc = bmp;
        if (mThumbnailBmp != null && !mThumbnailBmp.isRecycled()) {
            mThumbnailBmp.recycle();
            mThumbnailBmp = null;
        }
        mSrc = bmp;
        mThumbnailBmp = getPreviewBitmap(bmp, mCornerRadius);
        invalidate();
    }

    /**
     * set thumbnail Bitmap corner radius
     *
     * @param radiusInPixels
     */
    public void setCornerRadius(int radiusInPixels) {
        if (radiusInPixels == mCornerRadius) {
            return;
        }
        mCornerRadius = radiusInPixels;

        if (mSrc == null) {
            return;
        }
        if (!mThumbnailBmp.isRecycled()) {
            mThumbnailBmp.recycle();
            mThumbnailBmp = null;
        }

        mThumbnailBmp = getPreviewBitmap(mSrc, mCornerRadius);
        invalidate();
    }

    /**
     * clear thumbnail
     */
    public void reset() {
        if (mThumbnailBmp != null && !mThumbnailBmp.isRecycled()) {
            mThumbnailBmp.recycle();
            mThumbnailBmp = null;
        }
        invalidate();
    }
}
