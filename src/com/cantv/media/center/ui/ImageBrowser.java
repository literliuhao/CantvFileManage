package com.cantv.media.center.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

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
        mWatchingView.onRotationChanged((int) rotationOffset, true,true);
        setChildLayoutTransform(mWatchingView, layoutT);
    }

    public void changeUpRotation() {
        final LayoutTransform currLayoutT = getChildLayoutTransform(mWatchingView);
        final LayoutTransform layoutT = new LayoutTransform(currLayoutT);
        layoutT.setRotationZ(layoutT.getRotationZ() + 90);
        final float rotationOffset = currLayoutT.getRotationZ() + mWatchingView.getZoomAngle() - layoutT.getRotationZ();
        mWatchingView.onRotationChanged((int) rotationOffset, true,true);
        setChildLayoutTransform(mWatchingView, layoutT);
    }
    
    public void layoutOriginal(){
    	if(isFirst){
    		isFirst =false;
    		mCurrLayoutT = getChildLayoutTransform(mWatchingView);
        	mLayoutT = new LayoutTransform(mCurrLayoutT);
    	}
    }
    
    public void changeReset() {
    	mLayoutT.setRotationZ(mLayoutT.getRotationZ());
        final float rotationOffset = mCurrLayoutT.getRotationZ() + mWatchingView.getZoomAngle() - mLayoutT.getRotationZ();
        mWatchingView.onRotationChanged((int) rotationOffset, true,false);
        setChildLayoutTransform(mWatchingView, mLayoutT);
    }
    
}
