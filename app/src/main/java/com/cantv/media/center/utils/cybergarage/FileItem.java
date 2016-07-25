package com.cantv.media.center.utils.cybergarage;

import java.io.Serializable;

public class FileItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String mName = "";
	private String mPath = "/";
	private boolean isFile = false;

	public FileItem(String name, String path, boolean isFile) {
		this.mName = name;
		this.mPath = path;
		this.isFile = isFile;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	@Override
	public String toString() {
		return mName;
	}

}
