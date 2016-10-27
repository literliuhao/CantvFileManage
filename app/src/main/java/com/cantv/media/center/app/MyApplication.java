package com.cantv.media.center.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    public static Context mContext;
    //目前只为了存GridViewActivity,播放视频/音频/图片的Activity,为了解决0S/1439问题
    private static List<Activity> activityList;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        activityList = new ArrayList<>();
    }

    public static Context getContext() {
        return mContext;
    }

    public static void addActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    public static List getActivityList() {
        return activityList;
    }

    public static void removeActivity(Activity activity) {
        if (activityList.contains(activity)) {
            activityList.remove(activity);
        }
    }

    /**
     * author: yibh
     * Date: 2016/10/27  16:21 .
     * 将存的Activity都关闭
     */
    public static void onFinishThreeActivity() {
        for (int i = 0; i < activityList.size(); i++) {
            activityList.get(i).finish();
        }
        activityList.clear();
    }

}
