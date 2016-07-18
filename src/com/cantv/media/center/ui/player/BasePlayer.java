package com.cantv.media.center.ui.player;

import java.util.ArrayList;
import java.util.List;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.center.greendao.DaoOpenHelper;
import com.cantv.media.center.greendao.VideoPlayer;
import com.cantv.media.center.ui.player.PlayerController.CoverFlowViewListener;
import com.cantv.media.center.ui.player.PlayerController.PlayerCtrlBarContext;
import com.cantv.media.center.ui.player.PlayerController.PlayerCtrlBarListener;
import com.cantv.media.center.utils.MediaUtils;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public abstract class BasePlayer extends Activity implements OnCompletionListener, PlayerCtrlBarContext, PlayerCtrlBarListener, CoverFlowViewListener {

    protected List<String> mDataList;
    protected int mDefaultPlayIndex;
    private ProxyPlayer mPlayer;
    protected int mCurPlayIndex;
    private boolean mFistPlay = true;
    private boolean mInitDone = false;
    private boolean mPaused = false;
    protected VideoPlayer mRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataList = getIntent().getStringArrayListExtra("data_list");
        String url = Uri.decode(getIntent().getDataString());
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
        // UiUtils.doHideSystemBar(this);
    }

    protected ProxyPlayer getProxyPlayer() {
        if (mPlayer == null) {
            mPlayer = new ProxyPlayer();
            mPlayer.setOnCompletionListener(this);
        }
        return mPlayer;
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (mCurPlayIndex == mDataList.size() - 1) {
            Toast.makeText(BasePlayer.this, "没有下一个视频了！", 0).show();
            finish();
        } else {
            scrollToNext(null);
        }
    }

    @Override
    public boolean scrollToNext(OnCompletionListener listener) {
        if (mCurPlayIndex == mDataList.size() - 1) {
            Toast.makeText(this, "没有下一个视频了！", 0).show();
            return false;
        }
        mPaused = false;
        mCurPlayIndex++;
        mPlayer.setOnCompletionListener(listener);
        playMedia(mCurPlayIndex);
        return true;
    }

    @Override
    public boolean scrollPre(OnCompletionListener listener) {
        if (mCurPlayIndex == 0) {
            Toast.makeText(this, "没有上一个视频了！", 0).show();
            return false;
        }

        mPaused = false;
        mCurPlayIndex--;
        mPlayer.setOnCompletionListener(listener);
        playMedia(mCurPlayIndex);
        return true;
    }

    @Override
    public void onPlayNext() {

    }

    @Override
    public void onPlayPrev() {

    }

    @Override
    public void onPlayRewind() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPlayForwad() {

    }

    @Override
    public void onPlayerPlayOrPause() {
        mPaused = !mPaused;
        if (mPaused) getProxyPlayer().pause();
        else getProxyPlayer().start();
    }

    @Override
    public void onPlaySeekTo(int duration, OnSeekCompleteListener listener) {
        // mPaused = true;
        getProxyPlayer().seekTo(duration, listener);
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
        return mPlayer == null ? true : (!mPlayer.isPlaying());
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
                    mPlayer.setOnCompletionListener(BasePlayer.this);
                    mFistPlay = false;
                    runProgressBar();
                }
            });
            List<VideoPlayer> list = DaoOpenHelper.getInstance(this).queryInfo(mDataList.get(mCurPlayIndex));
            if (list.size() == 0) {
                mRecord = null;
            } else {
                mRecord = list.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void playDefualt() {
        mInitDone = true;
        playMedia(mDefaultPlayIndex);
    }

    protected abstract void runAfterPlay(boolean isFirst);

    protected abstract void runProgressBar();

    @Override
    public void finish() {
        getProxyPlayer().release();
        super.finish();
    }

    @Override
    public String getDefinition() {
        if (mPlayer != null) {
            int videoWidth = mPlayer.getVideoWidth();
            int videoHeight = mPlayer.getVideoHeight();
            return obtainDefinition(videoWidth, videoHeight);
        }
        return null;
    }

    public String obtainDefinition(int videoWidth, int videoHeight) {

        if (1080 == videoWidth && 1920 == videoHeight || 1080 == videoHeight && 1920 == videoWidth) {
            return "1080P";
        } else if (720 == videoWidth && 1280 == videoHeight || 720 == videoHeight && 1280 == videoWidth) {
            return "720P";
        } else if (3840 == videoWidth && 2160 == videoHeight || 3840 == videoHeight && 2160 == videoHeight) {
            return "4K";
        } else {
            return "";
        }
    }
}
