package com.cantv.media.center.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.app.core.utils.UiUtils;
import com.cantv.media.center.ui.MediaControllerBar;

public abstract class MediaPlayerActivity extends Activity {
	private int mDefaultPlayIndex;
	private List<String> mDataList;
	private boolean mOnKeyPressedContinuity = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String url = "";
		try {
			url = Uri.decode(getIntent().getDataString()).substring(7);
		} catch (Exception e) {
		}
		mDataList = getIntent().getStringArrayListExtra("data_list");
		if (!TextUtils.isEmpty(url)) {
			if (mDataList == null) {
				mDataList = new ArrayList<String>();
			}
			mDataList.clear();
			mDataList.add(url);
		}
		mDefaultPlayIndex = getIntent().getIntExtra("data_index", 0);
		if (mDataList == null) {
			mDefaultPlayIndex = 0;
			mDataList = new ArrayList<String>();
		}
		UiUtils.doHideSystemBar(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mOnKeyPressedContinuity || event.getRepeatCount() > 3) {
			mOnKeyPressedContinuity = true;
			return onKeyPressedContinuity(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mOnKeyPressedContinuity) {
			mOnKeyPressedContinuity = false;
			return onKeyPressedContinuity(keyCode, event);
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void finish() {
		super.finish();
		MediaControllerBar bar = getMediaControllerBar();
		if (bar != null) {
			bar.runWhenActivityfinish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MediaControllerBar bar = getMediaControllerBar();
		if (bar != null) {
			bar.runWhenActivityResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing() == false) {
			MediaControllerBar bar = getMediaControllerBar();
			if (bar != null) {
				bar.runWhenActivityPause();
			}
		}
	}

	protected List<String> getData() {
		return mDataList;
	}

	protected int indexOfDefaultPlay() {
		return mDefaultPlayIndex;
	}

	protected boolean onKeyPressedContinuity(int keyCode, KeyEvent event) {
		return false;
	}

	protected abstract MediaControllerBar getMediaControllerBar();

}
