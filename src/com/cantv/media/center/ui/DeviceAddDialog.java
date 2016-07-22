package com.cantv.media.center.ui;

import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class DeviceAddDialog extends Dialog implements OnFocusChangeListener {

	private OnIpConfirmedListener listener;
	private View contentView;
	private EditText mIpEt;
	private FocusUtils mFocusUtils;
	private Button mConfirmBtn;
	private Button mCancelBtn;

	private boolean isFirst = true;

	public DeviceAddDialog(Context context) {
		super(context, R.style.dialog_device_share);
		mFocusUtils = new FocusUtils(context, getWindow().getDecorView(), R.drawable.focus_full_content);
		setupLayout(context, R.layout.dialog_device_add);
	}

	public interface OnIpConfirmedListener {
		public void onConfirmed(String ip);
	}

	private void setupLayout(Context context, int layoutResId) {
		contentView = View.inflate(context, layoutResId, null);
		setContentView(contentView,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mIpEt = (EditText) contentView.findViewById(R.id.et_ip);
		mConfirmBtn = (Button) contentView.findViewById(R.id.btn_confirm);
		mCancelBtn = (Button) contentView.findViewById(R.id.btn_cancel);

		mIpEt.setOnFocusChangeListener(this);
		mConfirmBtn.setOnFocusChangeListener(this);
		mCancelBtn.setOnFocusChangeListener(this);

		mConfirmBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onConfirmed(mIpEt.getText().toString());
				}
			}

		});
		mCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DeviceAddDialog.this.dismiss();
			}
		});
	}

	public void updateBackground(Drawable drawable) {
		contentView.setBackground(drawable);
	}

	public void reset() {
		mIpEt.setText("");
		mFocusUtils.setFocusLayout(mIpEt, false, 0);
		mIpEt.requestFocus();
		if (isFirst) {
			isFirst = false;
			getWindow().getDecorView().postDelayed(new Runnable() {

				@Override
				public void run() {
					mFocusUtils.startMoveFocus(mIpEt, false, 0);
				}
			}, 500);
		}
	}

	public void setOnIpConfirmedListener(OnIpConfirmedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			if (v == mIpEt) {
				mFocusUtils.startMoveFocus(v, false, 0);
			} else if (v == mConfirmBtn || v == mCancelBtn) {
				mFocusUtils.startMoveFocus(v, true, 0.91f, 0, -6f);
			}
		}
	}

}
