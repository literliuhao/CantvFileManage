package com.cantv.media.center.share;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.data.DeviceInfo;
import com.cantv.media.center.directory.dialog.CommonDialog;
import com.cantv.media.center.share.dialog.DeviceAddDialog;
import com.cantv.media.center.share.dialog.DeviceAddDialog.OnIpConfirmedListener;
import com.cantv.media.center.share.dialog.DeviceLoginDialog;
import com.cantv.media.center.share.dialog.DeviceLoginDialog.OnLoginListener;
import com.cantv.media.center.dialog.LoadingDialog;
import com.cantv.media.center.directory.ui.GridViewActivity;
import com.cantv.media.center.share.cybergarage.FileItem;
import com.cantv.media.center.share.cybergarage.FileServer;
import com.cantv.media.center.share.cybergarage.ScanSambaTask;
import com.cantv.media.center.utils.NetworkUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.StatisticsUtil;
import com.cantv.media.center.utils.ToastUtils;
import com.cantv.media.center.share.cybergarage.ScanSambaTask.IScanFileListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;

/**
 * 添加共享设备页面
 */
public class DeviceShareActivity extends Activity implements OnFocusChangeListener {

    private TextView mNetNameTv;
    private TextView mNetIpTv;
    private LinearLayout mScrollView;
    private LinearLayout mDeviceItemGroup;
    private DeviceShareItemView mAddDeviceView;
    private DeviceAddDialog mAddDeviceDialog;
    private DeviceLoginDialog mLoginDeviceDialog;
    private LoadingDialog mLoadingDialog;

    private int[] mDeviceViewBgRes = new int[]{R.drawable.bj_01, R.drawable.bj_02, R.drawable.bj_03, R.drawable.bj_04, R.drawable.bj_05, R.drawable.bj_06, R.drawable.bj_07, R.drawable.bj_08};
    private LinkedList<DeviceInfo> mDeviceInfos;
    private LinkedList<DeviceShareItemView> mDeviceViews;

    private BroadcastReceiver mNetChangeReceiver;
    private IntentFilter mNetChangeIntentFilter;
    private FocusUtils mFocusUtils;
    private FocusScaleUtils mFocusScaleUtils;
    private FileServer mFileServer;
    private CheckNetAccessTask mCheckNetAccessTask;
    private ScanSambaTask mScanSambaTask;

