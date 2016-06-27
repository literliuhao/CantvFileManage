package com.cantv.media.center.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.R.integer;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cantv.media.R;
import com.cantv.media.center.ui.CustomListMenu.ListMenuListener;
import com.cantv.media.center.ui.ExternalSurfaceView;
import com.cantv.media.center.ui.ExternalSurfaceView.ShowType;
import com.cantv.media.center.ui.PlayerControllerBar;
import com.cantv.media.center.ui.RadiaoListMenu;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.liteplayer.core.subtitle.SubTitle;

public class VideoPlayerActivity extends PlayerActivity implements StDisplayCallBack, OnVideoSizeChangedListener {
	private TextView mSubTitleView;
	private PlayerControllerBar mCtrlBar;
	private ExternalSurfaceView mSurfaceView;
	private BroadcastReceiver mvideoReceiver;
	private ImageView mBackgroundView;
	private KeyguardLock mKeyguardLock;
	private PowerManager.WakeLock mWakeLock;
	private int curindex;
	private List<RadiaoListMenu> mdialoglists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media__video_view);
		mdialoglists = new ArrayList<RadiaoListMenu>();
		// KeyguardManager km = (KeyguardManager)
		// this.getSystemService(Context.KEYGUARD_SERVICE);
		// mKeyguardLock = km.newKeyguardLock("keyLock");
		// mKeyguardLock.disableKeyguard();
		acquireWakeLock();// 禁止屏保弹出
		mSubTitleView = (TextView) findViewById(R.id.media__video_view__subtitle);
		mCtrlBar = (PlayerControllerBar) findViewById(R.id.media__video_view__ctrlbar);
		TextView menutip = mCtrlBar.getMenuTip();
		menutip.setVisibility(View.VISIBLE);
		mBackgroundView = (ImageView) findViewById(R.id.media__video_view__background);
		mSurfaceView = (ExternalSurfaceView) findViewById(R.id.media__video_view__surface);
		mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);
		mSurfaceView.getHolder().addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				curindex = mCurPlayIndex;
			}

			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				getProxyPlayer().setPlayerDisplay(arg0);
				if (curindex != 0) {
					playMedia(curindex);
				} else {
					playDefualt();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

			}
		});
		getProxyPlayer().setOnVideoSizeChangedListener(this);
		mCtrlBar.setPlayerCtrlBarListener(this);
		mCtrlBar.setPlayerControllerBarContext(this);
		mvideoReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("liujun629", "mvideoReceiverAction===" + intent.getAction());
				if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
					if (mDataList == null || mDataList.size() == 0) {
						return;
					}
					String sourcepath = mDataList.get(0);
					String targetpath = intent.getDataString();
					boolean isequal = MediaUtils.isEqualDevices(sourcepath, targetpath);
					Log.i("liujun109", "isequal==="+isequal);
					// Intent temintent = new Intent();
					// temintent.setClass(VideoPlayerActivity.this,
					// IndexActivity.class);
					// VideoPlayerActivity.this.startActivity(temintent);
					if (isequal) {
						VideoPlayerActivity.this.finish();
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
		VideoPlayerActivity.this.registerReceiver(mvideoReceiver, usbFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mvideoReceiver);
		// mKeyguardLock.reenableKeyguard();
		releaseWakeLock();
	}

	@Override
	public void showSubTitleText(final String text) {
		mSubTitleView.post(new Runnable() {
			@Override
			public void run() {
				mSubTitleView.setText(text);
			}
		});
	}

	@Override
	public void onSubTitleChanging() {

	}

	@Override
	protected void runAfterPlay(boolean isFirst) {
		getProxyPlayer().setMovieSubTitle(0);
		getProxyPlayer().setMovieAudioTrack(0);
		getProxyPlayer().setSubTitleDisplayCallBack(this);
		mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_ORIGINAL);
		mSurfaceView.setWidthHeightRate(getProxyPlayer().getVideoWidthHeightRate());
	}

	@Override
	protected void playMedia(int index) {
		super.playMedia(index);
		mSubTitleView.setText("");
	}

	@Override
	public void onBackPressed() {
		if (mCtrlBar.onBackPressed()) {
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
			showSettingMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int arg1, int arg2) {
		boolean showBg = (arg1 == 0 || arg2 == 0);
		mBackgroundView.setVisibility(showBg ? View.VISIBLE : View.GONE);
	}

	private String[] getCurVideoSubtiles() {
		List<SubTitle> subTitles = getProxyPlayer().getVideoSubTitles();
		String[] titles = new String[subTitles.size()];
		for (int i = 0; i < subTitles.size(); i++) {
			titles[i] = subTitles.get(i).getAliasesName();
		}
		return titles;
	}

	private String[] getCurVideoAudioTracks() {
		List<AudioTrack> audioTracks = getProxyPlayer().getAudioTracks();
		String[] titles = new String[audioTracks.size()];
		for (int i = 0; i < audioTracks.size(); i++) {
			// if ("und".equals(audioTracks.get(i).getName().trim())) {
			// titles[i] = "音轨 "+i;
			// } else {
			// titles[i] = audioTracks.get(i).getName()+" "+i;
			// }
			int num = i + 1;
			titles[i] = "音轨 " + num;
		}
		return titles;
	}

	private void showSubMenu(String title, String[] data, ListMenuListener l) {
		RadiaoListMenu menu = new RadiaoListMenu(this);
		menu.setMenuItems(data);
		menu.setMenuTitle(title);
		menu.setListMenuListener(l);
		menu.show();
		mdialoglists.add(menu);
	}

	private void showSettingMenu() {
		RadiaoListMenu menu = new RadiaoListMenu(this);
		mdialoglists.add(menu);
		menu.setMenuItems(new String[] { "音轨选择", "字幕选择", "画面比例" });
		menu.setMenuTitle("菜单");
		menu.setListMenuListener(new ListMenuListener() {
			@Override
			public void onMenuItemClicked(final int position) {
				String title = position == 0 ? "音轨" : (position == 1) ? "字幕" : "画面比例";
				String[] data = position == 0 ? getCurVideoAudioTracks() : (position == 1) ? getCurVideoSubtiles() : new String[] { "原始比例", "全屏", "4:3", "16:9" };
				ListMenuListener l = new ListMenuListener() {
					@Override
					public void onMenuItemClicked(int index) {
						if (position == 0) {
							getProxyPlayer().setMovieAudioTrack(index);
						} else if (position == 1) {
							getProxyPlayer().setMovieSubTitle(index);
						} else if (position == 2) {
							ShowType type = ShowType.WIDTH_HEIGHT_ORIGINAL;
							type = index == 0 ? ShowType.WIDTH_HEIGHT_ORIGINAL : type;
							type = index == 1 ? ShowType.WIDTH_HEIGHT_FULL_SCREEN : type;
							type = index == 2 ? ShowType.WIDTH_HEIGHT_4_3 : type;
							type = index == 3 ? ShowType.WIDTH_HEIGHT_16_9 : type;
							mSurfaceView.setShowType(type);
						}
					}
				};
				if (data == null || data.length == 0) {
					return;
				}
				showSubMenu(title, data, l);
			}
		});
		menu.show();
	}

	private void showQuitDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提示");
		builder.setMessage("确定退出观看吗？");
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				VideoPlayerActivity.this.finish();
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

	}

	@Override
	public void scrollPre() {

	}

	@Override
	protected void onStop() {
		if (mdialoglists != null || mdialoglists.size() != 0) {
			for (RadiaoListMenu item : mdialoglists) {
				item.dismiss();
			}
		}
		super.onStop();
	}

}
