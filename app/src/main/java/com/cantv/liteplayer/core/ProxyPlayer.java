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

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.liteplayer.core.subtitle.SubTitle;
import com.cantv.media.BuildConfig;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.utils.StringUtil;
import com.cantv.media.center.utils.ToastUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.media.MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ProxyPlayer implements OnPreparedListener {
    private LitePlayer mLitePlayer;
    private PlayerStatusInfo mStatusInfo;
    private OnVideoSizeChangedListener mListener = null;
    public Boolean mRetryPlay = true;

    private OnPreparedListener mOnPreparedListener;

    private Runnable mRunnable;
    private String mType;

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

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
        getLitePlayer().setOnPreparedListener(this);
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
        this.mRunnable = callBack;
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

        getLitePlayer().setOnPreparedListener(this);
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


    /**
     * 得到是"ac3"就是Dolby音效
     *
     * @return
     */
    public String getDolbyType() {
        return mType;
    }

    /**
     * 判断是否是dolby音效
     * "aac".equalsIgnoreCase(getDolbyType()) ||
     *
     * @return
     */
    public boolean isDolby() {
        if ("eac3".equalsIgnoreCase(getDolbyType()) || "ac3".equals(getDolbyType().toLowerCase())) {
            return true;
        } else {
            return false;
        }
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
                if (null != mExceptionListener && !mRetryPlay) {
                    ToastUtils.showMessage(MyApplication.getContext(), MyApplication.getContext().getResources().getString(R.string.format_not_support));
                    mExceptionListener.ExceHappen();
                } else if (mRetryPlay) {
                    mRetryPlay = false;
                    if (null != mExceptionListener) {
                        mExceptionListener.RetryPlay();
                    } else {
                        ToastUtils.showMessage(MyApplication.getContext(), MyApplication.getContext().getResources().getString(R.string.format_not_support));
                        mExceptionListener.ExceHappen();
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
        String sourceUri = getLitePlayer().getStatusInfo().mSourceUri;
        ArrayList<String> saveSubIndexList = new ArrayList<>();
        if (sourceUri.equals("") || sourceUri.contains("//")) {
            return saveSubIndexList;
        }
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLanguageList(saveSubIndexList);
    }

    /**
     * 目的:把类似下列集合归类排序
     * ("und", "und", "1.a", "2.a", "3.a", "4.c", "6.b", "3.c", "2.und", "9.c", "1.b");
     *
     * @param list
     */
    private List<String> getLanguageList(List<String> list) {
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list3 = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            String s1 = list.get(i);
            String string = s1.substring(s1.indexOf(".") + 1);
            if (string.equals("und")) {
                list3.add(s1);
                continue;
            }
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

    @Override
    public void onPrepared(MediaPlayer mp) {

        if (null != BuildConfig.CANTV) {
            if ("can".equals(BuildConfig.CANTV)) {
                try {
                    Class<?> player = Class.forName("android.media.MediaPlayer");
                    Method method = player.getDeclaredMethod("getMetadata", boolean.class, boolean.class);
                    method.setAccessible(true);
                    Object metadataRes = method.invoke(mp, false, false);
                    Class<?> metaClass = Class.forName("android.media.Metadata");
                    Method methodKeyset = metaClass.getDeclaredMethod("keySet");
                    Object ketSetRes = methodKeyset.invoke(metadataRes);
                    Set<Integer> hashmap = (Set<Integer>) ketSetRes;
                    for (Integer i : hashmap) {
                        if (i == 26 /*AUDIO_CODEC*/) {
                            Method getString = metaClass.getDeclaredMethod("getString", int.class);
                            mType = getString.invoke(metadataRes, i).toString();
                            System.out.println("*************:" + mType.toString());
                            Log.w("DolbyType", mType.toString());
                        }
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }


        if (null != mOnPreparedListener) mOnPreparedListener.onPrepared(mp);
        if (null != mRunnable) mRunnable.run();
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
