package com.cantv.media.center.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yibh on 2017/1/13.
 */

public class Constant {

    //此处和音频/视频/图片单独入口略微不同,为了和它们区分开
    public static final String MEDIA_VIDEO = "video_main";
    public static final String MEDIA_IMAGE = "image_main";
    public static final String MEDIA_AUDIO = "audio_main";

    //此处音频/视频/图片单独入口进入
    public static final String MEDIA_VIDEO_SPE = "video";
    public static final String MEDIA_IMAGE_SPE = "image";
    public static final String MEDIA_AUDIO_SPE = "audio";

    public static List list = new ArrayList();

}
