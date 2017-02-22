package com.cantv.media.center.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.app.core.utils.UiUtils;
import com.cantv.media.center.data.Constant;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.player.MediaControllerBar;

import java.util.ArrayList;
import java.util.List;

public abstract class MediaPlayerActivity extends Activity {
    public int mDefaultPlayIndex;
    public List<Media> mDataList;
    private boolean mOnKeyPressedContinuity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = "";
        try {
            url = Uri.decode(getIntent().getDataString()).substring(7);
        } catch (Exception e) {
        }
        //修复MASERATI-230USB播放图片，选择文件名为超过1000张图片文件夹打开任意相片，电视提示很抱歉，文件管理已停止运行
        mDataList = Constant.list;
        if (!TextUtils.isEmpty(url)) {
            if (mDataList == null) {
                mDataList = new ArrayList<Media>();
            }
            mDataList.clear();
            //mDataList.add(url);
        }
        mDefaultPlayIndex = getIntent().getIntExtra("data_index", 0);
        if (mDataList == null) {
            mDefaultPlayIndex = 0;
            mDataList = new ArrayList<Media>();
        }
        UiUtils.doHideSystemBar(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mOnKeyPressedContinuity || event.getRepeatCount() > 3) {
            mOnKeyPressedContinuity = true;
            return onKeyPressedContinuity(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mOnKeyPressedContinuity) {
            mOnKeyPressedContinuity = false;
            return onKeyPressedContinuity(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();
        MediaControllerBar bar = getMediaControllerBar();
        if (bar != null) {
            bar.runWhenActivityfinish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaControllerBar bar = getMediaControllerBar();
        if (bar != null) {
            bar.runWhenActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() == false) {
            MediaControllerBar bar = getMediaControllerBar();
            if (bar != null) {
                bar.runWhenActivityPause();
            }
        }
    }

    protected List<Media> getData() {
        return mDataList;
    }

    protected int indexOfDefaultPlay() {
        return mDefaultPlayIndex;
    }

    protected boolean onKeyPressedContinuity(int keyCode, KeyEvent event) {
        return false;
    }

    protected abstract MediaControllerBar getMediaControllerBar();

}
