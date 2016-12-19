package com.cantv.media.center.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.cantv.media.center.app.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yibh on 2016/7/6.
 */

public class SharedPreferenceUtil {

    private static final String SHARE_USER_INFO_FLAG = "share_info";
    private static final String SHARE_IP_TAG = "share_ip";
    private static final String DEVICES_TAG = "devices_flag2";
    private static final String FILE_SORT_TYPE = "sort_type";
    private static final String GRID_STYLE = "grid_style";
    private static final String DISCLAIMER_TEXT = "disclaimer_text";
    private static final String SHARE_GUIDE = "share_guide";
    private static final String SHARE_DEFAULT_CONFIG = "default_config";
    private static SharedPreferences mSp = MyApplication.mContext.getSharedPreferences(SHARE_DEFAULT_CONFIG, Context.MODE_PRIVATE);
    private static SharedPreferences.Editor mEditor = mSp.edit();

    /**
     * 同步提交方式
     *
     * @param editor
     * @return
     */
    private static boolean commitInfo(SharedPreferences.Editor editor) {
        return editor.commit();
    }

    /**
     * 异步提交方式 不在乎是否提交成功,建议使用这个
     *
     * @param editor
     */
    private static void applyInfo(SharedPreferences.Editor editor) {
        editor.apply();
    }

    /**
     * 设置排序方式
     *
     * @param sortType
     * @return
     */
    public static boolean setSortType(int sortType) {
        mEditor.putInt(FILE_SORT_TYPE, sortType);
        return commitInfo(mEditor);
    }

    /**
     * 得到排序结果
     *
     * @return
     */
    public static int getSortType() {
        return mSp.getInt(FILE_SORT_TYPE, FileComparator.SORT_TYPE_NAME_UP);
    }

    public static boolean setGridStyle(int gridSytle) {
        mEditor.putInt(GRID_STYLE, gridSytle);
        return commitInfo(mEditor);
    }

    public static int getGridStyle() {
        if (mSp != null) {
            return mSp.getInt(GRID_STYLE, 1);
        } else {
            return 1;
        }
    }


    private static List<String> defaultDevices = new ArrayList<>();

    /**
     * author: yibh
     * Date: 2016/9/26  19:34 .
     * 添加默认地址,减少初始无设备的问题
     */
    static {
        //z1
        String hzStart = "/storage/udisk";
        for (int i = 0; i < 6; i++) {
            defaultDevices.add(hzStart + i);
        }
        //Box-469
        String boxPath = "/storage/external_storage/sda";
        for (int i = 0; i < 6; i++) {
            defaultDevices.add(boxPath + i);
        }


        //电视,实际硬盘中可能序号并不是按顺序来的(如:sdb1,sdb3,sdb5,sda1,sdb1,sdd1)
//      defaultDevices.add("/mnt/usb/sda1");
//      defaultDevices.add("/mnt/usb/sdb1");
        String[] dsPaths = new String[]{"a", "b", "c", "d", "e", "f", "g"};
        String dsStart = "/mnt/usb/sd";
        for (int i = 0; i < dsPaths.length; i++) {
            defaultDevices.add(dsStart + dsPaths[i] + 1);
            for (int j = 2; j < 10; j++) {   //可能存在移动硬盘分区的情况
                defaultDevices.add(dsStart + dsPaths[i] + j);
            }
        }
        dsStart = "/mnt/sda";
        for (int i = 0; i <= 9; i++) {
            defaultDevices.add(dsStart + i);
        }

    }

    /**
     * 保存设备地址
     *
     * @param path
     */
    public static void saveDevice(String path) {
        if (!defaultDevices.contains(path.trim())) {
            if (!mSp.getString(DEVICES_TAG, "").contains(path.trim())) {
                StringBuilder stringBuilder = new StringBuilder(mSp.getString(DEVICES_TAG, ""));
                stringBuilder.append(path).append("abc");
                mEditor.putString(DEVICES_TAG, stringBuilder.toString());
                applyInfo(mEditor);
            }
        }
    }

    /**
     * 获取设备地址
     */
    public static String getDevicesPath() {
        StringBuilder pathBuilder = new StringBuilder();
        for (String path : defaultDevices) {
            pathBuilder.append(path).append("abc");
        }
        return pathBuilder + mSp.getString(DEVICES_TAG, "");
    }

    /**
     * 设置免责显示
     *
     * @param
     */
    public static boolean setDisclaimer(int Disclaimer) {
        mEditor.putInt(DISCLAIMER_TEXT, Disclaimer);
        return commitInfo(mEditor);
    }

    /**
     * 获取免责显示
     *
     * @param
     */
    public static int getDisclaimer() {
        if (mSp != null) {
            return mSp.getInt(DISCLAIMER_TEXT, 0);
        } else {
            return 0;
        }
    }

    /**
     * 保存共享IP,如果已经保存过就不再保存
     *
     * @param host
     */
    public static boolean saveLinkHost(String host) {
        String linkHostList = getLinkHostList();
        if (!linkHostList.contains(host)) {
            StringBuilder stringBuilder = new StringBuilder(linkHostList);
            stringBuilder.append("abc").append(host);
            mEditor.putString(SHARE_IP_TAG, stringBuilder.toString());
            return commitInfo(mEditor);
        } else {
            return true;
        }
    }

    /**
     * 得到已经保存的共享IP
     *
     * @return
     */
    public static String getLinkHostList() {
        return mSp.getString(SHARE_IP_TAG, "");
    }

    /**
     * 保存更新设备信息: 用户IP,用户名,密码
     *
     * @param info
     */
    public static void saveShareUserInfo(String info) {
        mEditor.putString(SHARE_USER_INFO_FLAG, info);
        applyInfo(mEditor);
    }

    /**
     * 获取共享设备保存的用户信息
     *
     * @return
     */
    public static String getShareUserInfo() {
        return mSp.getString(SHARE_USER_INFO_FLAG, "");
    }

    /**
     * 设置共享向导显示
     *
     * @param
     */
    public static boolean setShareGuide(int Disclaimer) {
        mEditor.putInt(SHARE_GUIDE, Disclaimer);
        return commitInfo(mEditor);
    }

    /**
     * 获取共享向导显示
     *
     * @param
     */
    public static int getShareGuide() {
        if (mSp != null) {
            return mSp.getInt(SHARE_GUIDE, 0);
        } else {
            return 0;
        }
    }

}
