package com.cantv.liteplayer.core;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StDecodeResult;
import com.cantv.liteplayer.core.subtitle.StDecodeThread;
import com.cantv.liteplayer.core.subtitle.StDecodeThread.StDecoderListener;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.liteplayer.core.subtitle.StDisplayThread;
import com.cantv.liteplayer.core.subtitle.StUtil;
import com.cantv.liteplayer.core.subtitle.SubTitle;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class PlayerAssistant {
    private List<SubTitle> mVideoSubTitles;
    private List<AudioTrack> mAudioTracks;
    private StDisplayThread mStDisplayThread;
    private StDecodeThread mStDecodeThread;
    private StDisplayCallBack mStDisplayCallBack;

    private int mAudioTrackIndex = -1;
    private int mVideoSubTitleIndex = -1;

    public PlayerAssistant() {
        mVideoSubTitles = new ArrayList<SubTitle>();
        mAudioTracks = new ArrayList<AudioTrack>();
    }

    public List<SubTitle> getVideoSubTitles() {
        return mVideoSubTitles;
    }

    public List<AudioTrack> getAudioTracks() {
        return mAudioTracks;
    }

    public void setSubTitleDisplayCallBack(StDisplayCallBack callback) {
        mStDisplayCallBack = callback;
    }

    public void loadSubTitlesAndAudioTrack(LitePlayer player) throws Exception {
        MediaPlayer.TrackInfo[] trackInfos = player.getTrackInfo();
        int trackType = -1;
        String value = "";
        for (int i = 0; i < trackInfos.length; i++) {
            value = trackInfos[i].getLanguage();
            // if ("und".equals(value)) {
            // value = "鏃�";
            // continue;
            // }
            trackType = trackInfos[i].getTrackType();
            if (trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT || trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE) {
                mVideoSubTitles.add(new SubTitle(value, i));
            } else if (trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                mAudioTracks.add(new AudioTrack(value, i));
            }
        }
        List<String> externalSubTitles = StUtil.getSubtitlePath(player.getStatusInfo().mSourceUri);
        for (String each : externalSubTitles) {
            mVideoSubTitles.add(new SubTitle(each, -1));
        }

        setSubTitle(player, 0);
    }

    public void setAudioTrack(MediaPlayer player, int index) {
        if (mAudioTrackIndex == index || mAudioTracks == null || index < 0 || index >= mAudioTracks.size())
            return;
        mAudioTrackIndex = index;
        try {
            player.selectTrack(mAudioTracks.get(index).getIndexOfTrackes());
        } catch (Exception e) {
        }

    }

    public void setSubTitle(final MediaPlayer player, final int index) {
        if (mVideoSubTitleIndex == index || mVideoSubTitles == null || index < 0 || index >= mVideoSubTitles.size())
            return;
        mVideoSubTitleIndex = index;
        final SubTitle subTitle = mVideoSubTitles.get(mVideoSubTitleIndex);
        if (mStDecodeThread != null) {
            mStDecodeThread.cancel();
        }
        if (mStDisplayThread != null) {
            mStDisplayThread.cancel();
            notifySubTitleChanging();
        }
        if (subTitle.isExtrnalFile() == false) {
            try {
                player.selectTrack(subTitle.getIndexOfTrackes());
            } catch (Exception e) {
            }
            return;
        }
        mStDecodeThread = new StDecodeThread(subTitle.getName(), new StDecoderListener() {
            @Override
            public void onDecoded(StDecodeResult result) {
                mStDisplayThread = new StDisplayThread(player, result, 0, subTitle.getName());
                mStDisplayThread.setStDisplayCallBack(mStDisplayCallBack);
                mStDisplayThread.start();
            }
        });
        mStDecodeThread.start();
    }


    public void setSubTitle(final MediaPlayer player, String subPath) {
        final SubTitle subTitle = new SubTitle(subPath, -1);
        if (mStDecodeThread != null) {
            mStDecodeThread.cancel();
        }
        if (mStDisplayThread != null) {
            mStDisplayThread.cancel();
            notifySubTitleChanging();
        }
        if (subTitle.isExtrnalFile() == false) {
            try {
                player.selectTrack(subTitle.getIndexOfTrackes());
            } catch (Exception e) {
            }
            return;
        }
        mStDecodeThread = new StDecodeThread(subTitle.getName(), new StDecoderListener() {
            @Override
            public void onDecoded(StDecodeResult result) {
                mStDisplayThread = new StDisplayThread(player, result, 0, subTitle.getName());
                mStDisplayThread.setStDisplayCallBack(mStDisplayCallBack);
                mStDisplayThread.start();
            }
        });
        mStDecodeThread.start();
    }


    public void release() {
        if (mStDisplayThread != null) {
            mStDisplayThread.cancel();
            mStDisplayThread = null;
        }
        if (mStDecodeThread != null) {
            mStDecodeThread.cancel();
            mStDecodeThread = null;
        }
        mAudioTracks.clear();
        mVideoSubTitles.clear();
    }

    private void notifySubTitleChanging() {
        if (mStDisplayCallBack != null) {
            mStDisplayCallBack.onSubTitleChanging();
        }
    }
}
