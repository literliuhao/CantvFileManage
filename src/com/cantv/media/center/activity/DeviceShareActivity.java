package com.cantv.media.center.activity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.data.DeviceInfo;
import com.cantv.media.center.ui.DeviceAddDialog;
import com.cantv.media.center.ui.DeviceAddDialog.OnIpConfirmedListener;
import com.cantv.media.center.ui.DeviceLoginDialog;
import com.cantv.media.center.ui.DeviceLoginDialog.OnLoginListener;
import com.cantv.media.center.ui.DeviceShareItemView;
import com.cantv.media.center.utils.BitmapUtils;
import com.cantv.media.center.utils.NetworkUtils;
import com.cantv.media.center.utils.ToastUtils;
import com.cantv.media.center.utils.cybergarage.FileServer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceShareActivity extends Activity implements OnFocusChangeListener {

	private TextView mNetNameTv;
	private TextView mNetIpTv;
	private LinearLayout mDeviceItemGroup;
	private DeviceShareItemView mAddDeviceView;
	private DeviceAddDialog mAddDeviceDialog;
	private DeviceLoginDialog mLoginDeviceDialog;

	private BroadcastReceiver mNetChangeReceiver;
	private IntentFilter mNetChangeIntentFilter;

	private LinkedList<DeviceInfo> mDevices;
	private LinkedList<DeviceShareItemView> mDeviceViews;
	private int[] mDeviceViewBgRes = new int[] { R.drawable.bj_01, R.drawable.bj_02, R.drawable.bj_03, R.drawable.bj_04,
			R.drawable.bj_05, R.drawable.bj_06, R.drawable.bj_07, R.drawable.bj_08 };
	private FocusUtils mFocusUtils;
	private FocusScaleUtils mFocusScaleUtils;
	private boolean isFirst = true;
	private FileServer mFileServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(R.drawable.home_title);
		getWindow().getDecorView().setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
		getWindow().getDecorView().setDrawingCacheEnabled(true);
		setContentView(R.layout.activity_device_share);
		initUI();
		initData();
		addDeviceItemView(new DeviceInfo("192.168.1.55"));
		addDeviceItemView(new DeviceInfo("192.168.4.233"));
		addDeviceItemView(new DeviceInfo("192.168.56.32"));
		addDeviceItemView(new DeviceInfo("192.168.0.8"));
	}

	@Override
	protected void onStart() {
		super.onStart();
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

	@Override
	protected void onResume() {
		super.onResume();
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
	protected void onStop() {
		unregisterReceiver(mNetChangeReceiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mNetChangeReceiver = null;
		mNetChangeIntentFilter = null;
		hideAddDeviceDialog();
		mAddDeviceDialog = null;
		hideLoginDeviceDialog();
		mLoginDeviceDialog = null;
		mFileServer.release();
		super.onDestroy();
	}

	private void initUI() {
		mNetNameTv = (TextView) findViewById(R.id.tv_net_name);
		mNetIpTv = (TextView) findViewById(R.id.tv_net_ip);
		mDeviceItemGroup = (LinearLayout) findViewById(R.id.ll_device_list);
		mAddDeviceView = (DeviceShareItemView) mDeviceItemGroup.getChildAt(0);
		mAddDeviceView.setBackgroundResource(getRandomBgRes());
		mAddDeviceView.setOnFocusChangeListener(this);
		mAddDeviceView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showAddDeviceDialog();
			}
		});
		mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus_full_content);
		mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.05f, null, null);
	}

	private void initData() {
		mDevices = new LinkedList<DeviceInfo>();
		mDeviceViews = new LinkedList<DeviceShareItemView>();
		mFileServer = new FileServer();
		mFileServer.start();
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
		if (info == null) {
			return;
		}
		DeviceShareItemView view = new DeviceShareItemView(this);
		view.setViewType(DeviceShareItemView.TYPE_DEVICE);
		view.setIp(info.getIp());
		view.setBackgroundResource(getRandomBgRes());
		view.setTag(info);
		view.setOnFocusChangeListener(this);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLoginDeviceDialog(info);
			}
		});
		LayoutParams layoutParams = new LinearLayout.LayoutParams(
				getResources().getDimensionPixelSize(R.dimen.dimen_300px),
				getResources().getDimensionPixelSize(R.dimen.dimen_450px));
		layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.dimen_80px);
		mDeviceItemGroup.addView(view, 0, layoutParams);
		mDevices.add(info);
		mDeviceViews.add(view);
	}

	private int getRandomBgRes() {
		return mDeviceViewBgRes[new Random().nextInt(mDeviceViewBgRes.length)];
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			mFocusScaleUtils.scaleToLarge(v);
			mFocusUtils.startMoveFocus(v, true, 1.02F, -1f, 0.5f);
		} else {
			mFocusScaleUtils.scaleToNormal(v);
		}
	}

	public void showAddDeviceDialog() {
		if (mAddDeviceDialog == null) {
			mAddDeviceDialog = new DeviceAddDialog(this);
			mAddDeviceDialog.setOnShowListener(new OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {
					((DeviceAddDialog) dialog).reset();
					getWindow().getDecorView().setDrawingCacheEnabled(false);
					getWindow().getDecorView().setDrawingCacheEnabled(true);
					((DeviceAddDialog) dialog).updateBackground(BitmapUtils
							.blurBitmap(getWindow().getDecorView().getDrawingCache(), DeviceShareActivity.this));
				}
			});
			mAddDeviceDialog.setOnIpConfirmedListener(new OnIpConfirmedListener() {

				@Override
				public void onConfirmed(String ip) {
					if (TextUtils.isEmpty(ip)) {
						ToastUtils.showMessage(DeviceShareActivity.this, "请输入正确的ip地址", Toast.LENGTH_SHORT);
						return;
					}
					if (!validIP(ip)) {
						ToastUtils.showMessage(DeviceShareActivity.this, "请输入正确的ip地址", Toast.LENGTH_SHORT);
						return;
					}
					//TODO
					ToastUtils.showMessage(DeviceShareActivity.this, "ip正确", Toast.LENGTH_SHORT);
				}
			});
		}
		mAddDeviceDialog.show();
	}

	public void hideAddDeviceDialog() {
		if (mAddDeviceDialog != null) {
			mAddDeviceDialog.dismiss();
		}
	}

	public void showLoginDeviceDialog(DeviceInfo deviceInfo) {
		if (mLoginDeviceDialog == null) {
			mLoginDeviceDialog = new DeviceLoginDialog(this);
			mLoginDeviceDialog.setOnShowListener(new OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {
					((DeviceLoginDialog) dialog).reset();
					getWindow().getDecorView().setDrawingCacheEnabled(false);
					getWindow().getDecorView().setDrawingCacheEnabled(true);
					((DeviceLoginDialog) dialog).updateBackground(BitmapUtils
							.blurBitmap(getWindow().getDecorView().getDrawingCache(), DeviceShareActivity.this));
				}
			});
			mLoginDeviceDialog.setOnLoginListener(new OnLoginListener() {

				@Override
				public void onLogin(String userName, String password) {
					if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
						ToastUtils.showMessage(DeviceShareActivity.this, "请输入正确的用户名或密码", Toast.LENGTH_SHORT);
						return;
					}
					//TODO
					ToastUtils.showMessage(DeviceShareActivity.this,
							"userName = " + userName + ", password = " + password, Toast.LENGTH_SHORT);
				}
			});
		}
		mLoginDeviceDialog.show();
	}

	public void hideLoginDeviceDialog() {
		if (mLoginDeviceDialog != null) {
			mLoginDeviceDialog.dismiss();
		}
	}

	protected boolean validIP(String ip) {
		try {
			new URL(ip);
			return true;
		} catch (MalformedURLException e) {
		}
		return false;
	}

}
