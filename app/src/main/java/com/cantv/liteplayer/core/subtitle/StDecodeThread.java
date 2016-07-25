package com.cantv.liteplayer.core.subtitle;

import android.os.Handler;
import android.os.Looper;

public class StDecodeThread extends Thread {
	private Handler mNotify = null;
	private String mSubtitlePath = null;
	private Runnable mRunnable = null;
	private StDecoderListener mCallBack = null;
	private StDecodeResult mResult = null;

	public StDecodeThread(final String path, StDecoderListener listener) {
		mSubtitlePath = path;
		mCallBack = listener;
		mRunnable = new Runnable() {
			@Override
			public void run() {
				if (mCallBack != null)
					mCallBack.onDecoded(mResult);
			}
		};
		mNotify = new Handler(Looper.getMainLooper());
	}

	public void run() {
		StUtil.stopDecodeFlag = false;
		StDecodeResult result = StDecoder.decodeSubtitle(mSubtitlePath);
		mResult = result;
		mNotify.post(mRunnable);
	}

	public void cancel() {
		mCallBack = null;
		mNotify.removeCallbacks(mRunnable);
	}

	public interface StDecoderListener {
		void onDecoded(StDecodeResult result);
	}
}
