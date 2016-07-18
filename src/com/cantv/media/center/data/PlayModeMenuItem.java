package com.cantv.media.center.data;

public class PlayModeMenuItem extends MenuItem {

    private int playMode;

    public PlayModeMenuItem() {
        super();
    }

    public PlayModeMenuItem(String title, int type, int mode) {
        super(title, type);
        playMode = mode;
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    @Override
    public String toString() {
        super.toString();
        return "PlayModeMenuItem [playMode=" + playMode + "]";
    }
}
