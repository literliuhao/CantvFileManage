package com.cantv.media.center.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.SystemCateUtil;
import com.cantv.media.center.ui.dialog.CustomDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MediaBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MediaBroadcastReceiver";
    private Context mContext;
    private static CustomDialog dialog;

    public MediaBroadcastReceiver() {
    }

    private static MediaBroadcastReceiver mediaBroadcastReceiver;

    public static MediaBroadcastReceiver getInstance() {
        if (null == mediaBroadcastReceiver) {
            mediaBroadcastReceiver = new MediaBroadcastReceiver();
        }
        return mediaBroadcastReceiver;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            EventBus.getDefault().post(new UsbMounted(false, intent.getDataString()));

            Log.i("MediaBroadcastReceiver", SystemCateUtil.getPersist());
            String path = intent.getData().getPath();
            //保存路径到本地
            SharedPreferenceUtil.saveDevice(path);
            //老化模式下不弹出U盘提示
            if (!SystemCateUtil.getPersist().equals("1")) {
                showMountedDialog();
            }
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            EventBus.getDefault().post(new UsbMounted(true, intent.getDataString()));

            List<String> currPathList = MediaUtils.getCurrPathList();
            if (currPathList.size() < 1) {
                if (isShow()) {
                    dialog.dismiss();
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
            EventBus.getDefault().post(new UsbMounted(true, intent.getDataString()));
            //添加移除U盘提示
            Toast.makeText(MyApplication.getContext(), mContext.getResources().getString(R.string.device_remove), Toast.LENGTH_SHORT).show();
        }else if(intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)){
            EventBus.getDefault().post(new UsbMounted(true, intent.getDataString()));
        }
    }

    private boolean isShow() {
        dialog = CustomDialog.getInstance(mContext.getApplicationContext());
        if (null != dialog) {
            return dialog.isShowing();
        } else {
            return false;
        }
    }

    private void showMountedDialog() {
        if (!isShow()) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.setCancelable(true);
            dialog.show();
        }
    }
}
