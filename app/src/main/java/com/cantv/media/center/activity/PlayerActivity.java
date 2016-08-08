package com.cantv.media.center.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.center.constants.PlayMode;
import com.cantv.media.center.ui.PlayerControllerBar.CoverFlowViewListener;
import com.cantv.media.center.ui.PlayerControllerBar.PlayerCtrlBarContext;
import com.cantv.media.center.ui.PlayerControllerBar.PlayerCtrlBarListener;
import com.cantv.media.center.utils.MediaUtils;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public abstract class PlayerActivity extends Activity implements PlayerCtrlBarContext, PlayerCtrlBarListener, OnCompletionListener, CoverFlowViewListener {

    protected int mDefaultPlayIndex;
    protected List<String> mDataList;
    private ProxyPlayer mPlayer;
    protected int mCurPlayIndex;
    private boolean mPaused = false;
    private boolean mFirstPlay = true;
    private boolean mInitDone = false;
    protected CoverFlowViewListener mCoverFlowViewListener;
    protected int mPlayMode = PlayMode.IN_ORDER;// 默认顺序播放

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataList = getIntent().getStringArrayListExtra("data_list");
        if (mDataList == null) {
            mDataList = new ArrayList<String>();
        }

        String url = Uri.decode(getIntent().getDataString());
        if (!TextUtils.isEmpty(url)) {
            mDataList.clear();
            mDataList.add(url);
        }
        mDefaultPlayIndex = getIntent().getIntExtra("data_index", 0);
        if (mDefaultPlayIndex >= mDataList.size()) {
            mDefaultPlayIndex = 0;
        }
        //UiUtils.doHideSystemBar(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mInitDone) getProxyPlayer().runOnActivityPause();
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
        mPlayer.setOnCompletionListener(null);
        playMedia(mCurPlayIndex + 1);
    }

    private void onPlayRandomNext() {
        mPaused = false;
        mPlayer.setOnCompletionListener(null);
        playMedia(new Random().nextInt(mDataList.size()));
    }

    private void onPlayCycle() {
        mPaused = false;
        mPlayer.setOnCompletionListener(null);
        playMedia(mCurPlayIndex);
    }

    @Override
    public void onPlayPrev() {
        mPaused = false;
        mPlayer.setOnCompletionListener(null);
        playMedia(mCurPlayIndex - 1);

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
        if (mPaused) getProxyPlayer().pause();
        else getProxyPlayer().start();
    }

    @Override
    public void onPlaySeekTo(int duration) {
        mPaused = true;
        getProxyPlayer().seekTo(duration, null);
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        // 当播放器设置loop时，播放结束会自动重新开始，此时不会出发onCompletion回调
        if (mPlayMode == PlayMode.RANDOM_ORDER) {
            onPlayRandomNext();
        } else if (mPlayMode == PlayMode.SINGLE_CYCLE) {
            onPlayCycle();
        } else {
            onPlayNext();
        }
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
        if (mDataList.size() == 0) {
            return;
        }
        try {
            index = (index < 0) ? 0 : index;
            if(index >= mDataList.size()){
//            	mPlayer.release();
//            	finish();
//            	return;
				index = index % mDataList.size();
            }
            mCurPlayIndex = index;
            getProxyPlayer().playMedia(mDataList.get(index), new Runnable() {
                @Override
                public void run() {
                    getProxyPlayer().start();
                    runAfterPlay(mFirstPlay);
                    mPlayer.setOnCompletionListener(PlayerActivity.this);
                    mFirstPlay = false;
                }
            });
            runBeforePlay(mFirstPlay);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    protected abstract void runAfterPlay(boolean isFirst);

    protected abstract void runBeforePlay(boolean isFirst);

}
