package com.cantv.media.center.ui.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.widget.Toast;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.greendao.DaoOpenHelper;
import com.cantv.media.center.greendao.VideoPlayer;
import com.cantv.media.center.ui.player.PlayerController.CoverFlowViewListener;
import com.cantv.media.center.ui.player.PlayerController.PlayerCtrlBarContext;
import com.cantv.media.center.ui.player.PlayerController.PlayerCtrlBarListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BasePlayer extends Activity implements OnCompletionListener, PlayerCtrlBarContext, PlayerCtrlBarListener, CoverFlowViewListener, MediaPlayer.OnTimedTextListener {

    protected List<Media> mDataList;
    protected int mDefaultPlayIndex;
    private ProxyPlayer mPlayer;
    protected int mCurPlayIndex;
    private boolean mFistPlay = true;
    protected VideoPlayer mRecord;
    private boolean setVideoStop;   //为了解决OS-1677,回到主页视频会重试播放的异常

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
                if (!setVideoStop) {
                    if (null != mPlayer) {
                        mPlayer.mRetryPlaye = false;
                    }
                    playMedia(mCurPlayIndex);
                } else {
                    setVideoStop = !setVideoStop;
                }
            }
        });

        return mPlayer;
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        //循环播放
        scrollToNext(null);
    }

    @Override
    public boolean scrollToNext(OnCompletionListener listener) {
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
        if (!isPlayerPaused()) getProxyPlayer().pause();
        else getProxyPlayer().start();
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
            mPlayer.setOnTimedTextListener(null);
        }
        try {
            index = (index < 0) ? mDataList.size() - 1 : index;
            index = (index >= mDataList.size()) ? 0 : index;
            mCurPlayIndex = index;
            final String url = mDataList.get(index).isSharing ? mDataList.get(index).sharePath : mDataList.get(index).mUri;
            getProxyPlayer().playMedia(url, new Runnable() {
                @Override
                public void run() {
                    runAfterPlay(mFistPlay);
                    //添加内置字幕监听
                    mPlayer.addText(url, BasePlayer.this);
                    //添加内置字幕监听
                    mPlayer.setOnCompletionListener(BasePlayer.this);
                    mFistPlay = false;
                    runProgressBar();
                    if (null != mPlayer) {
                        mPlayer.mRetryPlaye = true;
                    }
                }
            });
            List<VideoPlayer> list = DaoOpenHelper.getInstance(this).queryInfo(mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri);
            if (list.size() == 0) {
                mRecord = null;
            } else {
                mRecord = list.get(0);
            }
        } catch (Exception e) {
            Toast.makeText(MyApplication.mContext, "不支持当前文件格式!", Toast.LENGTH_SHORT).show();
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
        setVideoStop = true;
        if (getProxyPlayer().isPlaying()) {
            getProxyPlayer().stop();
        }
        getProxyPlayer().reset();
        getProxyPlayer().release();
        super.onStop();
    }
}
