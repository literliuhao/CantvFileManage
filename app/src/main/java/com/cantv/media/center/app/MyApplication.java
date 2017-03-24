package com.cantv.media.center.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.cantv.media.BuildConfig;
import com.cantv.media.center.ui.upgrade.MyUpgradeListener;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.BuglyStrategy;
import com.tencent.bugly.beta.Beta;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    public static final String APP_ID = "af99255c0f"; //文件管理器的APP_ID TODO 替换成bugly上注册的appid
//    public static final String APP_ID = "22b100f58c"; //测试用的文件管理器的APP_ID TODO 替换成bugly上注册的appid
    public static Context mContext;
    //目前只为了存GridViewActivity,播放视频/音频/图片的Activity,为了解决0S/1439问题
//    private static List<Activity> activityList;
    public static List<Activity> mHomeActivityList;    //存HomeActivity
    public static boolean format = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        initBugly();
//        activityList = new ArrayList<>();
        mHomeActivityList = new ArrayList<>();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        MobclickAgent.enableEncrypt(true);
    }

    public static Context getContext() {
        return mContext;
    }

//    public static void addActivity(Activity activity) {
//        if (!activityList.contains(activity)) {
//            activityList.add(activity);
//        }
//    }

//    public static List getActivityList() {
//        return activityList;
//    }

//    public static void removeActivity(Activity activity) {
//        if (activityList.contains(activity)) {
//            activityList.remove(activity);
//        }
//    }

//    /**
//     * author: yibh
//     * Date: 2016/10/27  16:21 .
//     * 将存的Activity都关闭
//     */
//    public static void onFinishActivity() {
//        for (int i = 0; i < activityList.size(); i++) {
//            activityList.get(i).finish();
//        }
//        activityList.clear();
//    }

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

    //关闭首页activity
    public static void onFinishHomeActivity() {
        if (mHomeActivityList.size() > 0) {
            mHomeActivityList.get(0).finish();
        }
    }

    private RefWatcher refWatcher;
    public static RefWatcher getRefWatcher(Context context) {
        MyApplication application = (MyApplication) context.getApplicationContext();
        return application.refWatcher;
    }


    /**
     * 此功能和图片使用ViewPager功能一起上线
     */
    public void initBugly() {

        /**** Beta高级设置*****/
        /**
         * true表示app启动自动初始化升级模块；.
         * false不好自动初始化
         * 开发者如果担心sdk初始化影响app启动速度，可以设置为false
         * 在后面某个时刻手动调用
         */
        Beta.autoInit = true;

        /**
         * true表示初始化时自动检查升级.
         * false表示不会自动检查升级，需要手动调用Beta.checkUpgrade()方法
         */
        Beta.autoCheckUpgrade = true;


        /**
         * 设置sd卡的Download为更新资源保存目录;.
         * 后续更新资源会保存在此目录，需要在manifest中添加WRITE_EXTERNAL_STORAGE权限;
         */
        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        Beta.upgradeListener = new MyUpgradeListener();
        BuglyStrategy strategy = new BuglyStrategy();
//        strategy.setAppChannel("cantv");
        strategy.setUploadProcess(true);
        //设置开发设备
        Bugly.setIsDevelopmentDevice(mContext, true);
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        Bugly.init(mContext, APP_ID, BuildConfig.DEBUG, strategy);    //1.6.0版本这句取消注释
//        Bugly.init(mContext, APP_ID, false, strategy);

        /**
         * 已经接入Bugly用户改用上面的初始化方法,不影响原有的crash上报功能;.
         * init方法会自动检测更新，不需要再手动调用Beta.checkUpgrade(),如需增加自动检查时机可以使用Beta.checkUpgrade(false,false);
         * 参数1：applicationContext
         * 参数2：appId
         * 参数3：是否开启debug
         */
//        Bugly.init(getApplicationContext(), APP_ID, true);


    }

}
