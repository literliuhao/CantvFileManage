package com.cantv.media.center.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.cantv.media.R;

public class LoadingDialog extends Dialog implements OnDismissListener, OnShowListener {
    private ImageView mLoadingView;
    private RotateAnimation mRotateAnimation;
    private Activity mActivity;

    public LoadingDialog(Activity context) {
        super(context, R.style.dialog_loading);
        setContentView(R.layout.dialog_loading);
        mActivity = context;
        mLoadingView = (ImageView) findViewById(R.id.iv_loading);
        mRotateAnimation = new RotateAnimation(0, 360, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(900);
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
//        mRotateAnimation.cancel();
        mLoadingView.clearAnimation();
//        mLoadingView = null;
    }

    @Override
    public void cancel() {
        Log.w("cancel", "消失");
        mActivity.finish();
        super.cancel();
    }
}
