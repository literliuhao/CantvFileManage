package com.cantv.liteplayer.core;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.view.SurfaceHolder;

import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;

public class PlayerStatusInfo {
	public String mSourceUri = "";
	public int mCurrentPosition = 0;
	public int mAudioTrackIndex = -1;
	public SurfaceHolder mHolder = null;
	public int mVideoSubTitleIndex = -1;
	public StDisplayCallBack mStDisplayCallBack;
	public OnTimedTextListener mOnTimedTextListener = null;
	public OnCompletionListener mOnCompletionListener = null;
}
