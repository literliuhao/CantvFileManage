package com.cantv.media.center.utils;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

/**
 * 统计管理类（目前使用友盟统计）
 * Created by shenpx on 2017/3/16 0016.
 */

public class StatisticsUtil {

    public StatisticsUtil() {

    }

    /**
     * onResume调用
     * @param context
     */
    public static void registerResume(Context context) {
        MobclickAgent.onResume(context);
    }

    /**
     * onPause调用
     * @param context
     */
    public static void registerPause(Context context) {
        MobclickAgent.onPause(context);
    }

    /**
     * 自定义事件
     * @param context
     * @param eventId
     */
    public static void customEvent(Context context,String eventId) {
        MobclickAgent.onEvent(context, eventId);
    }

}
