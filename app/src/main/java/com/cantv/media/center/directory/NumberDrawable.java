package com.cantv.media.center.directory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.cantv.media.R;

@SuppressLint("ResourceAsColor")
public class NumberDrawable extends Drawable {
    private int mNum = 0;
    private Paint mPaint;
    private Drawable mDrawableBG;

    public NumberDrawable(Context mContext) {
        mPaint = new Paint();
        mPaint.setColor(mContext.getResources().getColor(R.color.txtcolor));
        mPaint.setTextSize(mContext.getResources().getDimension(R.dimen.px35));
        mDrawableBG = mContext.getResources().getDrawable(R.drawable.numbg);
    }

    public void setNum(int num) {
        mNum = num;
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawableBG.getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawableBG.getIntrinsicWidth();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mNum <= 0) return;
        Rect bounds = getBounds();
        mDrawableBG.setBounds(bounds);
        mDrawableBG.draw(canvas);

        canvas.save();
        canvas.translate(bounds.left, bounds.top);
        String text = "" + mNum;
        float textWidth = mPaint.measureText(text);
        int x = bounds.width() / 2 - Math.round(textWidth / 2);
        canvas.drawText(text, x, bounds.height() / 2 - mPaint.ascent() / 2, mPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

}
