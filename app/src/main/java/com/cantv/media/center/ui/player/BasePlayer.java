package com.cantv.media.center.ui.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.widget.Toast;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.greendao.DaoOpenHelper;
import com.cantv.media.center.greendao.VideoPlayer;
import com.cantv.media.center.ui.player.PlayerController.CoverFlowViewListener;
import com.cantv.media.center.ui.player.PlayerController.PlayerCtrlBarContext;
import com.cantv.media.center.ui.player.PlayerController.PlayerCtrlBarListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BasePlayer extends Activity implements OnCompletionListener, PlayerCtrlBarContext, PlayerCtrlBarListener, CoverFlowViewListener {

    protected List<Media> mDataList;
    protected int mDefaultPlayIndex;
    private ProxyPlayer mPlayer;
    protected int mCurPlayIndex;
    private boolean mFistPlay = true;
    protected VideoPlayer mRecord;

    protected abstract void runAfterPlay(boolean isFirst);

    protected abstract void runProgressBar();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataList = getIntent().getParcelableArrayListExtra("data_list");
        mDefaultPlayIndex = getIntent().getIntExtra("data_index", 0);
        if (mDataList == null) {
            mDefaultPlayIndex = 0;
            mDataList = new ArrayList<>();
        }
    }

    protected ProxyPlayer getProxyPlayer() {
        if (mPlayer == null) {
            mPlayer = new ProxyPlayer();
            mPlayer.setOnCompletionListener(this);
        }

        mPlayer.onExceptionListener(new ProxyPlayer.MediaplayExceptionListener() {
            @Override
            public void ExceHappen() {
                getProxyPlayer().release();
                finish();
            }

            @Override
            public void RetryPlay() {
                playMedia(mCurPlayIndex);
            }
        });

        return mPlayer;
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        //循环播放
//        if (mCurPlayIndex == mDataList.size() - 1) {
//            Toast.makeText(BasePlayer.this, "没有下一个视频了！", Toast.LENGTH_SHORT).show();
//            getProxyPlayer().stop();
//            finish();
//        } else
        {
            scrollToNext(null);
        }
    }

    @Override
    public boolean scrollToNext(OnCompletionListener listener) {
//        if (mCurPlayIndex == mDataList.size() - 1) {
//            Toast.makeText(this, "没有下一个视频了！", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        //循环播放
        mCurPlayIndex = ++mCurPlayIndex % mDataList.size();
        mPlayer.setOnCompletionListener(listener);
        playMedia(mCurPlayIndex);
        return true;
    }

    @Override
    public boolean scrollPre(OnCompletionListener listener) {
        if (mCurPlayIndex == 0) {
            Toast.makeText(this, "没有上一个视频了！", Toast.LENGTH_SHORT).show();
            return false;
        }

        mCurPlayIndex--;
        mPlayer.setOnCompletionListener(listener);
        playMedia(mCurPlayIndex);
        return true;
    }

    @Override
    public void onPlayerPlayOrPause() {
        if (!isPlayerPaused())
            getProxyPlayer().pause();
        else
            getProxyPlayer().start();
    }

    @Override
    public void onPlaySeekTo(int duration, OnSeekCompleteListener listener) {
        getProxyPlayer().seekTo(duration, listener);
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
            getProxyPlayer().playMedia(mDataList.get(index).isSharing ? mDataList.get(index).sharePath : mDataList.get(index).mUri, new Runnable() {
                @Override
                public void run() {
//                    getProxyPlayer().start();
                    runAfterPlay(mFistPlay);
                    mPlayer.setOnCompletionListener(BasePlayer.this);
                    mFistPlay = false;
                    runProgressBar();
                }
            });
            List<VideoPlayer> list = DaoOpenHelper.getInstance(this).queryInfo(mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri);
            if (list.size() == 0) {
                mRecord = null;
            } else {
                mRecord = list.get(0);
            }
        } catch (Exception e) {
            getProxyPlayer().stop();
            this.finish();
            e.printStackTrace();
        }
    }

    protected void playDefualt() {
        playMedia(mDefaultPlayIndex);
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

    @Override
    protected void onStop() {
        getProxyPlayer().pause();
        super.onStop();
    }
}
