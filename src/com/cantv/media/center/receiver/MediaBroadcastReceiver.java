package com.cantv.media.center.receiver;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MediaBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = "MediaBroadcastReceiver";
    private boolean mIsUsb = false;
    private boolean mIsTF = false;
    private Context mContext;
    private List<String> mlistusb;
    private String mtfpath;

    @Override
    public void onReceive(Context context, Intent intent) {
    }

}
