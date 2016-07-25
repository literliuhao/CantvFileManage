package com.cantv.liteplayer.core.subtitle;

import android.graphics.Bitmap;

public class StContent implements Comparable<StContent> {
	private int mFilepos = 0;
	private int mIndex = 0;
	private int mStartTime = 0;
	private int mEndTime = 0;

	private String mTitleLine = null;
	private Bitmap mTitleBmp = null;
	private String mLanguageClass = null;

	public StContent() {
		super();
	}

	public int getSubtitleIndex() {
		return mIndex;
	}

	public void setSubtitleIndex(int SubtitleIndex) {
		this.mIndex = SubtitleIndex;
	}

	public int getSubtitleStartTime() {
		return mStartTime;
	}

	public void setSubtitleStartTime(int SubtitleStartTime) {
		this.mStartTime = SubtitleStartTime;
	}

	public int getSubtitleEndTime() {
		return mEndTime;
	}

	public void setSubtitleEndTime(int SubtitleEndTime) {
		this.mEndTime = SubtitleEndTime;
	}

	public String getSubtitleLine() {
		return mTitleLine;
	}

	public void setSubtitleLine(String SubtitleLine) {
		this.mTitleLine = SubtitleLine;
	}

	public String getmLanguageClass() {
		return mLanguageClass;
	}

	public void setmLanguageClass(String mLanguageClass) {
		this.mLanguageClass = mLanguageClass;
	}

	public synchronized Bitmap getSubtitleBmp() {
		return mTitleBmp;
	}

	public synchronized void setSubtitleBmp(Bitmap SubtitleBmp) {
		this.mTitleBmp = SubtitleBmp;
	}

	public synchronized boolean hasSubTitleBmp() {
		return mTitleBmp != null && !mTitleBmp.isRecycled();
	}

	public synchronized void recycleSubTitleBmp() {
		if ((mTitleBmp != null) && (mTitleBmp.isRecycled())) {
			mTitleBmp.recycle();
			mTitleBmp = null;
		}
	}

	public int getmFilepos() {
		return mFilepos;
	}

	public void setmFilepos(int mFilepos) {
		this.mFilepos = mFilepos;
	}

	public int compareTo(StContent another) {
		return new Integer(this.getSubtitleStartTime()).compareTo(new Integer(another.getSubtitleStartTime()));
	}

	public StContent copy() {
		StContent newcontent = new StContent();
		newcontent.setmFilepos(this.getmFilepos());
		newcontent.setmLanguageClass(this.getmLanguageClass());
		newcontent.setSubtitleEndTime(this.getSubtitleEndTime());
		newcontent.setSubtitleIndex(this.getSubtitleIndex());
		newcontent.setSubtitleLine(this.getSubtitleLine());
		newcontent.setSubtitleStartTime(this.getSubtitleStartTime());
		newcontent.setSubtitleBmp(this.getSubtitleBmp());

		return newcontent;
	}
}
