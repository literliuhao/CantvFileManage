package com.cantv.media.center.data;

public class CanNetworkInfo {

	public enum NETTYPE {
		WIFI, ETHERNET;
	}

	private NETTYPE netType;
	private String netName;
	private String netIP;

	public NETTYPE getNetType() {
		return netType;
	}

	public void setNetType(NETTYPE netType) {
		this.netType = netType;
	}

	public String getNetName() {
		return netName;
	}

	public void setNetName(String netName) {
		this.netName = netName;
	}

	public String getNetIP() {
		return netIP;
	}

	public void setNetIP(String netIP) {
		this.netIP = netIP;
	}

	@Override
	public String toString() {
		return "CanNetworkInfo [netType=" + netType + ", netName=" + netName + ", netIP=" + netIP + "]";
	}

}
