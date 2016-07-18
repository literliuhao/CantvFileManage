package com.cantv.media.center.service;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.widgets.CustomDialog;

/**
 * Created by liuhao on 16/7/14.
 */
public class BootDialogService extends Service {

    private Context mContext;
    private Dialog dialog = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("onCreate", "Service onCreate........... >>>>>>>>>>>>>>>>>>>> ");
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("onStartCommand", "Service onStartCommand........... >>>>>>>>>>>>>>>>>>>> ");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
//        filter.addAction("com.cantv.service.MEDIA_MOUNTED");
        filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);
        return START_REDELIVER_INTENT;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                MediaUtils.addUsbRootPaths(intent.getData().getPath());
                showMountedDialog();
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                MediaUtils.removeUsbRootPaths(intent.getData().getPath());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intentStart = new Intent();
        intentStart.setAction("com.cantv.service.RECEIVER_START");
        intentStart.setClass(this, BootDialogService.class);
        this.startService(intentStart);
//        unregisterReceiver(mReceiver);
    }

    private boolean isShow() {
        if (null != dialog) {
            return dialog.isShowing() == true ? true : false;
        } else {
            return false;
        }
    }

    private void showMountedDialog() {
        if (isShow()) return;
        CustomDialog.Builder customBuilder = new CustomDialog.Builder(mContext);
        dialog = customBuilder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
