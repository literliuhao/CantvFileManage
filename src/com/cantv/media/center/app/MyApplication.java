package com.cantv.media.center.app;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

public class MyApplication extends Application {
	public static Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
	}

}
