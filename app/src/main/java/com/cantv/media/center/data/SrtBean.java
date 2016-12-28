package com.cantv.media.center.data;

public class SrtBean {
	private int beginTime;// 字幕开始时间
	private int endTime;// 字幕结束时间
	private String srt1;// 上面一行字幕
	private String srt2;// 下面一行字幕
	public int getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(int beginTime) {
		this.beginTime = beginTime;
	}
	public int getEndTime() {
		return endTime;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	public String getSrt1() {
		return srt1;
	}
	public void setSrt1(String srt1) {
		this.srt1 = srt1;
	}
	public String getSrt2() {
		return srt2;
	}
	public void setSrt2(String srt2) {
		this.srt2 = srt2;
	}
	@Override
	public String toString() {
		return "SrtBean [beginTime=" + beginTime + ", endTime=" + endTime + ", srt1=" + srt1 + ", srt2=" + srt2 + "]";
	}
	
	
}
