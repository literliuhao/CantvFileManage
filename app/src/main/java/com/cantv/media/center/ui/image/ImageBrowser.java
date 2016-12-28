package com.cantv.media.center.ui.image;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.app.core.ui.TransformView;

public class ImageBrowser extends TransformView {
    private final ImageWatchingView mWatchingView;
    private boolean isFirst = true;
    private LayoutTransform mCurrLayoutT;
    private LayoutTransform mLayoutT;

    // ### 构造函数 ###
    public ImageBrowser(Context context) {
        this(context, null);
    }

    public ImageBrowser(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageBrowser(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mWatchingView = new ImageWatchingView(context);
        addView(mWatchingView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.FILL));
    }

    public void setContentImageView(View view) {
        mWatchingView.setContentView(view, null);
    }

    public void onZoomIn() {
        mWatchingView.onZoomIn();
    }

    public void onZoomScale(float sclae) {
        mWatchingView.onZoomInScale(sclae);
    }

    public void onZoomOut() {
        mWatchingView.onZoomOut();
    }

    public void reset() {
        mWatchingView.reset();
    }

    public void changeRotation() {
        final LayoutTransform currLayoutT = getChildLayoutTransform(mWatchingView);
        final LayoutTransform layoutT = new LayoutTransform(currLayoutT);
        layoutT.setRotationZ(layoutT.getRotationZ() - 90);
        final float rotationOffset = currLayoutT.getRotationZ() + mWatchingView.getZoomAngle() - layoutT.getRotationZ();
        mWatchingView.onRotationChanged((int) rotationOffset, true, true, new ImageWatchingView.OnRotationListener() {

            @Override
            public void onRotationFinish() {
                // TODO Auto-generated method stub

            }
        });
        setChildLayoutTransform(mWatchingView, layoutT);
    }

    public void changeUpRotation() {
        final LayoutTransform currLayoutT = getChildLayoutTransform(mWatchingView);
        final LayoutTransform layoutT = new LayoutTransform(currLayoutT);
        layoutT.setRotationZ(layoutT.getRotationZ() + 90);
        final float rotationOffset = currLayoutT.getRotationZ() + mWatchingView.getZoomAngle() - layoutT.getRotationZ();
        mWatchingView.onRotationChanged((int) rotationOffset, true, true, new ImageWatchingView.OnRotationListener() {

            @Override
            public void onRotationFinish() {
                // TODO Auto-generated method stub

            }
        });
        setChildLayoutTransform(mWatchingView, layoutT);
    }

    public void layoutOriginal() {
        if (isFirst) {
            isFirst = false;
            mCurrLayoutT = getChildLayoutTransform(mWatchingView);
            mLayoutT = new LayoutTransform(mCurrLayoutT);
        }
    }

    public void changeReset() {
        ObjectAnimator startAlpha = ObjectAnimator.ofFloat(mLayoutT, "alpha", 1f, 0f);

        startAlpha.setDuration(0);
        startAlpha.start();

        mLayoutT.setRotationZ(mLayoutT.getRotationZ());
        final float rotationOffset = mCurrLayoutT.getRotationZ() + mWatchingView.getZoomAngle() - mLayoutT.getRotationZ();
        mWatchingView.onRotationChanged((int) rotationOffset, true, false, new ImageWatchingView.OnRotationListener() {

            @Override
            public void onRotationFinish() {
                // TODO Auto-generated method stub
            }
        });
        setChildLayoutTransform(mWatchingView, mLayoutT);
        ObjectAnimator endAlpha = ObjectAnimator.ofFloat(mLayoutT, "alpha", 0f, 1f);
        endAlpha.setDuration(1);
        endAlpha.start();


    }

}
