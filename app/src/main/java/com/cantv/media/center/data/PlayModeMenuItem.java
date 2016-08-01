package com.cantv.media.center.data;

public class PlayModeMenuItem extends MenuItem {

	private int playMode;
	private int drawableResId;

	public PlayModeMenuItem() {
		super();
	}

	public PlayModeMenuItem(String title, int type, int mode, int drawableResId) {
		super(title, type);
		playMode = mode;
		this.drawableResId = drawableResId;
	}

	public int getPlayMode() {
		return playMode;
	}

	public void setPlayMode(int playMode) {
		this.playMode = playMode;
	}

	public int getDrawableResId() {
		return drawableResId;
	}

	public void setDrawableResId(int drawableResId) {
		this.drawableResId = drawableResId;
	}

	@Override
	public String toString() {
		return "PlayModeMenuItem [playMode=" + playMode + ", drawableResId=" + drawableResId + "]";
	}

}
