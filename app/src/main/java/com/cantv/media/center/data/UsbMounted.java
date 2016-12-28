package com.cantv.media.center.data;

/**
 * Created by yibh on 2016/12/28.
 */

public class UsbMounted {
    public boolean mIsRemoved;  //是否是移除U盘
    public String mUsbPath; //插入/移除的U盘路径

    public UsbMounted(boolean isRemoved, String usbPath) {
        this.mIsRemoved = isRemoved;
        this.mUsbPath = usbPath;
    }
}
