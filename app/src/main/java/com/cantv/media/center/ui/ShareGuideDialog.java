package com.cantv.media.center.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.utils.SharedPreferenceUtil;

public class ShareGuideDialog extends Dialog implements OnFocusChangeListener {

	private OnClickableListener listener;
	private View contentView;
	private LinearLayout mDisclaimerText;
	private FocusUtils mFocusUtils;
	private Button mConfirmBtn;
	private Button mCancelBtn;

	private boolean isFirst = true;

	public ShareGuideDialog(Context context) {
		super(context, R.style.dialog_device_share);
		mFocusUtils = new FocusUtils(context, getWindow().getDecorView(), R.drawable.focus_full_content);
		setupLayout(context, R.layout.dialog_shareguide);
	}

	public interface OnClickableListener {
		public void onConfirmClickable();
		public void onCancelClickable();
	}

	private void setupLayout(Context context, int layoutResId) {
		contentView = View.inflate(context, layoutResId, null);
		setContentView(contentView,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mDisclaimerText = (LinearLayout) contentView.findViewById(R.id.et_text);
		mConfirmBtn = (Button) contentView.findViewById(R.id.btn_confirm);
		mCancelBtn = (Button) contentView.findViewById(R.id.btn_cancel);
		
		mConfirmBtn.setOnFocusChangeListener(this);
		mCancelBtn.setOnFocusChangeListener(this);
		
	/*	mFocusUtils.startMoveFocus(mConfirmBtn, true, 0.91f);
		mConfirmBtn.requestFocus();*/
		
		mConfirmBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onConfirmClickable();
				}
				SharedPreferenceUtil.setShareGuide(0);
				ShareGuideDialog.this.dismiss();
			}

		});
		mCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onCancelClickable();
				}
				SharedPreferenceUtil.setShareGuide(1);
				ShareGuideDialog.this.dismiss();
			}
		});
	}

	public void updateBackground(Drawable drawable) {
		contentView.setBackground(drawable);
	}

	public void reset() {
		mFocusUtils.startMoveFocus(mConfirmBtn, null, true, 0.96f, 0.80f, 0f, 0f);
		mConfirmBtn.requestFocus();
		  if (isFirst) {
			isFirst = false;
			getWindow().getDecorView().postDelayed(new Runnable() {

				@Override
				public void run() {
					mFocusUtils.startMoveFocus(mConfirmBtn, null, true, 0.96f, 0.80f, 0f, 0f);
				}
			}, 500);
		 }
	}

	  public void setOnClickableListener(OnClickableListener listener) {
		this.listener = listener;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			if (v == mConfirmBtn) {
				mFocusUtils.startMoveFocus(v, null, true, 0.96f, 0.80f, 0f, 0f);
			} else if (v == mCancelBtn) {
				mFocusUtils.startMoveFocus(v, null, true, 0.96f, 0.80f, 0f, 0f);
			}
		}
	}

}
