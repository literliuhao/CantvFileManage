package com.cantv.media.center.utils;

import java.util.HashMap;

/**
 * Created by yibh on 2016/7/4.
 */

public class FileCategoryHelper {
    public static final int TYPE_MOIVE = 0;
    public static final int TYPE_MUSIC = 1;
    public static final int TYPE_PICTURE = 2;
    public static final int TYPE_APP = 3;
    public static final int TYPE_UNKNOW = -1; //未知类型,即上面4个类型之外的
    private static HashMap<String, Integer> mfileExtToType = new HashMap<>();


    //2016-11-03修改
    static {
        //第三方版本规则：音频、视频、图片格式增加，此文件只能不减
//        addItem(new String[]{"avi", "mov", "asf", "wmv", "3gp", "flv", "mkv", "ram", "rmvb", "ts","mp4", "mpeg", "dat", "vob", "mpg", "trp", "tp", "m2ts", "webm"}, TYPE_MOIVE);
//        addItem(new String[]{"jpg", "jpeg", "gif", "png", "bmp"}, TYPE_PICTURE);
//        addItem(new String[]{"mp3", "wma", "wav", "ogg", "ape", "flac", "aac", "mka", "m4a", "ra"}, TYPE_MUSIC);
//        addItem(new String[]{"apk"}, TYPE_APP);

        //内部通用版本规则
        addItem(new String[]{"avi", "mov", "asf", "wmv", "3gp", "flv", "mkv", "ram", "rmvb", "ts", "mp4"}, TYPE_MOIVE);
        addItem(new String[]{"jpg", "jpeg", "gif", "png", "bmp"}, TYPE_PICTURE);
        addItem(new String[]{"mp3", "wma", "wav", "ogg", "ape", "flac"}, TYPE_MUSIC);
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
            return integer.intValue();
        }

    }

}
