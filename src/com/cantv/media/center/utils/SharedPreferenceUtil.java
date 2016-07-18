package com.cantv.media.center.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.cantv.media.center.app.MyApplication;

/**
 * Created by yibh on 2016/7/6.
 */

public class SharedPreferenceUtil {
    private static final String FILE_SORT_TYPE = "sort_type";
    private static final String GRID_STYLE = "grid_style";
    private static final String SHARE_DEFAULT_CONFIG = "default_config";
    private static SharedPreferences mSp = MyApplication.mContext.getSharedPreferences(SHARE_DEFAULT_CONFIG, Context.MODE_PRIVATE);
    private static SharedPreferences.Editor mEditor = mSp.edit();

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
     * 同步提交方式
     *
     * @param editor
     * @return
     */
    private static boolean commitInfo(SharedPreferences.Editor editor) {
        return editor.commit();
    }

    /**
     * 异步提交方式
     * 不在乎是否提交成功,建议使用这个
     *
     * @param editor
     */
    private static void applyInfo(SharedPreferences.Editor editor) {
        editor.apply();
    }


}