    private boolean isFirst = true;
    private Drawable mBlurDrawable;
    private CommonDialog mCommonDialog;
    private Bitmap mScreenShot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.home_title);
        getWindow().getDecorView().setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        setContentView(R.layout.activity_device_share);
        initUI();
        initData();
        MyApplication.addActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        regNetChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticsUtil.registerResume(this);
        updatePageNetInfo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst && hasFocus) {
            isFirst = false;
            mAddDeviceView.requestFocus();
        }
    }

    @Override
    protected void onPause() {
        StatisticsUtil.registerPause(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mNetChangeReceiver);
        super.onStop();
    }

    @Override
    public void finish() {
        if (null != mScanSambaTask) {
            mScanSambaTask.cancel(true);
            mScanSambaTask = null;
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        mNetChangeReceiver = null;
        mNetChangeIntentFilter = null;
        hideAddDeviceDialog();
        mAddDeviceDialog = null;
        hideLoginDeviceDialog();
        mLoginDeviceDialog = null;
        hideLoadingDialog();
        mLoadingDialog = null;
        mFileServer.release();
        if (mCheckNetAccessTask != null) {
            mCheckNetAccessTask.cancel(true);
            mCheckNetAccessTask = null;
        }
        if (null != mBlurDrawable) {
            mBlurDrawable.setCallback(null);
            mBlurDrawable = null;
        }
        MyApplication.removeActivity(this);
        super.onDestroy();
        if (mFocusUtils != null) {
            mFocusUtils.release();
        }

        mFocusUtils = null;
//        MyApplication.getRefWatcher(this.getApplicationContext()).watch(this);
    }

    private void initUI() {
        //修复OS-3929偶现文件管理器共享内添加设备后出现焦点变形（出现一次）
        mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus_full_content);
        mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.05f, null, null);
        mNetNameTv = (TextView) findViewById(R.id.tv_net_name);
        mNetIpTv = (TextView) findViewById(R.id.tv_net_ip);
        mScrollView = (LinearLayout) findViewById(R.id.hsv_device_list);
        mDeviceItemGroup = (LinearLayout) findViewById(R.id.ll_device_list);
        mAddDeviceView = (DeviceShareItemView) mDeviceItemGroup.getChildAt(0);
        mAddDeviceView.setBackgroundResource(getRandomBgRes());
        mAddDeviceView.setOnFocusChangeListener(this);
        getShareGuideDialog();
        mAddDeviceView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showAddDeviceDialog();
            }
        });
    }

    private void initData() {
        mDeviceInfos = new LinkedList<>();
        mDeviceViews = new LinkedList<>();
        mFileServer = new FileServer();
        mFileServer.start();

        String linkHostList = SharedPreferenceUtil.getLinkHostList();
        if (null != linkHostList && linkHostList.trim().length() > 1) {
            String[] ipList = linkHostList.split("abc");
            List<String> ips = new ArrayList<>();
            for (String ip : ipList) {
                ips.add(ip);
            }
            checkIPAccess(ips, true);

        }
    }

    private void regNetChangeReceiver() {
        if (mNetChangeReceiver == null) {
            mNetChangeReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    updatePageNetInfo();
                }
            };
            mNetChangeIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        }
        registerReceiver(mNetChangeReceiver, mNetChangeIntentFilter);
    }

    private void updatePageNetInfo() {
        NetworkInfo netInfo = NetworkUtils.getNetInfo(this);
        if (netInfo != null && netInfo.isConnected()) {
            int type = netInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                mNetNameTv.setText(getString(R.string.net_name) + NetworkUtils.getWifiName(this));
                mNetIpTv.setText(getString(R.string.ip_) + NetworkUtils.getWiFiIp(this));
            } else {
                mNetNameTv.setText(getString(R.string.net_name) + getString(R.string.local_connection));
                mNetIpTv.setText(getString(R.string.ip_) + NetworkUtils.getEthernetIp(this));
            }
        } else {
            mNetNameTv.setText(getString(R.string.no_connection));
            mNetIpTv.setText("");
        }
    }

    private void addDeviceItemView(final DeviceInfo info) {
        if (null == info) return;

        boolean b = SharedPreferenceUtil.saveLinkHost(info.getIp());
        if (b) ToastUtils.showMessage(MyApplication.getContext(), "IP保存成功");

        final DeviceShareItemView view = new DeviceShareItemView(this);
        view.setViewType(DeviceShareItemView.TYPE_DEVICE);
        view.setIp(info.getIp());
        view.setBackgroundResource(getRandomBgRes());
        view.setTag(info);
        view.setOnFocusChangeListener(this);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String shareUserInfo = SharedPreferenceUtil.getShareUserInfo();
                if (!shareUserInfo.equals("")) {
                    String[] split = shareUserInfo.split("   ");
                    String ip = split[0];
                    if (ip.equals(info.getIp())) {
                        zjLogin(info, split[1], split[2]);
                    } else {
                        showLoginDeviceDialog(info);
                    }
                } else {
                    showLoginDeviceDialog(info);
                }
            }
        });
        LayoutParams layoutParams = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.px300), getResources().getDimensionPixelSize(R.dimen.px450));
        layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.px80);
        mDeviceItemGroup.addView(view, 0, layoutParams);
        mDeviceInfos.add(info);
        mDeviceViews.add(view);
        mAddDeviceView.setFocusable(true);
        mAddDeviceView.requestFocus();
        //在addDeviceItemView 删除延时操作
        //修复OS-2252	【Launcher V5.1.11 偶现：复现率 1/10】选导航栏应用，进入文件管理，点击进入文件共享，当已有设备连接时，焦点异常。
    }

    private int getRandomBgRes() {
        return mDeviceViewBgRes[new Random().nextInt(mDeviceViewBgRes.length)];
    }

    @Override
    public void onFocusChange(final View v, final boolean hasFocus) {
        if (null == mFocusScaleUtils) {
            return;
        }
        if (hasFocus) {
//            if (v == mDeviceItemGroup.getChildAt(0)) {
//                mScrollView.smoothScrollTo(0, 0);
//            } else if (v == mDeviceItemGroup.getChildAt(mDeviceItemGroup.getChildCount() - 1)) {
//                mScrollView.smoothScrollTo(v.getLeft() + getResources().getDimensionPixelSize(R.dimen.px15), 0);
//            }
            mFocusScaleUtils.scaleToLarge(v);
            mFocusUtils.startMoveFocus(v, true, 1.065F, -1f, 0.5f);
        } else {
            mFocusScaleUtils.scaleToNormal(v);
        }
    }

    // <-- addDevice
    public void showAddDeviceDialog() {
        //修复OS-4040TV端未连接网络，进入文件管理，文件共享中，点击添加设备，提示文管理停止件运行
        //修复OS-4439文件共享输入错误的账号密码点击确定弹出提示后，按设置键进入设置菜单，按返回退出时提示文件管理已停止运行
        if (!NetworkUtils.isNetworkAvailable(this)) {
            ToastUtils.showMessage(MyApplication.mContext, getString(R.string.connection_fail), Toast.LENGTH_LONG);
            return;
        }
        mAddDeviceDialog = new DeviceAddDialog(this);
        mAddDeviceDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
//                ((DeviceAddDialog) dialog).updateBackground(R.color.blue_00b7ee);
                ((DeviceAddDialog) dialog).reset();
            }
        });
        mAddDeviceDialog.setOnIpConfirmedListener(new OnIpConfirmedListener() {

            @Override
            public void onConfirmed(final String ip) {
                final String host;
                if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(host = resolve(ip))) {
                    ToastUtils.showMessage(MyApplication.mContext, getString(R.string.ip_err_tips), Toast.LENGTH_LONG);
                    return;
                }
                if (indexOfAddedDevices(host) != -1) {
                    ToastUtils.showMessage(MyApplication.mContext, getString(R.string.devices_has_added), Toast.LENGTH_LONG);
                    return;
                }
                List<String> strings = new ArrayList<>();
                strings.add(ip);
                checkIPAccess(strings, false);
            }
        });
        mAddDeviceDialog.show();
    }

    public void hideAddDeviceDialog() {
        if (mAddDeviceDialog != null) {
            mAddDeviceDialog.dismiss();
        }
    }

    protected String resolve(String ip) {
        ip = ip.replace("\n", "").trim();
        if (Pattern.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$", ip)) {
            return ip;
        }

        try {
            return new URL(ip).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnNetCheckCallback {
        void onStartCheck();

        void onGetResult(List<String> ips);
    }

    public class CheckNetAccessTask extends AsyncTask<Void, Void, List<String>> {

        private OnNetCheckCallback callback;
        private List<String> mIPList;

        public CheckNetAccessTask(OnNetCheckCallback callback, List<String> ipList) {
            this.callback = callback;
            this.mIPList = ipList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null) {
                callback.onStartCheck();
            }
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            ArrayList<String> strings = new ArrayList<>();
            for (final String ip : mIPList) {
                if (null == ip && ip.trim().equals("")) {
                    continue;
                }
                if (NetworkUtils.ping(ip)) {
                    strings.add(ip);
                }
            }

            return strings;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            if (callback != null) {
                callback.onGetResult(result);
            }
        }
    }

    protected int indexOfAddedDevices(String ip) {
        int index = -1;
        int i = 0;
        for (DeviceInfo info : mDeviceInfos) {
            if (info.getIp().equals(ip)) {
                index = i;
                break;
            }
            i++;
        }
        return index;
    }

    protected void startCheckIpAccess(List<String> ipList, OnNetCheckCallback onNetCheckResultCallback) {
        if (mCheckNetAccessTask != null) {
            mCheckNetAccessTask.cancel(true);
            mCheckNetAccessTask = null;
        }
        mCheckNetAccessTask = new CheckNetAccessTask(onNetCheckResultCallback, ipList);
        mCheckNetAccessTask.execute();
    }

    // --> addDevice

    // <-- loginDevice
    public void showLoginDeviceDialog(final DeviceInfo deviceInfo) {
        mLoginDeviceDialog = new DeviceLoginDialog(this);
        mLoginDeviceDialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
//                ((DeviceLoginDialog) dialog).updateBackground(R.color.blue_00b7ee);
                ((DeviceLoginDialog) dialog).reset();
            }
        });
        mLoginDeviceDialog.setOnLoginListener(new OnLoginListener() {

            @Override
            public void onLogin(String userName, String password) {
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                    ToastUtils.showMessage(MyApplication.mContext, getString(R.string.username_pwd_err), Toast.LENGTH_SHORT);
                    return;
                }

                zjLogin(deviceInfo, userName, password);

            }
        });
        mLoginDeviceDialog.refreshData(deviceInfo.getUserName(), deviceInfo.getPassword());
        mLoginDeviceDialog.show();
    }

    /**
     * 不用输入用户名密码,直接登录
     */
    private void zjLogin(DeviceInfo deviceInfo, String userName, String password) {
        deviceInfo.setUserName(userName.trim());
        deviceInfo.setPassword(password.trim());
        String ipVal = new StringBuilder(deviceInfo.getUserName()).append(":").append(deviceInfo.getPassword()).append("@").append(deviceInfo.getIp()).toString();
        deviceInfo.setFileItem(new FileItem(ipVal, "smb://" + ipVal + "/", false));
        loginDevice(deviceInfo);
    }

    public void hideLoginDeviceDialog() {
        if (mLoginDeviceDialog != null) {
            mLoginDeviceDialog.dismiss();
        }
    }

    protected void loginDevice(final DeviceInfo deviceInfo) {
        if (mScanSambaTask != null) {
            mScanSambaTask.cancel(true);
            mScanSambaTask = null;
        }
        showLoadingDialog();
        mScanSambaTask = new ScanSambaTask(false, new IScanFileListener() {

            @Override
            public void onSuccess(ArrayList<SmbFile> list) {
                ToastUtils.showMessage(MyApplication.mContext, getString(R.string.login_succ), Toast.LENGTH_SHORT);
                getWindow().getDecorView().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        hideLoadingDialog();
                        Intent intent = new Intent(DeviceShareActivity.this, GridViewActivity.class);
                        intent.putExtra("type", "share");
                        intent.putExtra("title", deviceInfo.getUserName() + " (" + deviceInfo.getIp() + ")");
                        intent.putExtra("path", deviceInfo.getFileItem().getPath());
                        startActivity(intent);

                        if (null != mLoginDeviceDialog) {
                            mLoginDeviceDialog.dismiss();
                        }

                        //用空格分开是因为用户名,密码不会有空格
                        String info = deviceInfo.getIp() + "   " + deviceInfo.getUserName() + "   " + deviceInfo.getPassword();

                        SharedPreferenceUtil.saveShareUserInfo(info);

                    }
                }, 1000);
            }

            @Override
            public void onLoginFailed() {
                hideLoadingDialog();
                if (null == mLoginDeviceDialog || !mLoginDeviceDialog.isShowing()) {
                    showLoginDeviceDialog(deviceInfo);
                }
                ToastUtils.showMessage(MyApplication.mContext, getString(R.string.username_pwd_err), Toast.LENGTH_SHORT);
            }

            @Override
            public void onException(Throwable ta) {
                hideLoadingDialog();
                if (null == mLoginDeviceDialog || !mLoginDeviceDialog.isShowing()) {
                    showLoginDeviceDialog(deviceInfo);
                }
                ToastUtils.showMessage(MyApplication.mContext, getString(R.string.device_login_failed), Toast.LENGTH_SHORT);
            }

        });
        mScanSambaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceInfo.getFileItem().getPath());
    }

    // --> loginDevice
    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mScanSambaTask != null) {
                        mScanSambaTask.cancel(true);
                    }
                    if (mCheckNetAccessTask != null) {
                        mCheckNetAccessTask.cancel(true);
                    }
                }
            });
        }
        mLoadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    private Bitmap getScreenShot() {
        View view = getWindow().getDecorView();
        view.buildDrawingCache();
        getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bm = Bitmap.createBitmap(view.getDrawingCache());
        getWindow().getDecorView().setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        return bm;
    }

    /**
     * 链接到指定IP地址
     *
     * @param ips
     * @param isFirst
     */
    private void checkIPAccess(final List<String> ips, final boolean isFirst) {
        startCheckIpAccess(ips, new OnNetCheckCallback() {
            @Override
            public void onStartCheck() {
                showLoadingDialog();
            }

            @Override
            public void onGetResult(List<String> resultIPs) {
                if (resultIPs.size() > 0) {
                    if (null != mAddDeviceDialog) {
                        mAddDeviceDialog.dismiss();
                    }
                    mFocusUtils.hideFocus();
                    for (String ip : resultIPs) {
                        addDeviceItemView(new DeviceInfo(ip));
                    }
                    //OS-2252	【Launcher V5.1.11 偶现：复现率 1/10】选导航栏应用，进入文件管理，点击进入文件共享，当已有设备连接时，焦点异常。
                    mDeviceItemGroup.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onFocusChange(mAddDeviceView, true);
                            mFocusUtils.showFocus();
//                            mFocusUtils.hideFocusForStartMove(1000);
                        }
                    }, 200);
                    //OS-2252	【Launcher V5.1.11 偶现：复现率 1/10】选导航栏应用，进入文件管理，点击进入文件共享，当已有设备连接时，焦点异常。
                } else {
                    if (!isFirst) {
                        ToastUtils.showMessage(MyApplication.mContext, getString(R.string.devices_not_found), Toast.LENGTH_SHORT);
                    }
                    mAddDeviceView.setFocusable(true);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingDialog();
                    }
                }, 100);
            }
        });
    }

    /**
     * 共享向导
     */
    private void getShareGuideDialog() {
        int shareGuide = SharedPreferenceUtil.getShareGuide();
        if (shareGuide == 0) {
            if (mCommonDialog == null) {
                mCommonDialog = new CommonDialog(this);
                mCommonDialog.setTitle(getString(R.string.device_share_title)).setContent(getString(R.string.device_share_content), 0).setButtonContent(getString(R.string.device_share_ok), getString(R.string.device_share_cancel));
                mCommonDialog.setOnClickableListener(new CommonDialog.OnClickableListener() {
                    @Override
                    public void onConfirmClickable() {
                        SharedPreferenceUtil.setShareGuide(0);
                        mScrollView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelClickable() {
                        SharedPreferenceUtil.setShareGuide(1);
                        mScrollView.setVisibility(View.VISIBLE);
                    }
                });
            }
            mCommonDialog.setCancel(false);
            mCommonDialog.show();
        } else {
            mScrollView.setVisibility(View.VISIBLE);
            String linkHostList = SharedPreferenceUtil.getLinkHostList();
            if (TextUtils.isEmpty(linkHostList)) {
                mAddDeviceView.setFocusable(true);
            } else {
                mAddDeviceView.setFocusable(false);
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
