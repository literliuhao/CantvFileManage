package com.cantv.media.center.data;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public class ImageBean {

    private int width;
    private int height;
    private int position;
    private boolean loadingSucceed;//是否加载成功
    private boolean fullScreen;//是否全屏显示

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean getLoadingSucceed() {
        return loadingSucceed;
    }

    public void setLoadingSucceed(boolean loadingSucceed) {
        this.loadingSucceed = loadingSucceed;
    }

    public boolean getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public ImageBean() {
    }

    public ImageBean(int width, int height, int position, boolean loadingSucceed, boolean fullScreen) {
        this.width = width;
        this.height = height;
        this.position = position;
        this.loadingSucceed = loadingSucceed;
        this.fullScreen = fullScreen;
    }

    @Override
    public String toString() {
        return "ImageBean{" +
                "width=" + width +
                ", height=" + height +
                ", position=" + position +
                ", loadingSucceed=" + loadingSucceed +
                ", fullScreen=" + fullScreen +
                '}';
    }
}
