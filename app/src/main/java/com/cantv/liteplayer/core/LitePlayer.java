package com.cantv.liteplayer.core;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.liteplayer.core.subtitle.SubTitle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class LitePlayer extends MediaPlayer {
    private PlayerAssistant mAssitant;
    private PlayerStatusInfo mStatusInfo;
    private float mVideoWidthHeightRate = 0.5625f;
    private String mType;

    public LitePlayer() {
        mAssitant = new PlayerAssistant();
        mStatusInfo = new PlayerStatusInfo();
        setOnTimedTextListener(new OnTimedTextListener() {
            @Override
            public void onTimedText(MediaPlayer mp, TimedText text) {
                try {
                    if (mStatusInfo.mStDisplayCallBack != null) {
                        mStatusInfo.mStDisplayCallBack.showSubTitleText(text.getText());
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        });
    }

    public float getVideoWidthHeightRate() {
        return mVideoWidthHeightRate;
    }

    public PlayerStatusInfo getStatusInfo() {
        return mStatusInfo;
    }

    public void setMovieSubTitle(int index) {
        mAssitant.setSubTitle(this, index);
        mStatusInfo.mVideoSubTitleIndex = index;
    }

    public void setMovieAudioTrack(int index) {
        mAssitant.setAudioTrack(this, index);
        mStatusInfo.mAudioTrackIndex = index;
    }

    public List<SubTitle> getVideoSubTitles() {
        return mAssitant.getVideoSubTitles();
    }

    public List<AudioTrack> getAudioTracks() {
        return mAssitant.getAudioTracks();
    }

    public void setSubTitleDisplayCallBack(StDisplayCallBack callback) {
        mAssitant.setSubTitleDisplayCallBack(callback);
        mStatusInfo.mStDisplayCallBack = callback;
    }

    @Override
    public void release() {
        super.release();
        mAssitant.release();
    }

    @Override
    public void reset() {
        super.reset();
        mAssitant.release();
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        super.setDisplay(sh);
        mStatusInfo.mHolder = sh;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        super.setOnVideoSizeChangedListener(listener);
    }

    @Override
    public void setOnPreparedListener(final OnPreparedListener listener) {
        super.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoWidthHeightRate = 1.0f * mp.getVideoWidth() / mp.getVideoHeight();
                try {
                    mAssitant.loadSubTitlesAndAudioTrack(LitePlayer.this);
                } catch (Exception e) {
                }
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
                } finally {
                    if (listener != null) {
                        listener.onPrepared(mp);
                    }
                }
            }
        });
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        super.setOnCompletionListener(listener);
        mStatusInfo.mOnCompletionListener = listener;
    }

    @Override
    public void setOnTimedTextListener(OnTimedTextListener listener) {
        super.setOnTimedTextListener(listener);
        mStatusInfo.mOnTimedTextListener = listener;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        super.setDataSource(path);
        mStatusInfo.mSourceUri = path;
    }

    public void setSubPath(String subPath) {
        if (null != mAssitant) {
            mAssitant.setSubTitle(LitePlayer.this, subPath);
        }
    }

    /**
     * 用来判断是否是Dolby音效
     *
     * @return
     */
    public String getDolbyType() {
        return mType;
    }

}
