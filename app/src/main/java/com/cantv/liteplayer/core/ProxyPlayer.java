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
import com.cantv.media.center.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static android.media.MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ProxyPlayer {
    private LitePlayer mLitePlayer;
    private PlayerStatusInfo mStatusInfo;
    private OnVideoSizeChangedListener mListener = null;
    public Boolean mRetryPlaye = true;

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

    public void setOnTimedTextListener(OnTimedTextListener listener) {
        getLitePlayer().setOnTimedTextListener(listener);
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
//        getLitePlayer().stop();
        getLitePlayer().reset();
        byte[] bytes = uri.getBytes();
        String s = new String(bytes, "UTF-8");
        getLitePlayer().setDataSource(s);
        try {
            getLitePlayer().prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.w("有异常存在", "文件播放发生异常...!");
        }
        getLitePlayer().setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
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
                    Toast.makeText(MyApplication.getContext(), "不支持当前文件格式!", Toast.LENGTH_SHORT).show();
                    mExceptionListener.ExceHappen();
                } else if (mRetryPlaye) {
                    mRetryPlaye = false;
                    if (null != mExceptionListener) {
                        mExceptionListener.RetryPlay();
                    } else {
                        Toast.makeText(MyApplication.getContext(), "不支持当前文件格式!", Toast.LENGTH_SHORT).show();
                    }
                }

                return false;
            }
        });
        return mLitePlayer;
    }


    /**
     * 获取内置字幕列表
     *
     * @return
     */
    public List<String> getINSubList() {
        ArrayList<String> saveSubIndexList = new ArrayList<>();
        TrackInfo[] trackInfos = getLitePlayer().getTrackInfo();
        if (trackInfos != null && trackInfos.length > 0) {
            for (int i = 0; i < trackInfos.length; i++) {
                TrackInfo info = trackInfos[i];
                if (info.getTrackType() == MEDIA_TRACK_TYPE_TIMEDTEXT) {
                    String language = info.getLanguage();
                    if (!"und".equals(language)) {
                        language = StringUtil.getLanguage(language);
                        saveSubIndexList.add(saveSubIndexList.size(), i + "." + language);
                    } else {
                        saveSubIndexList.add(0, i + "." + language);
                    }
                }
            }
        }
        return getLanguageList(saveSubIndexList);
    }

    /**
     * 目的:把类似下列集合归类排序
     * ("und", "und", "1.a", "2.a", "3.a", "4.c", "6.b", "3.c", "2.und", "9.c", "1.b");
     * @param list
     */
    private List<String> getLanguageList(List<String> list) {
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list3 = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            String s1 = list.get(i);
            if (s1.equals("und")) {
                list3.add(s1);
                continue;
            }
            String string = s1.substring(list.get(i).indexOf("."));
            if (!list1.contains(string)) {
                list1.add(string);
            } else {
                continue;
            }
            ArrayList<String> list2 = new ArrayList<>();
            list2.add(s1);
            for (int j = i + 1; j < list.size(); j++) {
                String s = list.get(j).substring(list.get(j).indexOf("."));
                if (s.equals(string)) {
                    list1.add(s);
                    list2.add(list.get(j));
                }
            }
            ArrayList<String> list4 = new ArrayList<>();
            if (list2.size() > 1) {
                for (int m = 0; m < list2.size(); m++) {
                    list4.add(list2.get(m) + (m + 1));
                }
            } else {
                list4.addAll(list2);
            }
            list3.addAll(list4);
        }
        return list3;
    }

    public void selectTrackInfo(int index) {
        try {
            getLitePlayer().selectTrack(index);
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

    public void reset() {
        getLitePlayer().reset();
    }

    public void setSubPath(String subPath) {
        getLitePlayer().setSubPath(subPath);
    }


}
