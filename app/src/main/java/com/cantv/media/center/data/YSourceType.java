package com.cantv.media.center.data;

import com.cantv.media.center.constants.SourceType;

/**
 * Created by yibh on 2017/1/17.
 * 桌面弹窗进入文件管理器
 */

public class YSourceType {
    public SourceType mType;    //类型
    public int mTypeName;    //类型名称:图片,视频,音频等

    public YSourceType(SourceType mType, int typeName) {
        this.mType = mType;
        this.mTypeName = typeName;
    }
}
