package com.cantv.media.center.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cantv.media.center.app.MyApplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yibh on 2016/7/6.
 */

public class SharedPreferenceUtil {
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
	 * @param devicePath
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
	 * 
	 * @param devicePath
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
}
