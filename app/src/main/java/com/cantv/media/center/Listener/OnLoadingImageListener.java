package com.cantv.media.center.Listener;

/**
 * 图片加载接口
 * 包括图片加载成功，失败，是否已缩放，加载成功，获取图片尺寸
 * Created by Administrator on 2017/3/1 0001.
 */

public interface OnLoadingImageListener {
    void loadingImageSuccess(boolean loadSuccess, int position);

    void loadingImageFail(boolean loadFail, int position);

    void isScaled(boolean isScaled, int position);

    void loadingImageReady(boolean isLoadReady, int position);

    void getImageSize(int width, int height, boolean isScaled, int position);

}
