package com.cantv.media.center.ui.upgrade;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Button;

import com.cantv.media.R;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.download.DownloadListener;
import com.tencent.bugly.beta.download.DownloadTask;

import java.util.ArrayList;

public class UpgradeManager {

    private static UpgradeManager mInstance = null;
    private Context mContext;
    private String mTargeVersion;
    private String mUpgradeApkPath;

    //版本更新的一些信息，数据源
    private ArrayList<String> mUpgradeInfoList;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            show();
        }
    };

    private UpgradeManager(final Context context) {
        super();
        this.mContext = context.getApplicationContext();
    }

    public static UpgradeManager getIntance(final Context context) {
        if (mInstance == null) {
            synchronized (UpgradeManager.class) {
                if (mInstance == null) {
                    mInstance = new UpgradeManager(context);
                }
            }
        }
        return mInstance;
    }

    public void init() {
        if (mUpgradeInfoList == null) {
            mUpgradeInfoList = new ArrayList<>();
        }
        mUpgradeInfoList.clear();
        mUpgradeInfoList.add(Beta.getUpgradeInfo().newFeature);
        mTargeVersion = Beta.getUpgradeInfo().versionName;
//        show();
        mHandler.sendEmptyMessage(0);
    }

    public UpgradeManager() {
        if (mUpgradeInfoList == null) {
            mUpgradeInfoList = new ArrayList<>();
        }
        mUpgradeInfoList.clear();
        mUpgradeInfoList.add(Beta.getUpgradeInfo().newFeature);
        mTargeVersion = Beta.getUpgradeInfo().versionName;
    }

    public UpgradeManager(final Context context, String upgradeVersion, ArrayList<String> list, String path) {
        super();
        this.mContext = context;
        this.mUpgradeInfoList = list;
        mTargeVersion = upgradeVersion;
        mUpgradeApkPath = path;
    }

    public void show() {
        Dialog dialog = null;
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);

        customBuilder.setTitle("软件版本更新")
                .setNewcode("新版本：" + mTargeVersion)
                .setList(mUpgradeInfoList)
                .setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadTask task = Beta.startDownload();
                        Beta.registerDownloadListener(new DownloadListener() {
                            @Override
                            public void onReceive(DownloadTask task) {
                            }

                            @Override
                            public void onCompleted(DownloadTask task) {
//                                ToastUtils.showMessage(mContext,"下载完成，请安装");
                            }

                            @Override
                            public void onFailed(DownloadTask task, int code, String extMsg) {
                            }
                        });
                        dialog.dismiss();
                    }
                });

        dialog = customBuilder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setCancelable(true);
        dialog.show();
        Button btn = (Button) dialog.findViewById(R.id.positiveButton);
        btn.requestFocus();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Beta.unregisterDownloadListener();
            }
        });
    }

//    private void installApk(String uri) {
//        Intent installIntent = new Intent(Intent.ACTION_VIEW);
//        installIntent.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
//        installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        mContext.startActivity(installIntent);
//    }

}
