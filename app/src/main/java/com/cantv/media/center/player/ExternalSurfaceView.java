package com.cantv.media.center.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class ExternalSurfaceView extends SurfaceView {
    private ShowType mType;
    //修改视频全屏计算比例
    private float mWidth_height = 1.7778f; // 1920/1080

    public ExternalSurfaceView(Context context) {
        this(context, null);
    }

    public ExternalSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExternalSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mType = ShowType.WIDTH_HEIGHT_ORIGINAL;
    }

    public void setWidthHeightRate(float rate) {
        mWidth_height = rate;
    }

    public void setShowType(ShowType type) {
        mType = type;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int newheight = (mType == ShowType.WIDTH_HEIGHT_4_3) ? Math.round(0.75f * width) : height;
        newheight = (mType == ShowType.WIDTH_HEIGHT_16_9) ? Math.round(0.5625f * width) : newheight;
        newheight = (mType == ShowType.WIDTH_HEIGHT_21_9) ? Math.round(0.4286f * width) : newheight;
        newheight = (mType == ShowType.WIDTH_HEIGHT_ORIGINAL) ? Math.round(1.0f / mWidth_height * width) : newheight;
        if (newheight > height) {
            int newWidth = (mType == ShowType.WIDTH_HEIGHT_4_3) ? Math.round(1.33f * height) : width;
            newWidth = (mType == ShowType.WIDTH_HEIGHT_16_9) ? Math.round(1.778f * height) : newWidth;
            newWidth = (mType == ShowType.WIDTH_HEIGHT_21_9) ? Math.round(2.334f * height) : newWidth;
            newWidth = (mType == ShowType.WIDTH_HEIGHT_ORIGINAL) ? Math.round(mWidth_height * height) : newWidth;
            getHolder().setFixedSize(newWidth, height);
            super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
        } else {
            getHolder().setFixedSize(width, newheight);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(newheight, MeasureSpec.EXACTLY));
        }

        if (null != mChangeScreenListener) {
            mChangeScreenListener.changeAfter();
        }
    }


    public enum ShowType {
        WIDTH_HEIGHT_ORIGINAL, WIDTH_HEIGHT_4_3, WIDTH_HEIGHT_16_9, WIDTH_HEIGHT_21_9, WIDTH_HEIGHT_FULL_SCREEN,
    }

    public interface ChangeScreenListener {

        void changeAfter();
    }

    public ChangeScreenListener mChangeScreenListener;

    public void setChangeScreenListener(ChangeScreenListener changeScreenListener) {
        this.mChangeScreenListener = changeScreenListener;
    }


}
