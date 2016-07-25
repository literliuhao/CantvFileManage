package com.cantv.liteplayer.core.subtitle;

public class SubTitle {
	private String mName;
	private int mIndexOfTrackes;
	private String mAliasesName;
	
	public SubTitle(String name, int index) {
		mName = name;
		mIndexOfTrackes = index;
		int splitIndex = name.lastIndexOf("/");
		mAliasesName = splitIndex >=0 ? name.substring(splitIndex + 1) : name;
	}
	public String getName() {
		return mName;
	}
	public int getIndexOfTrackes() {
		return mIndexOfTrackes;
	}
	public boolean isExtrnalFile() {
		return mIndexOfTrackes < 0;
	}
	public String getAliasesName() {
		return mAliasesName;
	}
}
