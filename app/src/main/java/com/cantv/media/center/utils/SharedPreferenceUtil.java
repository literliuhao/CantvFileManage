package com.cantv.media.center.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.cantv.media.center.app.MyApplication;

/**
 * Created by yibh on 2016/7/6.
 */

public class SharedPreferenceUtil {

    private static final String VIDEO_CURR_PROGRESS_FLAG = "video_progress_flag";
    private static final String SHARE_USER_INFO_FLAG = "share_info";
    private static final String SHARE_IP_TAG = "share_ip";
    private static final String DEVICES_TAG = "devices_flag2";
    private static final String FILE_SORT_TYPE = "sort_type";
    private static final String GRID_STYLE = "grid_style";
    private static final String DISCLAIMER_TEXT = "disclaimer_text";
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

    /**
     * 保存设备地址
     *
     * @param path
     */
    public static void saveDevice(String path) {
        String devicesPath = getDevicesPath();

        if (!devicesPath.contains(path)) {
            StringBuilder stringBuilder = new StringBuilder(devicesPath);
            stringBuilder.append("abc").append(path);
            mEditor.putString(DEVICES_TAG, stringBuilder.toString());
            applyInfo(mEditor);
        }
    }

    /**
     * 获取设备地址
     */
    public static String getDevicesPath() {
        return mSp.getString(DEVICES_TAG, "");
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
        }else {
            return false;
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
     * 保存视频播放进度
     *
     * @param progress
     */
    public static void saveVideoProgress(String path,int progress) {
        mEditor.putString(VIDEO_CURR_PROGRESS_FLAG, path+","+progress);
        applyInfo(mEditor);
    }

    /**
     * 获取保存的视频进度
     *
     * @return
     */
    public static String  getVideoProgress() {
        return mSp.getString(VIDEO_CURR_PROGRESS_FLAG, "");
    }

}
