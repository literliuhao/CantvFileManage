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
    public static List<Activity> mHomeActivityList;    //存HomeActivity

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        activityList = new ArrayList<>();
        mHomeActivityList = new ArrayList<>();
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
    public static void onFinishActivity() {
        for (int i = 0; i < activityList.size(); i++) {
            activityList.get(i).finish();
        }
        activityList.clear();
    }

    /**
     * 修复:0S-1439,播放本地视频时按主页键退出，此时点击进入应用模块中的
     * 图片、视频、音乐均会续播之前播放的视频，点击进入“文件管理”再按返回键退出也会进入续播页面
     *
     * @param homeAc
     */
    public static void addHomeActivity(Activity homeAc) {
        if (!mHomeActivityList.contains(homeAc)) {
            mHomeActivityList.add(homeAc);
        }
    }

    public static void removeHomeActivity() {
        mHomeActivityList.clear();
    }

}
