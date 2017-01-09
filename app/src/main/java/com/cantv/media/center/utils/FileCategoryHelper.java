package com.cantv.media.center.utils;

import java.util.HashMap;

/**
 * 定义不同机型音频、视频、图片支持格式
 * Created by yibh on 2016/7/4.
 */

public class FileCategoryHelper {
    public static final int TYPE_MOVIE = 0;
    public static final int TYPE_MUSIC = 1;
    public static final int TYPE_PICTURE = 2;
    public static final int TYPE_APP = 3;
    public static final int TYPE_UNKNOW = -1; //未知类型,即上面4个类型之外的
    private static HashMap<String, Integer> mfileExtToType = new HashMap<>();


    //v1.4.8版本后支持文件格式
    static {
        addItem(new String[]{"avi", "mov", "asf", "wmv", "3gp", "flv", "mkv", "ram", "rmvb", "ts", "mp4", "mpeg", "dat", "vob", "mpg", "trp", "tp", "m2ts", "webm", "3gpp", "rm"}, TYPE_MOVIE);
        addItem(new String[]{"jpg", "jpeg", "gif", "png", "bmp"}, TYPE_PICTURE);
        addItem(new String[]{"mp3", "wma", "wav", "ogg", "ape", "flac", "aac", "mka", "m4a", "ra", "divx", "dts", "ac3", "amr","eac3"}, TYPE_MUSIC);
        addItem(new String[]{"apk"}, TYPE_APP);
    }


    private static void addItem(String[] exts, int resId) {
        if (exts != null) {
            for (String ext : exts) {
                mfileExtToType.put(ext.toLowerCase(), resId);
            }
        }
    }

    /**
     * 根据后缀名获取对应类型
     *
     * @param exitName
     * @return
     */
    public static int getFileType(String exitName) {
        Integer integer = mfileExtToType.get(exitName.toLowerCase());
        if (null == integer) {
            return TYPE_UNKNOW;
        } else {
            return integer;
        }

    }

}
