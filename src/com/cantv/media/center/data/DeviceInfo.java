package com.cantv.media.center.data;

import com.cantv.media.center.utils.cybergarage.FileItem;

public class DeviceInfo {

	private String ip;
	private String userName;
	private String password;
	private FileItem mFileItem;

	public DeviceInfo(String ip) {
		super();
		this.ip = ip;
	}

	public DeviceInfo() {
		super();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public FileItem getFileItem() {
		return mFileItem;
	}

	public void setFileItem(FileItem fileItem) {
		this.mFileItem = fileItem;
	}

	@Override
	public String toString() {
		return "DeviceInfo [ip=" + ip + ", userName=" + userName + ", password=" + password + ", mFileItem=" + mFileItem
				+ "]";
	}

}
