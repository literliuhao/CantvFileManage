package com.cantv.media.center.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.activity.DeviceShareActivity.CheckNetAccessTask.OnNetCheckCallback;
import com.cantv.media.center.data.DeviceInfo;
import com.cantv.media.center.ui.DeviceAddDialog;
import com.cantv.media.center.ui.DeviceAddDialog.OnIpConfirmedListener;
import com.cantv.media.center.ui.DeviceLoginDialog;
import com.cantv.media.center.ui.DeviceLoginDialog.OnLoginListener;
import com.cantv.media.center.ui.DeviceShareItemView;
import com.cantv.media.center.ui.LoadingDialog;
import com.cantv.media.center.utils.BitmapUtils;
import com.cantv.media.center.utils.NetworkUtils;
import com.cantv.media.center.utils.ToastUtils;
import com.cantv.media.center.utils.cybergarage.FileItem;
import com.cantv.media.center.utils.cybergarage.FileServer;
import com.cantv.media.center.utils.cybergarage.ScanSambaTask;
import com.cantv.media.center.utils.cybergarage.ScanSambaTask.IScanFileListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;

public class DeviceShareActivity extends Activity implements OnFocusChangeListener {

	private TextView mNetNameTv;
	private TextView mNetIpTv;
	private HorizontalScrollView mScrollView;
	private LinearLayout mDeviceItemGroup;
	private DeviceShareItemView mAddDeviceView;
	private DeviceAddDialog mAddDeviceDialog;
	private DeviceLoginDialog mLoginDeviceDialog;
	private LoadingDialog mLoadingDialog;

	private int[] mDeviceViewBgRes = new int[] { R.drawable.bj_01, R.drawable.bj_02, R.drawable.bj_03, R.drawable.bj_04,
			R.drawable.bj_05, R.drawable.bj_06, R.drawable.bj_07, R.drawable.bj_08 };
	private LinkedList<DeviceInfo> mDeviceInfos;
	private LinkedList<DeviceShareItemView> mDeviceViews;

	private BroadcastReceiver mNetChangeReceiver;
	private IntentFilter mNetChangeIntentFilter;
	private FocusUtils mFocusUtils;
	private FocusScaleUtils mFocusScaleUtils;
	private FileServer mFileServer;
	private CheckNetAccessTask mCheckNetAccessTask;
	private ScanSambaTask mScanSambaTask;

