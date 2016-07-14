package com.cantv.media.center.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cantv.media.center.service.BootDialogService;

public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("BootCompletedReceiver", "BootCompletedReceiver........... >>>>>>>>>>>>>>>>>>>> ");
		Intent intentStart = new Intent();
		intentStart.setAction("com.cantv.service.RECEIVER_START");
		intentStart.setClass(context, BootDialogService.class);
		context.startService(intentStart);
	}
}