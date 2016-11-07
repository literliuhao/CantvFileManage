package com.cantv.media.center.activity;

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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.PlayMode;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.LyricInfo;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.data.PlayModeMenuItem;
import com.cantv.media.center.ui.CDView;
import com.cantv.media.center.ui.CircleProgressBar;
import com.cantv.media.center.ui.DoubleColumnMenu;
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.DoubleColumnMenu.OnKeyEventListener;
import com.cantv.media.center.ui.LyricView;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.utils.BitmapUtils;
import com.cantv.media.center.utils.MediaUtils;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import static com.cantv.media.R.string.singer;

@SuppressLint("NewApi")
public class AudioPlayerActivity extends PlayerActivity implements android.view.View.OnClickListener {

    private final int INTERVAL_CHECK_PROGRESS = 1;

    private ImageView mContentBg;
    private CircleProgressBar mProgressBar;
    private CDView mCDView;
    private TextView mCurrProgressTv, mDurationTv, mPlayModeTv, mTitleTv, mSingerTv;
    private RelativeLayout mNoLyricLayout;
    private LyricView mLyricView;
    private ImageButton mPlayPauseBtn, mPreviousBtn, mNextBtn;
    private ImageView mPlayModeIconIv, mPlayModeView;

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

    private MenuItem playModeMenuItem;

