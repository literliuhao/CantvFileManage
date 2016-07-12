package com.cantv.media.center.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.R;
import com.cantv.media.center.constants.PlayMode;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.LyricInfo;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.data.PlayModeMenuItem;
import com.cantv.media.center.ui.CDView;
import com.cantv.media.center.ui.CircleProgressBar;
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.LyricView;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.utils.BitmapUtils;
import com.cantv.media.center.utils.MediaUtils;

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
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class AudioPlayerActivity extends PlayerActivity implements android.view.View.OnClickListener {

	private final int INTERVAL_CHECK_PROGRESS = 1;

	private ImageView mContentBg;
	private CircleProgressBar mProgressBar;
	private CDView mCDView;
	private TextView mCurrProgressTv, mDurationTv, mPlayModeTv, mTitleTv, mSingerTv;
	private RelativeLayout mNoLyricLayout;
	private RelativeLayout mLyricLayout;
	private LyricView mLyricView;
	private ImageButton mPlayPauseBtn, mPreviousBtn, mNextBtn;

	private BroadcastReceiver mUsbChangeReceiver;
	private IntentFilter mUsbFilter;
	private PowerManager.WakeLock mWakeLock;
	private Handler mHandler;
	private MenuDialog mMenuDialog;

	private List<MenuItem> mMenuList;
	private int mSelectedMenuPosi;
	private boolean showLyric = true;

	private LyricInfo mLyricInfo;

	private Formatter mFormatter;

	private StringBuilder mFormatBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupLayout();
		holdWakeLock();
		initData();
		regUsbChangeReceiver();
		playDefualt();
		initHandler();
	}

	private void setupLayout() {
		setContentView(R.layout.activity_audio_player);
		mContentBg = (ImageView) findViewById(R.id.iv_bg);
		mProgressBar = (CircleProgressBar) findViewById(R.id.pb_circle);
		mCDView = (CDView) findViewById(R.id.v_cd);
		mCurrProgressTv = (TextView) findViewById(R.id.tv_curr_progress);
		mDurationTv = (TextView) findViewById(R.id.tv_duration);
		mCurrProgressTv = (TextView) findViewById(R.id.tv_curr_progress);
		mCurrProgressTv = (TextView) findViewById(R.id.tv_curr_progress);
		mNoLyricLayout = (RelativeLayout) findViewById(R.id.rl_nolyric);
		mLyricLayout = (RelativeLayout) findViewById(R.id.rl_lyric);
		mTitleTv = (TextView) findViewById(R.id.tv_title);
		mSingerTv = (TextView) findViewById(R.id.tv_singer);
		mLyricView = (LyricView) findViewById(R.id.lv_lyric);
		mPlayPauseBtn = (ImageButton) findViewById(R.id.ib_play_pause);
		mPreviousBtn = (ImageButton) findViewById(R.id.ib_previous);
		mNextBtn = (ImageButton) findViewById(R.id.ib_next);
		mPlayModeTv = (TextView) findViewById(R.id.tv_play_mode);

		mPlayPauseBtn.setOnClickListener(this);
		mPreviousBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);

		int playPauseBtnId = mPlayPauseBtn.getId();
		int preBtnId = mPreviousBtn.getId();
		int nextBtnId = mNextBtn.getId();

		mPlayPauseBtn.setNextFocusLeftId(preBtnId);
		mPlayPauseBtn.setNextFocusRightId(nextBtnId);
		mPlayPauseBtn.setNextFocusUpId(playPauseBtnId);
		mPlayPauseBtn.setNextFocusDownId(playPauseBtnId);

		mPreviousBtn.setNextFocusLeftId(preBtnId);
		mPreviousBtn.setNextFocusRightId(playPauseBtnId);
		mPreviousBtn.setNextFocusUpId(preBtnId);
		mPreviousBtn.setNextFocusDownId(preBtnId);

		mNextBtn.setNextFocusLeftId(playPauseBtnId);
		mNextBtn.setNextFocusRightId(nextBtnId);
		mNextBtn.setNextFocusUpId(nextBtnId);
		mNextBtn.setNextFocusDownId(nextBtnId);

	}

	public void regUsbChangeReceiver() {
		if (mUsbChangeReceiver == null) {
			mUsbChangeReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)
							|| intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
						if (mDataList == null || mDataList.size() == 0) {
							return;
						}
						String sourcepath = mDataList.get(0);
						String targetpath = intent.getDataString();
						boolean isequal = MediaUtils.isEqualDevices(sourcepath, targetpath);
						if (isequal) {
							AudioPlayerActivity.this.finish();
						}
					}
				}
			};
			mUsbFilter = new IntentFilter();
			mUsbFilter.setPriority(1000);
			mUsbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			mUsbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
			mUsbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			mUsbFilter.addDataScheme("file");
		}
		registerReceiver(mUsbChangeReceiver, mUsbFilter);
	}

	@SuppressLint("HandlerLeak")
	public void initHandler() {
		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (getProxyPlayer().isPlaying()) {
					int currentPosition = getProxyPlayer().getCurrentPosition();
					boolean progressChanged = mProgressBar.setProgress(currentPosition);
					if (progressChanged) {
						mCurrProgressTv.setText(formatTime(currentPosition));
						if (showLyric) {
							mLyricView.setCurrTime(currentPosition);
						}
					}
				}
				sendMessageDelayed(obtainMessage(), INTERVAL_CHECK_PROGRESS);
			};
		};
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mHandler == null) {
			initHandler();
		}
		mHandler.removeCallbacksAndMessages(null);
		mHandler.sendEmptyMessage(0);
	}

	@Override
	protected void onStop() {
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbChangeReceiver);
		releaseWakeLock();
		mUsbChangeReceiver = null;
		mUsbFilter = null;
	}

	@SuppressWarnings("deprecation")
	private void holdWakeLock() {
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

	private void initData() {
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_play_pause:
			onPlayerPlayOrPause();
			if (isPlayerPaused()) {
				mCDView.stopRotate();
				mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
			} else {
				mCDView.startRotate();
				mPlayPauseBtn.setImageResource(R.drawable.selector_bg_pause_btn);
			}
			break;
		case R.id.ib_previous:
			onPlayPrev();
			break;
		case R.id.ib_next:
			onPlayNext();
			break;

		default:
			break;
		}
	}

	@Override
	protected void runBeforePlay(boolean isFirst) {
		resetUI();
		mCDView.startRotate();
		if (mDataList.size() > 0) {
			String uri = mDataList.get(mCurPlayIndex);
			String audioName = Audio.getAudioName(uri);
			mTitleTv.setText(TextUtils.isEmpty(audioName) ? getPlayerTitle() : audioName);
			String singer = Audio.getAudioSinger(uri);
			if (!TextUtils.isEmpty(singer)) {
				mSingerTv.setText(getString(R.string.singer) + singer);
			}
			Bitmap icon = Audio.getAudioPicture(uri, 800, 800);
			if (icon != null) {
				mCDView.setImageBitmap(icon);
				mContentBg.setBackground(BitmapUtils.blurBitmap(icon, this));
				mContentBg.setImageResource(R.color.per40_black);
			}
			mLyricInfo = Audio.getAudioLyric(uri);
			if (mLyricInfo == null) {
				showLyric = false;
				showNoLyricView();
			} else {
				showLyric = true;
				showLyricView();
				mLyricView.setLyricInfo(mLyricInfo);
			}
		}
	}

	@Override
	protected void runAfterPlay(boolean isFirst) {
		mPlayPauseBtn.setImageResource(R.drawable.selector_bg_pause_btn);
		if (isFirst) {
			mPlayPauseBtn.setFocusable(true);
			mPlayPauseBtn.requestFocus();
		}
		ProxyPlayer player = getProxyPlayer();
		int duration = player.getDuration();
		mProgressBar.setMax(duration);
		mDurationTv.setText(" / " + formatTime(duration));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenuDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showMenuDialog() {
		if (mDataList == null || mDataList.size() == 0) {
			return;
		}
		if (mMenuDialog == null) {
			mMenuDialog = new MenuDialog(this);
			mMenuList = createMenuData();
			mMenuDialog.setMenuList(mMenuList);
			mMenuDialog.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onSubMenuItemClick(LinearLayout parent, View view, int position) {
					MenuItem menuItemData = mMenuList.get(mSelectedMenuPosi);
					int lastSelectPosi = menuItemData.setChildSelected(position);
					if (mSelectedMenuPosi == 0) {
						// select playList item
						if (mCurPlayIndex == position) {
							return;
						}
						playMedia(position);

					} else if (mSelectedMenuPosi == 1) {
						// select playMode
						mPlayMode = ((PlayModeMenuItem) menuItemData.getChildAt(position)).getPlayMode();
						mPlayModeTv.setText(menuItemData.getSelectedChild().getTitle());

					} else if (mSelectedMenuPosi == 2) {
						MenuItem adjuestLyricMenuData = mMenuList.get(3);
						// select load lyric or no
						if (position == 0 && showLyric == false && mLyricInfo != null) {
							// show lyricView
							showLyric = true;
							showLyricView();
							mLyricView.setLyricInfo(mLyricInfo);
							mLyricView.setCurrTime(getProxyPlayer().getCurrentPosition());
							// enable adjust lyric
							adjuestLyricMenuData.setEnabled(true);
						} else if (position == 1 && showLyric == true) {
							// hide lyricView
							showLyric = false;
							showNoLyricView();
							mLyricView.setLyricInfo(null);
							// disable adjust lyric
							adjuestLyricMenuData.setEnabled(false);
						}
						// change adjust lyric menuItem enable
						View adjustLyricMenu = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 3);
						if (adjustLyricMenu != null) {
							mMenuDialog.getMenuAdapter().updateMenuItem(adjustLyricMenu, adjuestLyricMenuData);
						}

					} else if (mSelectedMenuPosi == 3) {
						// select adjust lyric
						if (position == 0) {
							mLyricView.adjustTimeOffset(200);
						} else if (position == 1) {
							mLyricView.adjustTimeOffset(-200);
						}
					}

					View oldSubMenuItemView = mMenuDialog.getMenu()
							.findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + lastSelectPosi);
					if (oldSubMenuItemView != null) {
						mMenuDialog.getMenuAdapter().updateSubMenuItem(oldSubMenuItemView,
								menuItemData.getChildAt(lastSelectPosi));
					}
					View subMenuItemView = mMenuDialog.getMenu()
							.findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + position);
					if (subMenuItemView != null) {
						mMenuDialog.getMenuAdapter().updateSubMenuItem(subMenuItemView,
								menuItemData.getSelectedChild());
					}
					View menuItemView = mMenuDialog.getMenu()
							.findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedMenuPosi);
					if (menuItemView != null) {
						mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
					}
				}

				@Override
				public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
					if (mSelectedMenuPosi == position) {
						return false;
					}
					mMenuList.get(mSelectedMenuPosi).setSelected(false);
					mSelectedMenuPosi = position;
					MenuItem menuItem = mMenuList.get(position);
					menuItem.setSelected(true);
					mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
					return false;
				}
			});
		}
		mMenuDialog.show();
	}

	private List<MenuItem> createMenuData() {
		List<MenuItem> menuList = new ArrayList<MenuItem>();

		MenuItem playListMenuItem = new MenuItem(getString(R.string.play_list));
		playListMenuItem.setType(MenuItem.TYPE_LIST);
		playListMenuItem.setSelected(true);
		List<MenuItem> playListSubMenuItems = new ArrayList<MenuItem>();
		for (int i = 0, dataCount = mDataList.size(); i < dataCount; i++) {
			String url = mDataList.get(i);
			MenuItem item = new MenuItem(url.substring(url.lastIndexOf("/") + 1));
			item.setType(MenuItem.TYPE_LIST);
			playListSubMenuItems.add(item);
			if (i == mCurPlayIndex) {
				item.setSelected(true);
			}
		}
		playListMenuItem.setChildren(playListSubMenuItems);
		menuList.add(playListMenuItem);

		MenuItem playModeMenuItem = new MenuItem(getString(R.string.play_mode));
		playModeMenuItem.setType(MenuItem.TYPE_SELECTOR);
		List<MenuItem> playModeSubMenuItems = new ArrayList<MenuItem>();
		PlayModeMenuItem menuItem = new PlayModeMenuItem(getString(R.string.play_mode_in_order), MenuItem.TYPE_SELECTOR,
				PlayMode.IN_ORDER);
		menuItem.setParent(playModeMenuItem);
		menuItem.setSelected(true);
		playModeSubMenuItems.add(menuItem);
		playModeSubMenuItems.add(new PlayModeMenuItem(getString(R.string.play_mode_in_random_order),
				MenuItem.TYPE_SELECTOR, PlayMode.RANDOM_ORDER));
		playModeSubMenuItems.add(new PlayModeMenuItem(getString(R.string.play_mode_single_cycle),
				MenuItem.TYPE_SELECTOR, PlayMode.SINGLE_CYCLE));
		playModeMenuItem.setChildren(playModeSubMenuItems);
		menuList.add(playModeMenuItem);

		MenuItem loadLyricMenuItem = new MenuItem(getString(R.string.load_lyric));
		loadLyricMenuItem.setType(MenuItem.TYPE_SELECTOR);
		List<MenuItem> loadLyricSubMenuItems = new ArrayList<MenuItem>();
		MenuItem menuItem2 = new MenuItem(getString(R.string.str_open), MenuItem.TYPE_SELECTOR);
		menuItem2.setParent(loadLyricMenuItem);
		menuItem2.setSelected(true);
		loadLyricSubMenuItems.add(menuItem2);
		loadLyricSubMenuItems.add(new MenuItem(getString(R.string.str_close), MenuItem.TYPE_SELECTOR));
		loadLyricMenuItem.setChildren(loadLyricSubMenuItems);
		menuList.add(loadLyricMenuItem);

		MenuItem adjustLyricMenuItem = new MenuItem(getString(R.string.adjust_lyric));
		List<MenuItem> adjustLyricSubMenuItems = new ArrayList<MenuItem>();
		adjustLyricSubMenuItems.add(new MenuItem(getString(R.string.forward_seconds), MenuItem.TYPE_LIST));
		adjustLyricSubMenuItems.add(new MenuItem(getString(R.string.delay_seconds), MenuItem.TYPE_LIST));
		adjustLyricMenuItem.setChildren(adjustLyricSubMenuItems);
		menuList.add(adjustLyricMenuItem);

		return menuList;
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

	@Override
	public void scrollToNext() {

	}

	@Override
	public void scrollPre() {

	}

	private void showLyricView() {
		mLyricLayout.setVisibility(View.VISIBLE);
		mNoLyricLayout.setVisibility(View.INVISIBLE);
	}

	private void showNoLyricView() {
		mNoLyricLayout.setVisibility(View.VISIBLE);
		mLyricLayout.setVisibility(View.INVISIBLE);
	}

	private void resetUI() {
		mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
		mProgressBar.setProgress(0);
		mCDView.setImageBitmap(null);
		mLyricLayout.setVisibility(View.INVISIBLE);
		mNoLyricLayout.setVisibility(View.INVISIBLE);
		mLyricView.setLyricInfo(null);
		mTitleTv.setText("");
		mSingerTv.setText(getString(R.string.singer) + getString(R.string.unknown));
	}

	protected CharSequence formatTime(int timeInMillis) {
		long totalSeconds = timeInMillis / 1000;
		long seconds = totalSeconds % 60;
		long minutes = (totalSeconds / 60) % 60;
		long hours = totalSeconds / 3600;
		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}
}
