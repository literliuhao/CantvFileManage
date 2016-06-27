package com.cantv.media.center.data;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.SourceType;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class Media {
	private String mUri;
	private String mName;
	private SourceType mType;
	private List<Media> mSubMedias;

	public Media(SourceType type, String uri) {
		mUri = uri;
		mType = type;
		mSubMedias = new LinkedList<Media>();
	}

	public String getUri() {
		if (isCollection() && TextUtils.isEmpty(mUri)) 
			mUri = new File(mSubMedias.get(0).getUri()).getParent();
		return mUri;
	}

	public String getName() {
		if (TextUtils.isEmpty(mName)) 
			mName = parseMediaName();
		return mName;
	}

	public int getSubMediasCount() {
		if (mSubMedias == null)
			return 0;
		return mSubMedias.size();
	}

	public List<Media> getSubMedias() {
		return mSubMedias;
	}

	public SourceType getSourceType() {
		return mType;
	}
	public MediaFormat getMediaFormat() {
		if (isCollection() && getSourceType() == SourceType.DEVICE)
			return MediaFormat.UNKNOW;
		if (isCollection() && getSourceType() == SourceType.MUSIC)
			return MediaFormat.AUDIO;
		if (isCollection() && getSourceType() == SourceType.MOIVE)
			return MediaFormat.VIDEO;
		if (isCollection() && getSourceType() == SourceType.PICTURE)
			return MediaFormat.IMAGE;
		return MediaFormat.UNKNOW;
	}

	public boolean isCollection() {
		return mSubMedias == null ? false : mSubMedias.size() > 0;
	}

	public void setSubMedias(List<Media> medias) {
		mSubMedias = medias;
	}
	public Bitmap getThumbnails() {
		return null;
	}
	protected String parseMediaName() {
		String fileName = new File(getUri()).getName();
		int index = fileName.lastIndexOf(".");
		return (index != -1) ? fileName.substring(0, index) : fileName;
	}
}
