package com.cantv.media.center.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.center.constants.PlayMode;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.PlayerControllerBar.CoverFlowViewListener;
import com.cantv.media.center.ui.PlayerControllerBar.PlayerCtrlBarContext;
import com.cantv.media.center.ui.PlayerControllerBar.PlayerCtrlBarListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PlayerActivity extends Activity implements PlayerCtrlBarContext, PlayerCtrlBarListener, OnCompletionListener, CoverFlowViewListener {

    protected int mDefaultPlayIndex;
    protected List<Media> mDataList;
    private ProxyPlayer mPlayer;
    protected int mCurPlayIndex;
    public boolean mAutoPaused;    //自动是否处于暂停状态:默认非暂停,用在界面不可见时
    public boolean mManualPaused;   //手动暂停处理:默认非暂停,用在手动停止
    private boolean mFirstPlay = true;
    private boolean mInitDone = false;
    protected CoverFlowViewListener mCoverFlowViewListener;
    protected int mPlayMode = PlayMode.IN_ORDER;// 默认顺序播放

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataList = getIntent().getParcelableArrayListExtra("data_list");
        if (mDataList == null) {
            mDataList = new ArrayList<>();
        }

        mDefaultPlayIndex = getIntent().getIntExtra("data_index", 0);
        if (mDefaultPlayIndex >= mDataList.size()) {
            mDefaultPlayIndex = 0;
        }
        //UiUtils.doHideSystemBar(this);
    }

    @Override
    protected void onStop() {
        if (!(mAutoPaused && mManualPaused)) {   //界面不可见时都要停止播放(已经暂停就不用再执行)
            if (mInitDone) {
                if (null != getProxyPlayer()) {
                    getProxyPlayer().runOnActivityPause();
                }
            }
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mInitDone) {
                if (!mManualPaused) {   //没有手动执行停止就执行开始,手动停止,就手动开始
                    getProxyPlayer().runOnActivityResume();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        if (null != getProxyPlayer() && getProxyPlayer().isPlaying()) {
            getProxyPlayer().stop();
        }
        super.finish();
    }

    @Override
    public String getPlayerTitle() {
        return mDataList.get(mCurPlayIndex).mName;
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
        return mAutoPaused;
    }

    @Override
    public void onPlayNext() {
        mAutoPaused = false;
        mPlayer.setOnCompletionListener(null);
        playMedia(mCurPlayIndex + 1);
    }

    private void onPlayRandomNext() {
        mAutoPaused = false;
        mPlayer.setOnCompletionListener(null);
        playMedia(new Random().nextInt(mDataList.size()));
    }

    private void onPlayCycle() {
        mAutoPaused = false;
        mPlayer.setOnCompletionListener(null);
        playMedia(mCurPlayIndex);
    }

    @Override
    public void onPlayPrev() {
        mAutoPaused = false;
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
        mAutoPaused = !mAutoPaused;
        if (mAutoPaused) getProxyPlayer().pause();
        else
            getProxyPlayer().start();
    }

    @Override
    public void onPlaySeekTo(int duration) {
        mAutoPaused = true;
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
            if (index >= mDataList.size()) {
//            	mPlayer.release();
//            	finish();
//            	return;
                index = index % mDataList.size();
            }
            mCurPlayIndex = index;
            getProxyPlayer().playMedia(mDataList.get(index).isSharing ? mDataList.get(index).sharePath : mDataList.get(index).mUri, new Runnable() {
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

    /**
     * 设置当前是否暂停,有这个需求(播放音频中有用到)
     *
     * @param mPaused
     */
    public void setmPaused(boolean mPaused) {
        this.mAutoPaused = mPaused;
    }


    public boolean ismManualPaused() {
        return mManualPaused;
    }

    public void setmManualPaused(boolean mManualPaused) {
        this.mManualPaused = mManualPaused;
    }

}
