package com.cantv.media.center.activity;

import java.util.Iterator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.MediaUtils.ImageCallBack;
import com.cantv.media.center.data.Audio;
import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.ui.CoverFlowImageView;
import com.cantv.media.center.ui.CoverFlowImageView.MediaPlayerControl;
import com.cantv.media.center.ui.CoverFlowView;
import com.cantv.media.center.ui.PlayerControllerBar;
import com.cantv.media.center.ui.PlayerControllerBar.ShowMediaTitle;

@SuppressLint("NewApi")
public class AudioPlayerActivity extends PlayerActivity implements ShowMediaTitle, MediaPlayerControl {
	private PlayerControllerBar mCtrlBar;
	private CoverFlowView mCoverFlowView;
	private BroadcastReceiver maudiaoReceiver;
	private RelativeLayout media__ctrl_view__content;
	private TextView maudiao_title;
	private TextView mTitleView;
	private PowerManager.WakeLock mWakeLock;
	private Bitmap mBitmap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media__audio_view);
		// KeyguardManager km = (KeyguardManager)
		// this.getSystemService(Context.KEYGUARD_SERVICE);
		// mKeyguardLock = km.newKeyguardLock("keyLock");
		// mKeyguardLock.disableKeyguard();
		acquireWakeLock();
		media__ctrl_view__content = (RelativeLayout) findViewById(R.id.media__ctrl_view__content);
		media__ctrl_view__content.getBackground().setAlpha(0);
		mCtrlBar = (PlayerControllerBar) findViewById(R.id.media__audio_view__ctrl);
		maudiao_title = (TextView) findViewById(R.id.audiao_title);
		mTitleView = (TextView) findViewById(R.id.media__ctrl_view__name);
		mTitleView.setVisibility(View.GONE);
		mCoverFlowViewListener = this;
		playDefualt();
		mCtrlBar.setCanHideView(false);
		mCtrlBar.setPlayerCtrlBarListener(this);
		mCtrlBar.setPlayerControllerBarContext(this);
		mCtrlBar.setShowMediaTitle(this);
		mCoverFlowView = (CoverFlowView) findViewById(R.id.custom_gallery);
		mCtrlBar.setCoverFlowViewListener(this);
		initCoverFlowView();

		maudiaoReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
					// Intent temintent = new Intent();
					// temintent.setClass(AudioPlayerActivity.this,
					// IndexActivity.class);
					// AudioPlayerActivity.this.startActivity(temintent);
					if (mDataList == null || mDataList.size() == 0) {
						return;
					}
					String sourcepath = mDataList.get(0);
					String targetpath = intent.getDataString();
					boolean isequal = MediaUtils.isEqualDevices(sourcepath, targetpath);
					// Intent temintent = new Intent();
					// temintent.setClass(VideoPlayerActivity.this,
					// IndexActivity.class);
					// VideoPlayerActivity.this.startActivity(temintent);
					if (isequal) {
						AudioPlayerActivity.this.finish();
					}

				}
			}
		};
		IntentFilter usbFilter = new IntentFilter();
		usbFilter.setPriority(1000);
		// usbFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		// usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		usbFilter.addDataScheme("file");
		AudioPlayerActivity.this.registerReceiver(maudiaoReceiver, usbFilter);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(maudiaoReceiver);
		// mKeyguardLock.reenableKeyguard();
		releaseWakeLock();
	}

	@Override
	protected void runAfterPlay(boolean isFirst) {

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	public void initCoverFlowView() {
		if (mDataList == null || mDataList.size() == 0) {
			return;
		}
		int width = (int) getResources().getDimension(R.dimen.px1200);
		int height = (int) getResources().getDimension(R.dimen.px1400);
		int count = mDataList.size();
		for (int i = 0; i < count; i++) {
			Audio audio = new Audio(SourceType.MUSIC, mDataList.get(i));
			// final ImageView imageView = new ImageView(this);
			final CoverFlowImageView imageView = new CoverFlowImageView(this);
			imageView.setMediaPlayerControl(this);
			imageView.setTag(i);
			imageView.setBackground(getResources().getDrawable(R.drawable.musicdefault));
			imageView.setLayoutParams(new LayoutParams(width, height));
			MediaUtils.loadBitmap(audio, new ImageCallBack() {
				@Override
				public void imageLoad(Bitmap bitmap) {
					if (bitmap != null) {
						imageView.setBackground(null);
						imageView.setImageBitmap(bitmap);
					}
				}
			});
			// imageView.setOnFocusChangeListener(new OnFocusChangeListener() {
			//
			// @Override
			// public void onFocusChange(View v, boolean hasFocus) {
			// // TODO Auto-generated method stub
			// if (hasFocus) {
			// Log.v("liujun722", "index====" + v.getTag());
			// v.setBackground(getResources().getDrawable(R.drawable.f));
			// } else {
			// imageView.setBackground(getResources().getDrawable(R.drawable.musicdefault));
			// }
			// }
			// });
			mCoverFlowView.addItemView(imageView);
		}
		mCoverFlowView.defaultindex = mDefaultPlayIndex;
	}

	public void showDialog() {
		AlertDialog.Builder builder = new Builder(AudioPlayerActivity.this);
		builder.setTitle("提示");
		builder.setMessage("确定退出音乐吗?");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AudioPlayerActivity.this.finish();

			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				onPlayerPlayOrPause();
			}
		});
		builder.show();

	}

	private void acquireWakeLock() {
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
			mWakeLock.acquire();
		}

	}

	private void releaseWakeLock() {
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}

	}

	@Override
	public void scrollToNext() {
		mCoverFlowView.scrollToNext(1);
	}

	@Override
	public void scrollPre() {
		mCoverFlowView.scrollToNext(-1);
	}

	@Override
	public void updateMediaTitle(String title) {
		maudiao_title.setText(title);
	}

	@Override
	public void onCoverFlowNext() {
		mCtrlBar.coverFlowNext();
	}

	@Override
	public void onCoverFlowPrev() {
		mCtrlBar.coverFlowPre();
	}

	@Override
	public void onCoverFlowPlayOrPause() {
		mCtrlBar.coverFlowPlayorPause();
	}

}