    private static final int UNDATE_UI = -1;  //更新UI
    private String mUri;
    private LoadingMuUITask muUITask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();
        MyApplication.addActivity(this);
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
        mDurationTv = (TextView) findViewById(R.id.tv_duration);
        mCurrProgressTv = (TextView) findViewById(R.id.tv_curr_progress);
        mNoLyricLayout = (RelativeLayout) findViewById(R.id.rl_nolyric);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mSingerTv = (TextView) findViewById(R.id.tv_singer);
        mLyricView = (LyricView) findViewById(R.id.lv_lyric);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.ib_play_pause);
        mPreviousBtn = (ImageButton) findViewById(R.id.ib_previous);
        mNextBtn = (ImageButton) findViewById(R.id.ib_next);
        mPlayModeView = (ImageView) findViewById(R.id.iv_bg_play_mode);
        mPlayModeIconIv = (ImageView) findViewById(R.id.iv_play_mode);
        mPlayModeTv = (TextView) findViewById(R.id.tv_play_mode);

        mPlayPauseBtn.setOnClickListener(this);
        mPreviousBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPlayModeView.setOnClickListener(this);
        int playPauseBtnId = mPlayPauseBtn.getId();
        int preBtnId = mPreviousBtn.getId();
        int nextBtnId = mNextBtn.getId();
        int playModeViewId = mPlayModeView.getId();

        mPlayPauseBtn.setNextFocusLeftId(preBtnId);
        mPlayPauseBtn.setNextFocusRightId(nextBtnId);
        mPlayPauseBtn.setNextFocusUpId(playPauseBtnId);
        mPlayPauseBtn.setNextFocusDownId(playPauseBtnId);

        mPreviousBtn.setNextFocusLeftId(playModeViewId);
        mPreviousBtn.setNextFocusRightId(playPauseBtnId);
        mPreviousBtn.setNextFocusUpId(preBtnId);
        mPreviousBtn.setNextFocusDownId(preBtnId);

        mNextBtn.setNextFocusLeftId(playPauseBtnId);
        mNextBtn.setNextFocusRightId(nextBtnId);
        mNextBtn.setNextFocusUpId(nextBtnId);
        mNextBtn.setNextFocusDownId(nextBtnId);

        mPlayModeView.setNextFocusLeftId(playModeViewId);
        mPlayModeView.setNextFocusRightId(preBtnId);
        mPlayModeView.setNextFocusUpId(playModeViewId);
        mPlayModeView.setNextFocusDownId(playModeViewId);
    }

    public void regUsbChangeReceiver() {
        if (mUsbChangeReceiver == null) {
            mUsbChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                        if (mDataList == null || mDataList.size() == 0) {
                            return;
                        }
                        String sourcepath = mDataList.get(0).isSharing ? mDataList.get(0).sharePath : mDataList.get(0).mUri;
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
            }

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
    protected void onResume() {
        if (!ismManualPaused()) {
            mCDView.startRotate();
            mPlayPauseBtn.setImageResource(R.drawable.selector_bg_pause_btn);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
//        mCDView.pauseRotate();
//        mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
        super.onPause();
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
        unregisterReceiver(mUsbChangeReceiver);
        muUITask.cancel(true);
        releaseWakeLock();
        mUsbChangeReceiver = null;
        mUsbFilter = null;
        hideMenuDialog();
        mMenuDialog = null;
        mHandler.removeCallbacksAndMessages(null);
        mCDView.stopRotate();
        mCDView = null;
        super.onDestroy();
        MyApplication.removeActivity(this);
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
                if (mDataList == null || mDataList.size() == 0) {
                    break;
                }
                onPlayerPlayOrPause();
                if (isPlayerPaused()) {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }

                    setmManualPaused(true);
                    mCDView.pauseRotate();
                    mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
                } else {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(0);
                    }

                    setmManualPaused(false);
                    mCDView.startRotate();
                    mPlayPauseBtn.setImageResource(R.drawable.selector_bg_pause_btn);
                }
                break;
            case R.id.ib_previous:
                if (mCurPlayIndex == 0) {
                    Toast.makeText(AudioPlayerActivity.this, "无上一首", Toast.LENGTH_LONG).show();
                } else {
                    onPlayPrev();
                }
                break;
            case R.id.ib_next:
                // 当前是最后一个文件,并且是顺序播放模式,点击下一个不会进入下一个
                if (mDataList.size() - 1 == mCurPlayIndex) {
                    Toast.makeText(AudioPlayerActivity.this, "无下一首", Toast.LENGTH_LONG).show();
                } else {
                    onPlayNext();
                }

                break;
            case R.id.iv_bg_play_mode:
                cycleChangePlayMode();
                break;
            default:
                break;
        }
    }

    @Override
    protected void runBeforePlay(boolean isFirst) {
        resetUI();
        setmManualPaused(false);
        mCDView.startRotate();
        if (mDataList.size() > 0) {
            mUri = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
            mTitleTv.setText(mDataList.get(mCurPlayIndex).mName);
            String singer = mDataList.get(mCurPlayIndex).isSharing ? "" : Audio.getAudioSinger(mUri);
            if (!TextUtils.isEmpty(singer)) {
                mSingerTv.setText(getString(R.string.singer) + singer);
            }
            if (null != muUITask) {    //先取消之前
                muUITask.cancel(true);
                muUITask = null;
            }
            muUITask = new LoadingMuUITask();
            muUITask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        setmPaused(false);  //暂停时,在列表中播放别的曲目,这个参数可能不准确
        mHandler.sendEmptyMessage(0);   //避免没有进度
        mDurationTv.setText(" / " + formatTime(duration));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mMenuDialog == null || !mMenuDialog.isShowing()) {
                showMenuDialog();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showMenuDialog() {
        if (mDataList == null || mDataList.size() == 0) {
            return;
        }
        if (mMenuDialog == null) {
            mMenuDialog = new MenuDialog(this);
            if (mMenuList == null) {
                mMenuList = createMenuData();
            }
            mMenuDialog.setMenuList(mMenuList);
            mMenuDialog.setOnItemFocusChangeListener(new DoubleColumnMenu.OnItemFocusChangeListener() {
                @Override
                public void onMenuItemFocusChanged(LinearLayout leftViewGroup, View view, int position, boolean hasFocus) {
                    if (mSelectedMenuPosi == position) {
                        return;
                    }
                    mMenuList.get(mSelectedMenuPosi).setSelected(false);

                    View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedMenuPosi);

                    mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, mMenuList.get(mSelectedMenuPosi));

                    mSelectedMenuPosi = position;
                    MenuItem menuItem = mMenuList.get(position);
                    menuItem.setSelected(true);
                    mMenuDialog.getMenuAdapter().updateMenuItem(view, menuItem);
                    //修复菜单焦点问题
                    if (position == 0) {
                        mMenuList.get(0).setChildSelected(mCurPlayIndex);
                        mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
                    } else {
                        mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
                    }
                    return;
                }

                @Override
                public void onSubMenuItemFocusChanged(LinearLayout rightViewGroup, View view, int position, boolean hasFocus) {

                }
            });
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
                        changePlayModeByIndex(lastSelectPosi, position, menuItemData);
                        return;
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
                    View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + lastSelectPosi);
                    if (oldSubMenuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateSubMenuItem(oldSubMenuItemView, menuItemData.getChildAt(lastSelectPosi));
                    }
                    View subMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + position);
                    if (subMenuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateSubMenuItem(subMenuItemView, menuItemData.getSelectedChild());
                    }
                    View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedMenuPosi);
                    if (menuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
                    }
                }

                @Override
                public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
                    if (mSelectedMenuPosi == position) {
                        return false;
                    }
                    return false;
                }
            });
            mMenuDialog.setOnItemKeyEventListener(new OnKeyEventListener() {

                @Override
                public boolean onMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    // if current choice is playList, selected subMenuItem
                    // should be auto-focused after left-key
                    // down
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN && mSelectedMenuPosi == 0) {
                        mMenuDialog.getMenu().openSubMenu(true, mMenuList.get(0).getSelectedChildIndex());
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onSubMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    return false;
                }
            });
        }
        if (mSelectedMenuPosi == 0) {
            mMenuList.get(0).setChildSelected(mCurPlayIndex);
            mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            mMenuDialog.getMenu().focusSubMenuItem2(mMenuList.get(0).getSelectedChildIndex());
        }
        mMenuDialog.show();
    }

    public void hideMenuDialog() {
        if (mMenuDialog != null) {
            mMenuDialog.dismiss();
        }
    }

    private List<MenuItem> createMenuData() {
        List<MenuItem> menuList = new ArrayList<MenuItem>();

        MenuItem playListMenuItem = new MenuItem(getString(R.string.play_list));
        playListMenuItem.setType(MenuItem.TYPE_LIST);
        playListMenuItem.setSelected(true);
        List<MenuItem> playListSubMenuItems = new ArrayList<MenuItem>();
        for (int i = 0, dataCount = mDataList.size(); i < dataCount; i++) {
            MenuItem item = new MenuItem(mDataList.get(i).mName);
            item.setType(MenuItem.TYPE_SELECTOR_MARQUEE);
            playListSubMenuItems.add(item);
        }
        playListMenuItem.setChildren(playListSubMenuItems);
        playListMenuItem.setChildSelected(mCurPlayIndex);
        menuList.add(playListMenuItem);

        playModeMenuItem = new MenuItem(getString(R.string.play_mode));
        playModeMenuItem.setType(MenuItem.TYPE_SELECTOR);
        List<MenuItem> playModeSubMenuItems = new ArrayList<MenuItem>();
        PlayModeMenuItem menuItem = new PlayModeMenuItem(getString(R.string.play_mode_in_order), MenuItem.TYPE_SELECTOR, PlayMode.IN_ORDER, R.drawable.icon_play_mode_in_order);
        menuItem.setParent(playModeMenuItem);
        menuItem.setSelected(true);
        playModeSubMenuItems.add(menuItem);
        playModeSubMenuItems.add(new PlayModeMenuItem(getString(R.string.play_mode_in_random_order), MenuItem.TYPE_SELECTOR, PlayMode.RANDOM_ORDER, R.drawable.icon_play_mode_random));
        playModeSubMenuItems.add(new PlayModeMenuItem(getString(R.string.play_mode_single_cycle), MenuItem.TYPE_SELECTOR, PlayMode.SINGLE_CYCLE, R.drawable.icon_play_mode_cycle));
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
        mLyricView.setVisibility(View.VISIBLE);
        mNoLyricLayout.setVisibility(View.INVISIBLE);
    }

    private void showNoLyricView() {
        mNoLyricLayout.setVisibility(View.VISIBLE);
        mLyricView.setVisibility(View.INVISIBLE);
    }

    private void resetUI() {
        mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
        mProgressBar.setProgress(0);
        mCDView.setImageBitmap(null);
        mContentBg.setBackgroundResource(0);
        mContentBg.setImageResource(0);
        mCurrProgressTv.setText("");
        mDurationTv.setText("");
        mLyricView.setVisibility(View.INVISIBLE);
        mNoLyricLayout.setVisibility(View.INVISIBLE);
        mLyricView.setLyricInfo(null);
        mTitleTv.setText("");
        mSingerTv.setText(getString(singer) + getString(R.string.unknown));
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

    private void cycleChangePlayMode() {
        if (mMenuList == null) {
            mMenuList = createMenuData();
        }
        MenuItem playModeMenu = mMenuList.get(1);
        List<MenuItem> children = playModeMenu.getChildren();
        int selectedChildIndex = playModeMenu.getSelectedChildIndex();
        int nextIndex = ++selectedChildIndex % children.size();
        changePlayModeByIndex(selectedChildIndex, nextIndex, playModeMenu);
    }

    private void changePlayModeByIndex(int selectedChildPosi, int nextPosi, MenuItem menuItemData) {
        menuItemData.setChildSelected(nextPosi);
        PlayModeMenuItem playModeItem = (PlayModeMenuItem) menuItemData.getSelectedChild();
        mPlayMode = playModeItem.getPlayMode();
        mPlayModeTv.setText(playModeItem.getTitle());
        mPlayModeIconIv.setImageResource(playModeItem.getDrawableResId());
        Log.i("", "selectedChildPosi = " + selectedChildPosi + ", nextPosi = " + nextPosi);
        if (mMenuDialog != null && mSelectedMenuPosi == 1) {
            View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + selectedChildPosi);
            if (oldSubMenuItemView != null) {
                mMenuDialog.getMenuAdapter().updateSubMenuItem(oldSubMenuItemView, menuItemData.getChildAt(selectedChildPosi));
            }
            View subMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + nextPosi);
            if (subMenuItemView != null) {
                mMenuDialog.getMenuAdapter().updateSubMenuItem(subMenuItemView, playModeItem);
            }
            View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 1);
            if (menuItemView != null) {
                mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
            }
        }
    }

    /**
     * author: yibh
     * Date: 2016/10/26  17:26 .
     * 加载音频文件内的背景图,使用异步加载,在未加载出来时退出界面,取消加载.
     */
    class LoadingMuUITask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            loadUI();
            return null;
        }

    }

    /**
     * 设置背景
     */
    private void loadUI() {
        //有些机器可能出现内存溢出
        final Bitmap icon = Audio.getAudioPicture(mUri, 800, 800);
        if (icon != null) {
            //耗时操作,在主线程中执行
            final Drawable drawable = BitmapUtils.blurBitmap(icon, AudioPlayerActivity.this);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null != mCDView) {
                        mCDView.setImageBitmap(icon);
                    }
                    if (null != mContentBg) {
                        mContentBg.setBackground(drawable);
                        mContentBg.setImageResource(R.color.per40_black);
                    }
                }
            });
        }

        mLyricInfo = Audio.getAudioLyric(mUri);  //这个比较耗时
        if (mLyricInfo == null) {
            showLyric = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showNoLyricView();
                }
            });
        } else {
            showLyric = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLyricView();
                    mLyricView.setLyricInfo(mLyricInfo);
                }
            });
        }
    }

}
