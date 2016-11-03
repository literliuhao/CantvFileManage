package com.cantv.media.center.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.cantv.media.R;

/**
 * Toast工具类
 */
public class ToastUtils {
	private static Handler mHandler = new Handler(Looper.getMainLooper());
	private static Toast mToast = null;
	// private static TextView mTextView;
	private static Object synObj = new Object();

	/**
	 * Toast发送消息，默认Toast.LENGTH_SHORT
	 * 
	 * @param act
	 * @param msg
	 */
	public static void showMessage(final Context act, final String msg) {
		showMessage(act, msg, Toast.LENGTH_SHORT);
	}

	/**
	 * Toast发送消息，默认Toast.LENGTH_LONG
	 * 
	 * @param act
	 * @param msg
	 */
	public static void showMessageLong(final Context act, final String msg) {
		showMessage(act, msg, Toast.LENGTH_LONG);
	}

	/**
	 * Toast发送消息，默认Toast.LENGTH_LONG
	 * 
	 * @param act
	 */
	public static void showMessageLong(final Context act, int resId) {
		showMessage(act, act.getResources().getString(resId), Toast.LENGTH_LONG);
	}

	/**
	 * Toast发送消息
	 * 
	 * @param act
	 * @param msg
	 * @param len
	 */
	public static void showMessage(final Context act, final String msg, final int len) {
		if (mToast != null) {
			mToast.cancel();
		}
		//适配分辨率
		final int left = (int) act.getResources().getDimension(R.dimen.px50);
		final int up = (int) act.getResources().getDimension(R.dimen.px50);
		final int fontSize = (int) act.getResources().getDimension(R.dimen.px30);

		new Thread(new Runnable() {
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						synchronized (synObj) {
							if (mToast == null) {
								mToast = new Toast(act);
							}
							TextView mTextView = null;
							if (mTextView == null) {
								mTextView = new TextView(act);
								mTextView.setBackgroundResource(R.drawable.shape_toast);
								mTextView.setTextColor(Color.WHITE);
								mTextView.setPadding(left, up, left, up);
								mTextView.setTextSize(fontSize);
							}
							mTextView.setText(msg);
							mToast.setDuration(len);
							mToast.setView(mTextView);
							mToast.show();
						}
					}
				});
			}
		}).start();
	}

	/**
	 * 关闭当前Toast
	 */
	public static void cancelCurrentToast() {
		if (mToast != null) {
			mToast.cancel();
		}
	}
}
