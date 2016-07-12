package com.cantv.media.center.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.media.R;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.greendao.DaoOpenHelper;
import com.cantv.media.center.greendao.VideoPlayer;
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.ExternalSurfaceView;
import com.cantv.media.center.ui.ExternalSurfaceView.ShowType;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.ui.player.BasePlayer;
import com.cantv.media.center.ui.player.PlayerController;
import com.cantv.media.center.utils.MediaUtils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VideoPlayActicity extends BasePlayer implements OnVideoSizeChangedListener, StDisplayCallBack {
	
	
	
	private PowerManager.WakeLock mWakeLock;
	private ExternalSurfaceView mSurfaceView;
	private TextView mSubTitleView1;
	private TextView mSubTitleView2;
	private ImageView mBackgroundView;
	private PlayerController mCtrBar;
	private ArrayList<SrtBean> mSrts;
	private MenuDialog mMenuDialog;
	private List<MenuItem> list;
	private MenuItem mSelectedMenuItem;
	
	private int curindex;
	private int size = 0;
	private boolean isSubTitle = true;
	private int mMoveTime = 0;

	private int mSelectedPosi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		if (mDataList == null || mDataList.size() == 0) {
			return;
		}
		for (int i = 0; i < mDataList.size(); i++) {
			Log.e("sunyanlong", "url:" + mDataList.get(i));
		}
		acquireWakeLock();// 禁止屏保弹出
		initView();
		
		DaoOpenHelper.getInstance(this);
	}

	private void initView() {
		mSubTitleView1 = (TextView) findViewById(R.id.media__video_view__subtitle1);
		mSubTitleView2 = (TextView) findViewById(R.id.media__video_view__subtitle2);
		mSurfaceView = (ExternalSurfaceView) findViewById(R.id.media__video_view__surface);
		mBackgroundView = (ImageView) findViewById(R.id.media__video_view__background);
		mCtrBar = (PlayerController) findViewById(R.id.media__video_view__ctrlbar);
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

		mCtrBar.setPlayerCtrlBarListener(this);
		mCtrBar.setPlayerControllerBarContext(this);
		mCtrBar.setPlayerCoverFlowViewListener(this);

	}

	@Override
	protected void runAfterPlay(boolean isFirst) {
		getProxyPlayer().setMovieSubTitle(0);
		getProxyPlayer().setMovieAudioTrack(0);
		getProxyPlayer().setSubTitleDisplayCallBack(this);
		mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_ORIGINAL);
		mSurfaceView.setWidthHeightRate(getProxyPlayer().getVideoWidthHeightRate());
	}

	private void acquireWakeLock() {
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
			mWakeLock.acquire();
		}

	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int arg1, int arg2) {
		boolean showBg = (arg1 == 0 || arg2 == 0);
		mBackgroundView.setVisibility(showBg ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onSubTitleChanging() {

	}

	@Override
	public void showSubTitleText(final String text) {
		mSurfaceView.post(new Runnable() {
			@Override
			public void run() {
				// mSubTitleView.setText(text);
			}
		});
	}

	@Override
	protected void runProgressBar() {
		
		String path = mDataList.get(mCurPlayIndex);
		mCtrBar.setPlayDuration();
		List<VideoPlayer> list= DaoOpenHelper.getInstance(this).queryInfo(path);
		if(list.size() != 0){
			mRecord = list.get(0);
			final int positon = list.get(0).getPosition();
			
			AlertDialog.Builder dialog = new Builder(VideoPlayActicity.this);
			dialog.setMessage("是否恢复到上次观看?");
			dialog.setTitle("提示");
			dialog.setPositiveButton("确定", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onPlaySeekTo(positon,null);
					Log.e("sunyanlong","back="+positon);
					mCtrBar.showController();
					dialog.dismiss();
				}
			});
			dialog.setNegativeButton("取消", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			dialog.create().show();
		}
		
		
		addTimedText();
		// // 添加字幕
		// String url = mDataList.get(mCurPlayIndex);
		// String srt = url.substring(0, url.indexOf(".")) + ".srt";
		// File file = new File(srt);
		// if (file.exists() || file.canRead()) {
		// getProxyPlayer().addText(srt, new OnTimedTextListener() {
		//
		// @Override
		// public void onTimedText(MediaPlayer mp, TimedText text) {
		// if (text == null) {
		// showSubTitleText("");
		// return;
		// } else if (text.getText() != null
		// && !"".equals(text.getText())) {
		// showSubTitleText(text.getText());
		// }
		// }
		// });
		// } else {
		// Toast.makeText(this, "没有对应的字幕文件！", 0).show();
		// }

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_MENU:

			showMenuDialog();

			break;

		default:
			break;
		}
		mCtrBar.onKeyDownEvent(keyCode,event);
		
		return super.onKeyDown(keyCode, event);

	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		mCtrBar.onKeyUpEvent(keyCode,event);
		return super.onKeyUp(keyCode, event);
	}
	private void getAllVideo() {

		new Thread() {
			@Override
			public void run() {
				super.run();
				String path = MediaUtils.getUsbRootPath();
				ArrayList<String> arrayList = getVideos(path);

				Message m = Message.obtain();
				m.arg1 = arrayList.size();
			}
		}.start();

	}

	public ArrayList<String> getVideos(String path) {
		File file = new File(path);
		ArrayList<String> list = new ArrayList<String>();
		if (file.isDirectory()) {

			File[] files = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return MediaUtils.isVideo(name);
				}
			});

			for (int i = 0; i < files.length; i++) {
				list.add(file.getAbsolutePath() + "/" + files[i].getName());
			}

			File[] file1 = file.listFiles();

			for (int i = 0; i < file1.length; i++) {

				if (file1[i].isDirectory()) {
					ArrayList<String> list2 = getVideos(file1[i].getAbsolutePath());
					if (list2.size() != 0) {
						list.addAll(list2);
					}
				}
			}

		} else {
			if (MediaUtils.isVideo(file.getName())) {
				list.add(file.getAbsolutePath());
			}
		}

		return list;
	}

	private void addTimedText() {
		// 添加字幕
		// String url = mDataList.get(mCurPlayIndex);
		// String srt = url.substring(0, url.indexOf(".")) + ".srt";
		// File file = new File(srt);
		// if (file.exists() || file.canRead()) {
		// getProxyPlayer().addText(srt, new OnTimedTextListener() {
		//
		// @Override
		// public void onTimedText(MediaPlayer mp, TimedText text) {
		// if (text == null) {
		// showSubTitleText("");
		// return;
		// } else if (text.getText() != null && !"".equals(text.getText())) {
		// showSubTitleText(text.getText());
		// }
		// }
		// });
		// } else {
		// Toast.makeText(this, "没有对应的字幕文件！", 0).show();
		// }

		String url = mDataList.get(mCurPlayIndex);
		final String srt = url.substring(0, url.indexOf(".")) + ".srt";
		File file = new File(srt);
		if (file.exists() || file.canRead()) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					mSrts = SrtParse.parseSrt(srt);
				}
			}).start();

		} else {
			Toast.makeText(this, "没有对应的字幕文件！", Toast.LENGTH_SHORT).show();
		}

	}

	public void setSrt(int time) {

		if (!isSubTitle) {
			return;
		}

		time += mMoveTime;

		if (mSrts != null && mSrts.size() != 0) {

			for (SrtBean bean : mSrts) {
				if (time >= bean.getBeginTime() && time <= bean.getEndTime()) {

					if (bean.getSrt1() != null) {
						mSubTitleView1.setText(bean.getSrt1().trim());
					} else {
						mSubTitleView1.setText("");
					}

					if (bean.getSrt2() != null) {
						mSubTitleView2.setText(bean.getSrt2().trim());
					} else {
						mSubTitleView2.setText("");
					}

					return;
				}
			}
			mSubTitleView1.setText("");
			mSubTitleView2.setText("");
		}
	}
	
	@Override
	public void onBackPressed() {
		storeDuration();
		super.onBackPressed();
	}
	
	public void storeDuration(){
		long position = getPlayerCurPosition();
		String path = mDataList.get(mCurPlayIndex);
		
		if(mRecord == null){
			List<VideoPlayer> list= DaoOpenHelper.getInstance(this).queryInfo(path);
			if(list.size() == 0){
				VideoPlayer info = new VideoPlayer();
				info.setName(path);
				info.setPosition((int)position);
				DaoOpenHelper.getInstance(this).execInsert(info);
			}else{
				mRecord = list.get(0);
				mRecord.setPosition((int)position);
				DaoOpenHelper.getInstance(this).update(mRecord);
			}
		}else{
			mRecord.setPosition((int)position);
			DaoOpenHelper.getInstance(this).update(mRecord);
		}
		
		Log.e("sunyanlong","back="+position);
		
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0) {
		if(mCtrBar!= null){
			mCtrBar.setFullProgress();
			mCtrBar.removeAllMessage();
		}
		if(mRecord != null){
			DaoOpenHelper.getInstance(this).deleteInfo(mRecord);
		}
		super.onCompletion(arg0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mCtrBar!= null){
			mCtrBar.removeAllMessage();
		}
	}

	private String[] getCurVideoAudioTracks() {
		final List<AudioTrack> audioTracks = getProxyPlayer().getAudioTracks();
		String[] titles = new String[audioTracks.size()];
		for (int i = 0; i < audioTracks.size(); i++) {
			int num = i + 1;
			titles[i] = "音轨 " + num;
			Log.e("sunyanlong", "AudioTrack:" + audioTracks.get(i).getName());
		}
		return titles;
	}


	private void showMenuDialog() {
		if (mMenuDialog == null) {
			mMenuDialog = new MenuDialog(this);
			list = createMenuData();
			mMenuDialog.setMenuList(list);
			mMenuDialog.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onSubMenuItemClick(LinearLayout parent, View view, int position) {
					MenuItem menuItemData=list.get(mSelectedPosi);
					int lastSelectPosi = menuItemData.setChildSelected(position);
					View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + lastSelectPosi);
					if(oldSubMenuItemView != null){
						mMenuDialog.getMenuAdapter().updateSubMenuItem(oldSubMenuItemView, menuItemData.getChildAt(lastSelectPosi));
					}

					View subMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + position);
					if(subMenuItemView != null){
						mMenuDialog.getMenuAdapter().updateSubMenuItem(subMenuItemView, menuItemData.getSelectedChild());
					}
					View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedPosi);
					if(menuItemView != null){
						mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
					}
					performSubmenuClickEvent(menuItemData.getSelectedChild(),position);
				}

				@Override
				public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
					if (mSelectedPosi==position) {
						return false;
					}
					list.get(mSelectedPosi).setSelected(false);
					list.get(position).setSelected(true);
					mSelectedPosi=position;
					mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
					return false;
				}
			});
		}
		mMenuDialog.show();
	}

	private void performSubmenuClickEvent(MenuItem mSubSelectedMenu, int position) {
		switch (mSubSelectedMenu.getType()) {
		case MenuItem.TYPE_LIST:
			performTypeListEvent(mSubSelectedMenu, position);
			break;
		case MenuItem.TYPE_NORMAL:
			performTypeNormalEvent(mSubSelectedMenu);
			break;
		case MenuItem.TYPE_SELECTOR:
			performTypeSelectedEvent(mSubSelectedMenu);
			break;
		}

	}

	private void performTypeNormalEvent(MenuItem mSubSelectedMenu) {
		switch (mSubSelectedMenu.getTitle()) {
		case MenuConstant.SUBMENU_ADJUSTSUBTITLE_FORWORD:
			mMoveTime += 200;
			Toast.makeText(this, "提前0.2秒", Toast.LENGTH_SHORT).show();
			break;
		case MenuConstant.SUBMENU_ADJUSTSUBTITLE_BACKWORD:
			mMoveTime -= 200;
			Toast.makeText(this, "延迟0.2秒", Toast.LENGTH_SHORT).show();
			break;

		}

	}

	private void performTypeListEvent(MenuItem mSubSelectedMenu, int position) {
		if (position == mCurPlayIndex) {
			return;
		}
		playMedia(position);
	}

	private void performTypeSelectedEvent(MenuItem mSubSelectedMenu) {

		switch (mSubSelectedMenu.getTitle()) {
		case MenuConstant.SUBMENU_AUDIOTRACKER_ONE:
			getProxyPlayer().setMovieAudioTrack(size);
			break;
		case MenuConstant.SUBMENU_IMAGESCALE_ORIGINAL:
			mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_ORIGINAL);
			break;
		case MenuConstant.SUBMENU_IMAGESCALE_FULL:
			mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_FULL_SCREEN);
			break;
		case MenuConstant.SUBMENU_IMAGESCALE_16_9:
			mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_16_9);
			break;
		case MenuConstant.SUBMENU_IMAGESCALE_4_3:
			mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_4_3);
			break;
		case MenuConstant.SUBMENU_LOADINGSUBTITLE_CLOSE:
			isSubTitle = false;
			mSubTitleView1.setText("");
			mSubTitleView2.setText("");
			break;
		case MenuConstant.SUBMENU_LOADINGSUBTITLE_OPEN:
			isSubTitle = true;
			break;
		default:
			break;
		}
	}

	private List<MenuItem> createMenuData() {
		List<MenuItem> menuList = new ArrayList<MenuItem>();
		// 播放列表
		MenuItem playListMenuItem = new MenuItem("播放列表");
		playListMenuItem.setType(MenuItem.TYPE_LIST);
		playListMenuItem.setSelected(true);
		List<MenuItem> playListSubMenuItems = new ArrayList<MenuItem>();
		for (int i = 0; i < mDataList.size(); i++) {
			String url = mDataList.get(i);
			MenuItem item = new MenuItem(mDataList.get(i).substring(url.lastIndexOf("/") + 1));
			item.setType(MenuItem.TYPE_LIST);
			playListSubMenuItems.add(item);
		}
		playListMenuItem.setChildren(playListSubMenuItems);
		menuList.add(playListMenuItem);

		// 音轨设置
		MenuItem audioTrackMenuItem = new MenuItem("音轨设置");

		audioTrackMenuItem.setType(MenuItem.TYPE_SELECTOR);
		List<MenuItem> audioTrackMenuItems = new ArrayList<MenuItem>();
		MenuItem menuItem = new MenuItem(MenuConstant.SUBMENU_AUDIOTRACKER_ONE, MenuItem.TYPE_SELECTOR);
		menuItem.setSelected(true);
		audioTrackMenuItem.setSelectedChild(menuItem);
		audioTrackMenuItems.add(menuItem);
		audioTrackMenuItem.setChildren(audioTrackMenuItems);
		menuList.add(audioTrackMenuItem);

		// 画面比例
		MenuItem imageScaleMenuItem = new MenuItem("画面比例", MenuItem.TYPE_SELECTOR);
		MenuItem imageScaleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_ORIGINAL,
				MenuItem.TYPE_SELECTOR);
		imageScaleMenuItem.setSelectedChild(imageScaleSubMenuOriginal);
		imageScaleSubMenuOriginal.setSelected(true);
		List<MenuItem> imageScaleMenuItems = new ArrayList<MenuItem>();
		imageScaleMenuItems.add(imageScaleSubMenuOriginal);
		imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_FULL, MenuItem.TYPE_SELECTOR));
		imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_4_3, MenuItem.TYPE_SELECTOR));
		imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_16_9, MenuItem.TYPE_SELECTOR));
		imageScaleMenuItem.setChildren(imageScaleMenuItems);
		menuList.add(imageScaleMenuItem);

		// 载入字母
		MenuItem subtitlesLoadingMenuItem = new MenuItem("载入字幕", MenuItem.TYPE_SELECTOR);
		MenuItem subtitlesLoadingSubMenuItemOpen = new MenuItem(MenuConstant.SUBMENU_LOADINGSUBTITLE_OPEN,
				MenuItem.TYPE_SELECTOR);
		subtitlesLoadingSubMenuItemOpen.setSelected(true);
		subtitlesLoadingMenuItem.setSelectedChild(subtitlesLoadingSubMenuItemOpen);
		MenuItem subtitlesLoadingSubMenuItemColse = new MenuItem(MenuConstant.SUBMENU_LOADINGSUBTITLE_CLOSE,
				MenuItem.TYPE_SELECTOR);
		List<MenuItem> subtitlseLoadintMenus = new ArrayList<MenuItem>();
		subtitlseLoadintMenus.add(subtitlesLoadingSubMenuItemOpen);
		subtitlseLoadintMenus.add(subtitlesLoadingSubMenuItemColse);
		subtitlesLoadingMenuItem.setChildren(subtitlseLoadintMenus);
		menuList.add(subtitlesLoadingMenuItem);

		// 调整字幕
		MenuItem adjustSubtitleMenuItem = new MenuItem("字幕调整", MenuItem.TYPE_NORMAL);
		List<MenuItem> adjustSubtitlesSubMenus = new ArrayList<MenuItem>();
		adjustSubtitlesSubMenus.add(new MenuItem(MenuConstant.SUBMENU_ADJUSTSUBTITLE_FORWORD, MenuItem.TYPE_NORMAL));
		adjustSubtitlesSubMenus.add(new MenuItem(MenuConstant.SUBMENU_ADJUSTSUBTITLE_BACKWORD, MenuItem.TYPE_NORMAL));
		adjustSubtitleMenuItem.setChildren(adjustSubtitlesSubMenus);
		menuList.add(adjustSubtitleMenuItem);

		return menuList;
	}

}
