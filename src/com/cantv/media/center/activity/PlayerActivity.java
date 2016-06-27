package com.cantv.media.center.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.app.core.utils.UiUtils;
import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.center.ui.PlayerControllerBar.CoverFlowViewListener;
import com.cantv.media.center.ui.PlayerControllerBar.PlayerCtrlBarContext;
import com.cantv.media.center.ui.PlayerControllerBar.PlayerCtrlBarListener;
import com.cantv.media.center.utils.MediaUtils;

public abstract class PlayerActivity extends Activity implements PlayerCtrlBarContext, PlayerCtrlBarListener, OnCompletionListener, CoverFlowViewListener {
	private static final String TAG = "PlayerActivity";
	protected int mDefaultPlayIndex;
	protected List<String> mDataList;
	private ProxyPlayer mPlayer;
	protected int mCurPlayIndex;
	private boolean mPaused = false;
	private boolean mFistPlay = true;
	private boolean mInitDone = false;
	protected CoverFlowViewListener mCoverFlowViewListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDataList = getIntent().getStringArrayListExtra("data_list");
		String url = Uri.decode(getIntent().getDataString());
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
	protected void onPause() {
		super.onPause();
		if (mInitDone)
			getProxyPlayer().runOnActivityPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (mInitDone) {
				getProxyPlayer().runOnActivityResume();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void finish() {
		super.finish();
		getProxyPlayer().release();
	}

	@Override
	public String getPlayerTitle() {
		return MediaUtils.getFileName(mDataList.get(mCurPlayIndex));
	}

	@Override
	public int getPlayerDuration() {
		return mPlayer == null ? 0 : getProxyPlayer().getDuration();
	}

	@Override
	public int getPlayerCurPosition() {
		return mPlayer == null ? 0 : getProxyPlayer().getCurrentPosition();
	}

	@Override
	public boolean isPlayerPaused() {
		return mPaused;
	}

	@Override
	public void onPlayNext() {
		mPaused = false;
		mCurPlayIndex++;
		mPlayer.setOnCompletionListener(null);
		playMedia(mCurPlayIndex);
	}

	@Override
	public void onPlayPrev() {
		mPaused = false;
		mCurPlayIndex--;
		mPlayer.setOnCompletionListener(null);
		playMedia(mCurPlayIndex);

	}

	@Override
	public void onPlayRewind() {
		int seekDuraiton = Math.max(getPlayerDuration() / 64, 10000);
		int a = Math.max(0, getPlayerCurPosition() - seekDuraiton);
		onPlaySeekTo(Math.max(0, getPlayerCurPosition() - seekDuraiton));
	}

	@Override
	public void onPlayForwad() {
		int seekDuraiton = Math.max(getPlayerDuration() / 64, 10000);
		int toposition = getPlayerCurPosition() + seekDuraiton;
		onPlaySeekTo(Math.min(getPlayerDuration(), toposition));

	}

	@Override
	public void onPlayerPlayOrPause() {
		mPaused = !mPaused;
		if (mPaused)
			getProxyPlayer().pause();
		else
			getProxyPlayer().start();
	}

	@Override
	public void onPlaySeekTo(int duration) {
		mPaused = true;
		getProxyPlayer().seekTo(duration, null);

	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		onPlayNext();
		if (mCoverFlowViewListener != null) {
			mCoverFlowViewListener.scrollToNext();
		}
	}

	protected ProxyPlayer getProxyPlayer() {
		if (mPlayer == null) {
			mPlayer = new ProxyPlayer();
			mPlayer.setOnCompletionListener(this);
		}
		return mPlayer;
	}

	protected void playDefualt() {
		mInitDone = true;
		playMedia(mDefaultPlayIndex);
	}

	protected void playMedia(int index) {
		if (mPlayer != null) {
			mPlayer.setOnCompletionListener(null);
		}
		try {
			index = (index < 0) ? mDataList.size() - 1 : index;
			index = (index >= mDataList.size()) ? 0 : index;
			mCurPlayIndex = index;
			getProxyPlayer().playMedia(mDataList.get(index), new Runnable() {
				@Override
				public void run() {
					getProxyPlayer().start();
					runAfterPlay(mFistPlay);
					mPlayer.setOnCompletionListener(PlayerActivity.this);
					mFistPlay = false;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void runAfterPlay(boolean isFirst);

}
