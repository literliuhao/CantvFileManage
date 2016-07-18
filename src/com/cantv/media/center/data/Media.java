package com.cantv.media.center.data;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.SourceType;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Media {
    public String mUri;
    public String mName;
    public SourceType mType;
    public List<Media> mSubMedias;
    public long fileSize;
    public long mTotalSize;
    public int childCount;
    public long modifiedDate;
    public boolean isDir;
    public boolean canRead;
    public boolean canWrite;
    public boolean isHidden;

    public Media(SourceType type, String uri) {
        mUri = uri;
        mType = type;
        mSubMedias = new LinkedList<Media>();
    }

    public String getUri() {
        if (isCollection() && TextUtils.isEmpty(mUri)) mUri = new File(mSubMedias.get(0).getUri()).getParent();
        return mUri;
    }

    public String getName() {
        if (TextUtils.isEmpty(mName)) mName = parseMediaName();
        return mName;
    }

    public int getSubMediasCount() {
        if (mSubMedias == null) return 0;
        return mSubMedias.size();
    }

    public List<Media> getSubMedias() {
        return mSubMedias;
    }

    public SourceType getSourceType() {
        return mType;
    }

    public MediaFormat getMediaFormat() {
        if (getSourceType() == SourceType.DEVICE) return MediaFormat.UNKNOW;
        if (getSourceType() == SourceType.MUSIC) return MediaFormat.AUDIO;
        if (getSourceType() == SourceType.MOIVE) return MediaFormat.VIDEO;
        if (getSourceType() == SourceType.PICTURE) return MediaFormat.IMAGE;
        if (getSourceType() == SourceType.APP) return MediaFormat.APP;
        if (getSourceType() == SourceType.FOLDER) {
            return MediaFormat.FOLDER;
        }
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

    public String getmUri() {
        return mUri;
    }

    public void setmUri(String mUri) {
        this.mUri = mUri;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public SourceType getmType() {
        return mType;
    }

    public void setmType(SourceType mType) {
        this.mType = mType;
    }

    public List<Media> getmSubMedias() {
        return mSubMedias;
    }

    public void setmSubMedias(List<Media> mSubMedias) {
        this.mSubMedias = mSubMedias;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
}
