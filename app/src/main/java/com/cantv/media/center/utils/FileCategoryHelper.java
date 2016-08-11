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


    //设置好对应类型
    static {
//        addItem(new String[]{"mp4", "avi", "mkv", "wmv", "mpg", "ts", "3gp", "flv", "mka", "mov", "webm", "m2ts", "vob", "mpeg", "f4v", "rmvb", "wmv", "rm", "m4v", "3gpp", "3g2", "3gpp2", "asf"}, TYPE_MOIVE);
//        AVI、MOV、ASF、WMV、3GP、FLV、MKV、RAM、RMVB、ts
        addItem(new String[]{"avi", "mov", "asf", "wmv", "3gp", "flv", "mkv", "ram", "rmvb", "ts", "mp4"}, TYPE_MOIVE);
//        BMP、PNG、JPEG、GIF、JPG
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
