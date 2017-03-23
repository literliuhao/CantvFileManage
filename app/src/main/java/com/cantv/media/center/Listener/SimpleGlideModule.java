package com.cantv.media.center.Listener;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.GlideModule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by shenpx on 2017/2/21 0021.
 * 修改Glide所加载的图片质量
 */

public class SimpleGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder glideBuilder) {
        if (getDeviceTotalMemory() > 1800) {
            glideBuilder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        }else{
            glideBuilder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
        }
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }

    /**
     * 获取系统总内存
     *
     * @return 单位：Byte
     */
    public long getDeviceTotalMemory() {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l / 1024 / 1024;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
