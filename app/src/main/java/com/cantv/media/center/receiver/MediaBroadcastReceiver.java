package com.cantv.media.center.receiver;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Toast;

import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.widgets.CustomDialog;

import java.util.List;

public class MediaBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MediaBroadcastReceiver";
    private Context mContext;
    private Dialog dialog = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String path = intent.getData().getPath();
            //保存路径到本地
            SharedPreferenceUtil.saveDevice(path);
            showMountedDialog();
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            //添加移除U盘提示
            if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
                Toast.makeText(MyApplication.getContext(), mContext.getResources().getString(R.string.device_remove), Toast.LENGTH_SHORT).show();
            }
            List<String> currPathList = MediaUtils.getCurrPathList();
            if (currPathList.size() < 1) {
                if (null != dialog && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }
    }

    private boolean isShow() {
        if (null != dialog) {
            return dialog.isShowing() ? true : false;
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

}
