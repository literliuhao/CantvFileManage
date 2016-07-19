package com.cantv.media.center.data;

public class DeviceInfo {

	private String ip;

	public DeviceInfo(String ip) {
		super();
		this.ip = ip;
	}

	public DeviceInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public String toString() {
		return "DeviceInfo [ip=" + ip + "]";
	}

}
