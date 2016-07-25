package com.cantv.liteplayer.core.subtitle;

public class SubtitleStyle {
	private int fontSize;
	private int fontColor;

	public SubtitleStyle(int fontSize, int fontColor) {
		super();
		this.fontSize = fontSize;
		this.fontColor = fontColor;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getFontColor() {
		return fontColor;
	}

	public void setFontColor(int fontColor) {
		this.fontColor = fontColor;
	}
}
