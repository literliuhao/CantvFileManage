package com.cantv.media.center.index.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.cantv.liteplayer.core.interfaces.IMediaListener;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.index.dialog.DialogActivity;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.SystemCateUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MediaBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MediaBroadcastReceiver";
    private Context mContext;
    private final int minNumber = 1;

    public MediaBroadcastReceiver() {
    }

    private static MediaBroadcastReceiver mediaBroadcastReceiver;
    private static IMediaListener iMediaListener;

    public static MediaBroadcastReceiver getInstance() {
        if (null == mediaBroadcastReceiver) {
            mediaBroadcastReceiver = new MediaBroadcastReceiver();
        }
        return mediaBroadcastReceiver;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        mContext = context;
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            EventBus.getDefault().post(new UsbMounted(false, intent.getDataString()));
            final String path = intent.getData().getPath();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //保存路径到本地
                    SharedPreferenceUtil.saveDevice(path);
                    //老化模式下不弹出U盘提示
                    if (!SystemCateUtil.getPersist().equals("1")) {
                        showMountedDialog();
                    }
                }
            }).start();
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            EventBus.getDefault().post(new UsbMounted(true, intent.getDataString()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<String> currPathList = MediaUtils.getCurrPathList();
                    if (currPathList.size() < minNumber) {
                        if (null != iMediaListener) {
                            iMediaListener.onFinish();
                        }
                    }
                }
            }).start();
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
            EventBus.getDefault().post(new UsbMounted(true, intent.getDataString()));
            //添加移除U盘提示
            Toast.makeText(MyApplication.getContext(), mContext.getResources().getString(R.string.device_remove), Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
            EventBus.getDefault().post(new UsbMounted(true, intent.getDataString()));
        }
    }

    public void setListener(IMediaListener mediaListener) {
        iMediaListener = mediaListener;
    }

    private void showMountedDialog() {
        Intent intent = new Intent(mContext, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
