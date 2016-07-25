package com.cantv.liteplayer.core.audiotrack;

public class AudioTrack {
	private String mName;
	private int mIndexOfTrackes;
	
	public AudioTrack(String name, int index) {
		mName = name;
		mIndexOfTrackes = index;
	}
	
	public String getName() {
		return mName;
	}
	
	public int getIndexOfTrackes() {
		return mIndexOfTrackes;
	}
}
