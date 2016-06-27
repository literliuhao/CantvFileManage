package com.cantv.media.center.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public class GridFocusView extends AnimateView {
	private static final String TAG = "GridFocusView"; 
	private Drawable mFocusFrameDrawable;
	private int mlastleft;
	private int mlasttop;
	private int mcurleft;
	private int mcurtop;
	private int mwidth;
	private int mheight;

	public GridFocusView(Context context) {
		super(context);
		forceHide();
	}

	public void setFocusFrameImageResourceAndMeasure(int resId) {
		mFocusFrameDrawable = getResources().getDrawable(resId);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int l = 0;
		int t = 0;
		int r = 0;
		int b = 0;
		float progress = getAnimateRate();
		int toX = Math.round(progress * (mcurleft - mlastleft) + mlastleft);
		int toY = Math.round(progress * (mcurtop - mlasttop) + mlasttop);
		l = toX;
		t = toY;
		r = toX + mwidth;
		b = toY + mheight;
		mFocusFrameDrawable.setBounds(l, t, r, b);
//		mFocusFrameDrawable.setAlpha(Math.round(progress * 255));
		mFocusFrameDrawable.draw(canvas);
		if (needReDraw()) {
			invalidate();
		}
	}

	public void move(int lastleft, int lasttop, int curleft, int curtop, int width, int height) {
		if (View.VISIBLE != getVisibility())
			return;
		mlastleft = lastleft;
		mlasttop = lasttop;
		mcurleft = curleft;
		mcurtop = curtop;
		mwidth = width;
		mheight = height;
		forceHide();
		if (lastleft != curleft || lasttop != curtop) {
			post(new Runnable() {
				@Override
				public void run() {
					toShow();
				}
			});
		}
	}

}
