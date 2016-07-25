package com.cantv.media.center.ui;

import com.cantv.media.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class LoadingDialog extends Dialog implements OnDismissListener, OnShowListener {

	private ImageView mLoadingView;
	private RotateAnimation mRotateAnimation;
	
	public LoadingDialog(Context context) {
		super(context, R.style.dialog_loading);
		setContentView(R.layout.dialog_loading);
		mLoadingView = (ImageView) findViewById(R.id.iv_loading);
		mRotateAnimation = new RotateAnimation(0, -360, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mRotateAnimation.setInterpolator(new LinearInterpolator());
		mRotateAnimation.setDuration(600);
		mRotateAnimation.setFillAfter(true);
		mRotateAnimation.setRepeatCount(Animation.INFINITE);
		setOnShowListener(this);
		setOnDismissListener(this);
	}

	@Override
	public void onShow(DialogInterface dialog) {
		mLoadingView.startAnimation(mRotateAnimation);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mLoadingView.clearAnimation();
	}

}
