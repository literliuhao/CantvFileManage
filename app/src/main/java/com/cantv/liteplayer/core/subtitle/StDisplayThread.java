package com.cantv.liteplayer.core.subtitle;

import android.media.MediaPlayer;
import android.text.Html;

import java.util.List;
import java.util.Map;

public class StDisplayThread extends Thread {
    private boolean mRunningFlag;
    private int mSecondSubIndex = 0;
    private MediaPlayer mPlayer = null;
    private String mSubtitlePath = null;
    private StContent mSubtitleContent = null;
    private StContent mOldSubtitleContent = null;
    private StDecodeResult mStDecodeResult = null;
    private StDisplayCallBack mDisplayCallBack = null;
    private Map<String, List<StContent>> mContentMap = null;

    public StDisplayThread(MediaPlayer player, StDecodeResult result, int secondSubIndex, final String path) {
        mPlayer = player;
        mRunningFlag = true;
        mSubtitlePath = path;
        mStDecodeResult = result;
        mSecondSubIndex = secondSubIndex;
        mContentMap = result.subtitleContentMap;
    }

    public void setStDisplayCallBack(StDisplayCallBack callback) {
        mDisplayCallBack = callback;
    }

    public void cancel() {
        mDisplayCallBack = null;
        mRunningFlag = false;
    }

    public void run() {
        mRunningFlag = true;
        while (mRunningFlag) {
            int delayTime = 0;
            try {
                int position = mPlayer.getCurrentPosition();
                mSubtitleContent = StUtil.get2ndSubtitleContent(position, mSecondSubIndex, mContentMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mSubtitleContent != null) {
                delayTime = mSubtitleContent.getSubtitleEndTime() - mSubtitleContent.getSubtitleStartTime();
            }

            if ((mSubtitleContent != null) && (mOldSubtitleContent != null) && mSubtitleContent.getSubtitleIndex() == mOldSubtitleContent.getSubtitleIndex()) {
                smartSleep();
                continue;
            }

            if ((mSubtitleContent != null) && (mStDecodeResult != null)) {
                if (mStDecodeResult.isPictureSub) {
                    StUtil.decodePictureSubtitle(mSubtitlePath, mSubtitleContent, mStDecodeResult, 1000);
                    mOldSubtitleContent = mSubtitleContent;
                    sendShowSubtitleMsg(delayTime, true);
                } else {
                    sendShowSubtitleMsg(delayTime, false);
                }
            }
            smartSleep();
            if (mSubtitleContent != null) {
                mSubtitleContent.recycleSubTitleBmp();
            }
            if (mOldSubtitleContent != null) {
                mOldSubtitleContent.recycleSubTitleBmp();
            }
        }
    }

    private void smartSleep() {
        try {
            int framePerSec = 300;
            if (mSubtitlePath.endsWith(".sub")) {
                Long time = framePerSec > 10 ? (long) framePerSec : 10;
                if (!mStDecodeResult.isPictureSub) {
                    time *= 10;
                }
                Thread.sleep(time);
            } else {
                Thread.sleep(500L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendShowSubtitleMsg(int delay, boolean isPicture) {
        if (mDisplayCallBack != null) {
            if (null != mSubtitleContent.getSubtitleLine()) {
                mDisplayCallBack.showSubTitleText(Html.fromHtml(mSubtitleContent.getSubtitleLine()).toString());
            }
        }
    }
}
