package com.cantv.media.center.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.cantv.liteplayer.core.interfaces.IMediaListener;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.SystemCateUtil;
import com.cantv.media.center.ui.dialog.CustomDialog;

import java.util.ArrayList;
import java.util.List;

public class MediaBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MediaBroadcastReceiver";
    private Context mContext;
    private static CustomDialog dialog;

    private static List<IMediaListener> mediaListener = new ArrayList<>();

    public MediaBroadcastReceiver() {
    }

    private static MediaBroadcastReceiver mediaBroadcastReceiver;

    public static MediaBroadcastReceiver getInstance() {
        if (null == mediaBroadcastReceiver) {
            mediaBroadcastReceiver = new MediaBroadcastReceiver();
        }
        return mediaBroadcastReceiver;
    }

    public void addListener(IMediaListener iMediaListener) {
        mediaListener.add(iMediaListener);
    }

    public void removeListener(IMediaListener iMediaListener) {
        mediaListener.remove(iMediaListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            for (int i = 0; i < mediaListener.size(); i++) {
                mediaListener.get(i).onMounted(intent);
            }

            Log.i("MediaBroadcastReceiver", SystemCateUtil.getPersist());
            String path = intent.getData().getPath();
            //保存路径到本地
            SharedPreferenceUtil.saveDevice(path);
            //老化模式下不弹出U盘提示
            if (!SystemCateUtil.getPersist().equals("1")) {
                showMountedDialog();
            }
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            for (int i = 0; i < mediaListener.size(); i++) {
                mediaListener.get(i).onUnmounted(intent);
            }
            List<String> currPathList = MediaUtils.getCurrPathList();
            if (currPathList.size() < 1) {
                if (isShow()) {
                    dialog.dismiss();
                }
            }
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
            //添加移除U盘提示
            Toast.makeText(MyApplication.getContext(), mContext.getResources().getString(R.string.device_remove), Toast.LENGTH_SHORT).show();
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
