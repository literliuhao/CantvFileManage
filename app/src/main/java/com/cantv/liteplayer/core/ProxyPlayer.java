package com.cantv.liteplayer.core;

import android.annotation.TargetApi;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaPlayer.TrackInfo;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.liteplayer.core.subtitle.SubTitle;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.utils.ToastUtils;

import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ProxyPlayer {
    private LitePlayer mLitePlayer;
    private PlayerStatusInfo mStatusInfo;
    private OnVideoSizeChangedListener mListener = null;
    private Boolean mRetryPlaye = true;

    public void start() {
        getLitePlayer().start();
    }

    public void stop() {
        getLitePlayer().stop();
    }

    public void pause() {
        getLitePlayer().pause();
    }

    public void release() {
        getLitePlayer().release();
        mLitePlayer = null;
    }

    public boolean isPlaying() {
        return getLitePlayer().isPlaying();
    }

    public int getDuration() {
        return getLitePlayer().getDuration();
    }

    public int getVideoWidth() {
        return getLitePlayer().getVideoWidth();
    }

    public int getVideoHeight() {
        return getLitePlayer().getVideoHeight();
    }

    public float getVideoWidthHeightRate() {
        return getLitePlayer().getVideoWidthHeightRate();
    }

    public int getCurrentPosition() {
        return getLitePlayer().getCurrentPosition();
    }

    public void setPlayerDisplay(SurfaceHolder sh) {
        getLitePlayer().setDisplay(sh);
    }

    public void setMovieSubTitle(int index) {
        getLitePlayer().setMovieSubTitle(index);
    }

    public void setMovieAudioTrack(int index) {
        getLitePlayer().setMovieAudioTrack(index);
    }

    public List<SubTitle> getVideoSubTitles() {
        return getLitePlayer().getVideoSubTitles();
    }

    public List<AudioTrack> getAudioTracks() {
        return getLitePlayer().getAudioTracks();
    }

    public void setSubTitleDisplayCallBack(StDisplayCallBack callback) {
        getLitePlayer().setSubTitleDisplayCallBack(callback);
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        getLitePlayer().setOnCompletionListener(listener);
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
        mListener = l;
    }

    public void seekTo(int duration, OnSeekCompleteListener listener) {
        getLitePlayer().seekTo(duration);
        getLitePlayer().setOnSeekCompleteListener(listener);
        start();
    }

    public void playMedia(String uri, final Runnable callBack) throws Exception {
        getLitePlayer().reset();
        getLitePlayer().setDataSource(uri);
        getLitePlayer().prepareAsync();
        getLitePlayer().setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                if (callBack != null) {
                    callBack.run();
                }
            }
        });
        getLitePlayer().setOnVideoSizeChangedListener(mListener);
    }

    public void runOnActivityPause() {
        mStatusInfo = getLitePlayer().getStatusInfo();
        mStatusInfo.mCurrentPosition = getCurrentPosition();
        release();
    }

    public void runOnActivityResume() throws Exception {
        if (mStatusInfo == null) return;
        getLitePlayer().setDisplay(mStatusInfo.mHolder);
        setOnCompletionListener(mStatusInfo.mOnCompletionListener);
        getLitePlayer().setOnTimedTextListener(mStatusInfo.mOnTimedTextListener);
        setSubTitleDisplayCallBack(mStatusInfo.mStDisplayCallBack);
        playMedia(mStatusInfo.mSourceUri, new Runnable() {
            @Override
            public void run() {
                if (mStatusInfo.mAudioTrackIndex >= 0)
                    setMovieAudioTrack(mStatusInfo.mAudioTrackIndex);
                if (mStatusInfo.mVideoSubTitleIndex >= 0)
                    setMovieSubTitle(mStatusInfo.mVideoSubTitleIndex);
                seekTo(mStatusInfo.mCurrentPosition, new OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer arg0) {
                        mStatusInfo = null;
                        start();
                    }
                });
            }
        });
    }

    private LitePlayer getLitePlayer() {
        if (mLitePlayer == null) {
            mLitePlayer = new LitePlayer();
            mLitePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        mLitePlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.w("异常", "文件播放发生异常!");
                if (null != mExceptionListener && !mRetryPlaye) {
                    mExceptionListener.ExceHappen();
                } else if (mRetryPlaye) {
                    mRetryPlaye = false;
                    if (null != mExceptionListener) {
                        mExceptionListener.RetryPlay();
                    } else {
                        Toast.makeText(MyApplication.getContext(), "播放发生异常!",Toast.LENGTH_LONG).show();
                    }
                }

                return false;
            }
        });
        return mLitePlayer;
    }

    public void addText(String srtPath, OnTimedTextListener listener) {
        try {
            getLitePlayer().addTimedTextSource(srtPath, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);

            getLitePlayer().setOnTimedTextListener(listener);

            TrackInfo[] trackInfos = getLitePlayer().getTrackInfo();

            if (trackInfos != null && trackInfos.length > 0) {
                for (int i = 0; i < trackInfos.length; i++) {
                    final TrackInfo info = trackInfos[i];
                    if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                        // mMediaPlayer.selectTrack(i);
                    } else if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                        getLitePlayer().selectTrack(i);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放器出现异常的监听
     *
     * @author yibh
     */
    public interface MediaplayExceptionListener {
        void ExceHappen();

        void RetryPlay();
    }

    private MediaplayExceptionListener mExceptionListener;

    public void onExceptionListener(MediaplayExceptionListener exceptionListener) {
        this.mExceptionListener = exceptionListener;
    }

}
