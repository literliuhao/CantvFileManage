package com.cantv.media.center.ui;

import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class DeviceLoginDialog extends Dialog implements OnFocusChangeListener {

	private OnLoginListener listener;
	private View contentView;
	private EditText mUserNameEt;
	private EditText mPasswordEt;
	private CheckBox mShowPwdCb;
	private FocusUtils mFocusUtils;
	private Button mConfirmBtn;
	private Button mCancelBtn;
	private boolean isFirst = true;

	public DeviceLoginDialog(Context context) {
		super(context, R.style.dialog_device_share);
		mFocusUtils = new FocusUtils(context, getWindow().getDecorView(), R.drawable.focus_full_content);
		setupLayout(context, R.layout.dialog_device_login);
	}

	public interface OnLoginListener {
		public void onLogin(String userName, String password);
	}

	private void setupLayout(Context context, int layoutResId) {
		contentView = View.inflate(context, layoutResId, null);
		setContentView(contentView,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mUserNameEt = (EditText) contentView.findViewById(R.id.et_username);
		mPasswordEt = (EditText) contentView.findViewById(R.id.et_pwd);
		mShowPwdCb = (CheckBox) contentView.findViewById(R.id.cb_show_pwd);
		mConfirmBtn = (Button) contentView.findViewById(R.id.btn_confirm);
		mCancelBtn = (Button) contentView.findViewById(R.id.btn_cancel);

		mUserNameEt.setOnFocusChangeListener(this);
		mPasswordEt.setOnFocusChangeListener(this);
		mShowPwdCb.setOnFocusChangeListener(this);
		mConfirmBtn.setOnFocusChangeListener(this);
		mCancelBtn.setOnFocusChangeListener(this);

		mShowPwdCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mPasswordEt
							.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					mPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
			}
		});
		mConfirmBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onLogin(mUserNameEt.getText().toString(), mPasswordEt.getText().toString());
				}
			}

		});
		mCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DeviceLoginDialog.this.dismiss();
			}
		});
	}

	public void updateBackground(Drawable drawable) {
		contentView.setBackground(drawable);
	}

	public void reset() {
		mUserNameEt.setText("");
		mPasswordEt.setText("");
		mUserNameEt.requestFocus();
		mFocusUtils.setFocusLayout(mUserNameEt, false, 0);
		if (isFirst) {
			isFirst = false;
			getWindow().getDecorView().postDelayed(new Runnable() {

				@Override
				public void run() {
					mFocusUtils.startMoveFocus(mUserNameEt, false, 0);
				}
			}, 500);
		}
	}

	public void setOnLoginListener(OnLoginListener listener) {
		this.listener = listener;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			if (v == mUserNameEt || v == mPasswordEt) {
				mFocusUtils.startMoveFocus(v, false, 0);
			} else if (v == mShowPwdCb) {
				mFocusUtils.startMoveFocus(v, true, 1.4f, 30f, 0);
			} else if (v == mConfirmBtn || v == mCancelBtn) {
				mFocusUtils.startMoveFocus(v, true, 0.91f, 0, -6f);
			}
		}
	}
	
	public void refreshData(String userName, String password){
		if(!TextUtils.isEmpty(userName)){
			mUserNameEt.setText(userName);
		}
		if(!TextUtils.isEmpty(password)){
			mPasswordEt.setText(password);
		}
	}

}