	private boolean isFirst = true;
	Drawable mBlurDrawable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(R.drawable.home_title);
		getWindow().getDecorView().setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
		setContentView(R.layout.activity_device_share);
		initUI();
		initData();
	}

	@Override
	protected void onStart() {
		super.onStart();
		regNetChangeReceiver();
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
		mBlurDrawable.setCallback(null);
		mBlurDrawable = null;
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
		hideLoadingDialog();
		mLoadingDialog = null;
		mFileServer.release();
		if (mCheckNetAccessTask != null) {
			mCheckNetAccessTask.cancel(true);
			mCheckNetAccessTask = null;
		}
		super.onDestroy();
	}

	private void initUI() {
		mNetNameTv = (TextView) findViewById(R.id.tv_net_name);
		mNetIpTv = (TextView) findViewById(R.id.tv_net_ip);
		mScrollView = (HorizontalScrollView) findViewById(R.id.hsv_device_list);
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
		mDeviceInfos = new LinkedList<DeviceInfo>();
		mDeviceViews = new LinkedList<DeviceShareItemView>();
		mFileServer = new FileServer();
		mFileServer.start();
	}

	private void regNetChangeReceiver() {
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
		final DeviceShareItemView view = new DeviceShareItemView(this);
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
		mDeviceInfos.add(info);
		mDeviceViews.add(view);
		mDeviceItemGroup.postDelayed(new Runnable() {

			@Override
			public void run() {
				view.requestFocus();
			}
		}, 500);
	}

	private int getRandomBgRes() {
		return mDeviceViewBgRes[new Random().nextInt(mDeviceViewBgRes.length)];
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			if (v == mDeviceItemGroup.getChildAt(0)) {
				mScrollView.smoothScrollTo(0, 0);
			} else if (v == mDeviceItemGroup.getChildAt(mDeviceItemGroup.getChildCount() - 1)) {
				mScrollView.smoothScrollTo(v.getLeft() + getResources().getDimensionPixelSize(R.dimen.dimen_15px), 0);
			}
			mFocusScaleUtils.scaleToLarge(v);
			mFocusUtils.startMoveFocus(v, true, 1.065F, -1f, 0.5f);
		} else {
			mFocusScaleUtils.scaleToNormal(v);
		}
	}

	// <-- addDevice
	public void showAddDeviceDialog() {
		if (mAddDeviceDialog == null) {
			mAddDeviceDialog = new DeviceAddDialog(this);
			mAddDeviceDialog.setOnShowListener(new OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {
					((DeviceAddDialog) dialog).reset();
//					Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.folder_photo);
					mBlurDrawable = BitmapUtils.blurBitmap(getScreenShot(), DeviceShareActivity.this);
					((DeviceAddDialog) dialog)
//					.updateBackground(MyApplication.mContext.getResources().getDrawable(R.drawable.bg));
//							.updateBackground(BitmapUtils.blurBitmap(decodeResource, DeviceShareActivity.this));
							.updateBackground(mBlurDrawable);
//					d.setCallback(null);
				}
			});
			mAddDeviceDialog.setOnIpConfirmedListener(new OnIpConfirmedListener() {

				@Override
				public void onConfirmed(final String ip) {
					final String host;
					if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(host = resolve(ip))) {
						ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.ip_err_tips),
								Toast.LENGTH_LONG);
						return;
					}
					if (indexOfAddedDevices(host) != -1) {
						ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.devices_has_added),
								Toast.LENGTH_LONG);
						return;
					}
					startCheckIpAccess(host, new OnNetCheckCallback() {

						@Override
						public void onStartCheck() {
							showLoadingDialog();
						}

						@Override
						public void onGetResult(boolean success) {
							hideLoadingDialog();
							if (success) {
								mAddDeviceDialog.dismiss();
								addDeviceItemView(new DeviceInfo(host));
							} else {
								ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.devices_not_found),
										Toast.LENGTH_SHORT);
							}
						}
					});
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

	protected String resolve(String ip) {
		ip = ip.replace("\n", "").trim();
		if (Pattern.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$", ip)) {
			return ip;
		}

		try {
			return new URL(ip).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class CheckNetAccessTask extends AsyncTask<String, Void, Boolean> {

		public interface OnNetCheckCallback {
			public void onStartCheck();

			public void onGetResult(boolean success);
		}

		private OnNetCheckCallback callback;

		public CheckNetAccessTask(OnNetCheckCallback callback) {
			super();
			this.callback = callback;
		}

		@Override
		protected void onPreExecute() {
			if (callback != null) {
				callback.onStartCheck();
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return NetworkUtils.ping(params[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (callback != null) {
				callback.onGetResult(result);
			}
		}
	}

	protected int indexOfAddedDevices(String ip) {
		int index = -1;
		int i = 0;
		for (DeviceInfo info : mDeviceInfos) {
			if (info.getIp().equals(ip)) {
				index = i;
				break;
			}
			i++;
		}
		return index;
	}

	protected void startCheckIpAccess(String ip, OnNetCheckCallback onNetCheckResultCallback) {
		if (mCheckNetAccessTask != null) {
			mCheckNetAccessTask.cancel(true);
			mCheckNetAccessTask = null;
		}
		mCheckNetAccessTask = new CheckNetAccessTask(onNetCheckResultCallback);
		mCheckNetAccessTask.execute(ip);
	}
	// --> addDevice

	// <-- loginDevice
	public void showLoginDeviceDialog(final DeviceInfo deviceInfo) {
		if (mLoginDeviceDialog == null) {
			mLoginDeviceDialog = new DeviceLoginDialog(this);
			mLoginDeviceDialog.setOnShowListener(new OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {
					((DeviceLoginDialog) dialog).reset();
//					Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.folder_photo);
					((DeviceLoginDialog) dialog)
//							.updateBackground(BitmapUtils.blurBitmap(getScreenShot(), DeviceShareActivity.this));
//					.updateBackground(MyApplication.mContext.getResources().getDrawable(R.drawable.bg));
							.updateBackground(mBlurDrawable);
//					.updateBackgroundColor(MyApplication.mContext.getResources().getColor(R.color.per50_white));
				}
			});
			mLoginDeviceDialog.setOnLoginListener(new OnLoginListener() {

				@Override
				public void onLogin(String userName, String password) {
					if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
						ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.username_pwd_err),
								Toast.LENGTH_SHORT);
						return;
					}
					deviceInfo.setUserName(userName.trim());
					deviceInfo.setPassword(password.trim());
					String ipVal = new StringBuilder(deviceInfo.getUserName()).append(":")
							.append(deviceInfo.getPassword()).append("@").append(deviceInfo.getIp()).toString();
					deviceInfo.setFileItem(new FileItem(ipVal, "smb://" + ipVal + "/", false));
					loginDevice(deviceInfo);
				}
			});
		}
		mLoginDeviceDialog.refreshData(deviceInfo.getUserName(), deviceInfo.getPassword());
		mLoginDeviceDialog.show();
	}

	public void hideLoginDeviceDialog() {
		if (mLoginDeviceDialog != null) {
			mLoginDeviceDialog.dismiss();
		}
	}

	protected void loginDevice(final DeviceInfo deviceInfo) {
		if (mScanSambaTask != null) {
			mScanSambaTask.cancel(true);
			mScanSambaTask = null;
		}
		showLoadingDialog();
		mScanSambaTask = new ScanSambaTask(false, new IScanFileListener() {

			@Override
			public void onSuccess(ArrayList<SmbFile> list) {
				ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.login_succ), Toast.LENGTH_SHORT);
				getWindow().getDecorView().postDelayed(new Runnable() {

					@Override
					public void run() {
						hideLoadingDialog();
						Intent intent = new Intent(DeviceShareActivity.this, GridViewActivity.class);
						intent.putExtra("type", "share");
						intent.putExtra("title", deviceInfo.getUserName() + " (" + deviceInfo.getIp() + ")");
						intent.putExtra("path", deviceInfo.getFileItem().getPath());
						startActivity(intent);
					}
				}, 1000);
			}

			@Override
			public void onLoginFailed() {
				hideLoadingDialog();
				ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.username_pwd_err),
						Toast.LENGTH_SHORT);
			}

			@Override
			public void onException(Throwable ta) {
				hideLoadingDialog();
				ToastUtils.showMessage(DeviceShareActivity.this, getString(R.string.device_login_failed),
						Toast.LENGTH_SHORT);
			}

		});
		mScanSambaTask.execute(deviceInfo.getFileItem().getPath());
	}
	// --> loginDevice

	private void showLoadingDialog() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(this);
			mLoadingDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					if (mScanSambaTask != null) {
						mScanSambaTask.cancel(true);
					}
					if (mCheckNetAccessTask != null) {
						mCheckNetAccessTask.cancel(true);
					}
				}
			});
		}
		mLoadingDialog.show();
	}

	private void hideLoadingDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
	}

	private Bitmap getScreenShot() {
		View view = getWindow().getDecorView();
		view.buildDrawingCache();
		getWindow().getDecorView().setDrawingCacheEnabled(true);
		Bitmap bm = Bitmap.createBitmap(view.getDrawingCache());
		getWindow().getDecorView().setDrawingCacheEnabled(false);
		view.destroyDrawingCache();
		return bm;
//		return getWindow().getDecorView().getDrawingCache();
	}

}
