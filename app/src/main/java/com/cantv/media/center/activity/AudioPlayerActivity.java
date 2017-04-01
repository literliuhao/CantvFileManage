package com.cantv.media.center.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
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

import com.cantv.liteplayer.core.ProxyPlayer;
import com.cantv.media.R;
import com.cantv.media.center.Listener.PlayMode;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.LyricInfo;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.data.PlayModeMenuItem;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.ui.audio.CDView;
import com.cantv.media.center.ui.audio.CircleProgressBar;
import com.cantv.media.center.ui.audio.LyricView;
import com.cantv.media.center.ui.dialog.DoubleColumnMenu;
import com.cantv.media.center.ui.dialog.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.dialog.DoubleColumnMenu.OnKeyEventListener;
import com.cantv.media.center.ui.dialog.MenuDialog;
import com.cantv.media.center.ui.dialog.MenuDialog.MenuAdapter;
import com.cantv.media.center.utils.FastBlurUtil;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.StatisticsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import static com.cantv.media.R.string.singer;

/**
 * 播放音频
 */
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

    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler;
    private MenuDialog mMenuDialog;

    private List<MenuItem> mMenuList;
    private int mSelectedMenuPosi;
//    private boolean showLyric = true;

    private LyricInfo mLyricInfo;

    private Formatter mFormatter;

    private StringBuilder mFormatBuilder;

    private MenuItem playModeMenuItem;

    private String mUri;
    private LoadingMuUITask muUITask;
    private int currentMode = 5;
    private List<MenuItem> playListSubMenuItems;
    private List<MenuItem> mCurrentSubMenuList;
    private int mCurrentSubMenuPos;//当前二级菜单位置
    private int mDuration;
    private boolean isShowOutLrc;
    public boolean isOnPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();
        EventBus.getDefault().register(this);
        MyApplication.addActivity(this);
        initHandler();
        initData();
        playDefualt();
        StatisticsUtil.customEvent(AudioPlayerActivity.this, "music_player");
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


    @SuppressLint("HandlerLeak")
    public void initHandler() {
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (getProxyPlayer().isPlaying()) {
                    int currentPosition = getProxyPlayer().getCurrentPosition();
                    boolean progressChanged = mProgressBar.setProgress(currentPosition);
                    if (progressChanged) {
                        mCurrProgressTv.setText(formatTime(currentPosition));
                        mLyricView.setCurrTime(currentPosition);
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
        StatisticsUtil.registerResume(this);
        holdWakeLock();
        if (mDataList.size() < 1) {
//            Toast.makeText(this, " 当前播放路径 " + SharedPreferenceUtil.getMediaPath(), Toast.LENGTH_SHORT).show();
            String mediaPath = SharedPreferenceUtil.getMediaPath();
            mediaPath = mediaPath.subSequence(0, mediaPath.lastIndexOf("/")).toString();

            FileUtil.getFileList(mediaPath, false, new FileUtil.OnFileListListener() {
                @Override
                public void findFileListFinish(List<Media> list) {
                    List<Media> fileList = list;
                    FileUtil.sortList(fileList, FileComparator.SORT_TYPE_DEFAULT, true);
                    if (fileList.size() > 0) {
                        mDataList.clear();
                        mDataList.addAll(fileList);
                    }
                    for (int i = 0; i < fileList.size(); i++) {
                        String path = fileList.get(i).isSharing ? fileList.get(i).sharePath : fileList.get(i).mUri;
                        if (SharedPreferenceUtil.getMediaPath().equals(path)) {
                            mCurPlayIndex = i;
                            mDefaultPlayIndex = i;
                            break;
                        }
                    }
                    if (mDataList.size() > 0) {
                        playDefualt();
                    }
                }
            }, SourceType.MUSIC);

        }

        if (!ismManualPaused()) {
            mCDView.start();
            mPlayPauseBtn.setImageResource(R.drawable.selector_bg_pause_btn);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        StatisticsUtil.registerPause(this);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        isPressback = true;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        //为了处理从不同的入口进入文件管理器,出现的类型错乱,如：从视频入口进入，按home键,再从图片进入,显示的还是视频类型
//        if (!isPressback && !(MyApplication.mHomeActivityList.size() > 0)) {
//            MyApplication.onFinishActivity();
//        }
        releaseWakeLock();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mMenuDialog != null && mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (null != muUITask) {
            muUITask.cancel(true);
        }
        hideMenuDialog();
        mMenuDialog = null;
        mHandler.removeCallbacksAndMessages(null);
        mCDView.pause();
        mCDView = null;
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        if (!isOnPrepared) return;
        switch (v.getId()) {
            case R.id.ib_play_pause:
                //OS-5062	【OS V1.2.0.1489918440 必现】添加共享设备-登录后退出，将共享设备网络断掉，然后再进入文件共享，界面一直处于加载中，过5分钟左右才加载。
                //add isOnPrepared
                if (mDataList == null || mDataList.size() == 0) {
                    break;
                }
                //OS-5062	【OS V1.2.0.1489918440 必现】添加共享设备-登录后退出，将共享设备网络断掉，然后再进入文件共享，界面一直处于加载中，过5分钟左右才加载。
                onPlayerPlayOrPause();
                if (isPlayerPaused()) {
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }

                    setmManualPaused(true);
                    mCDView.pause();
                    mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
                } else {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(0);
                    }

                    setmManualPaused(false);
                    mCDView.start();
                    mPlayPauseBtn.setImageResource(R.drawable.selector_bg_pause_btn);
                }
                break;
            case R.id.ib_previous:
//                if (mCurPlayIndex == 0 && mPlayMode != PlayMode.RANDOM_ORDER) {
//                    //修复OS-4163在文件管理器中播放本地音频，在播放最后一个音频的时候一直按下一曲，一直按多按几次提示无下一首该提示语按的次数越多消失的越慢
//                    ToastUtils.showMessage(MyApplication.getContext(), "无上一首");
//                } else {
                onPlayPrev();
//                }
                break;
            case R.id.ib_next:
                // 当前是最后一个文件,并且是顺序播放模式,点击下一个不会进入下一个
//                if (mDataList.size() - 1 == mCurPlayIndex && mPlayMode != PlayMode.RANDOM_ORDER) {
//                    ToastUtils.showMessage(MyApplication.getContext(), "无下一首");
//                } else {
                onPlayNext();
//                }
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
        mCDView.start();
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
        mDuration = player.getDuration();
        mProgressBar.setMax(mDuration);
        setmPaused(false);  //暂停时,在列表中播放别的曲目,这个参数可能不准确
        mHandler.sendEmptyMessage(0);   //避免没有进度
        mDurationTv.setText(" / " + formatTime(mDuration));

        //保存当前播放的路径
        String path = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
        SharedPreferenceUtil.saveMediaPath(path);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (mMenuDialog == null || !mMenuDialog.isShowing()) {
                    showMenuDialog();
                }
                break;

            //上一曲
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
//                if (mCurPlayIndex == 0) {
//                    Toast.makeText(AudioPlayerActivity.this, R.string.pr_music, Toast.LENGTH_LONG).show();
//                } else {
                onPlayPrev();
//                }
                break;

            //下一曲
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // 当前是最后一个文件,并且是顺序播放模式,点击下一个不会进入下一个
//                if (mDataList.size() - 1 == mCurPlayIndex) {
//                    Toast.makeText(AudioPlayerActivity.this, R.string.next_music, Toast.LENGTH_LONG).show();
//                } else {
                onPlayNext();   //
//                }

                break;

            //暂停/开始
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                mPlayPauseBtn.callOnClick();
                break;

        }

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
                    mCurrentSubMenuPos = position;
                    if (mCurPlayIndex == position) {
                        view.setSelected(true);
                    } else {
                        view.setSelected(hasFocus);
                    }
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
                        //修复MASERATI-222文件夹内有带歌词与不带歌词音乐,USB播放不带歌词音乐打开载入歌词，此时在播放列表切换任意带歌词音乐，播放时无显示歌词
                        if (position == 0) {
//                            showOrHideLrc();
//                            mLyricView.setLyricInfo(mLyricInfo);
                            isShowOutLrc = false;
                            mLyricView.setCurrTime(getProxyPlayer().getCurrentPosition());
                            switchLrcTH();
                            // enable adjust lyric
                            adjuestLyricMenuData.setEnabled(true);
                        } else if (position == 1) {
                            isShowOutLrc = true;
//                            showOrHideLrc();
//                            mLyricView.setLyricInfo(null);
                            // disable adjust lyric
//                            adjuestLyricMenuData.setEnabled(false);
                            mLyricView.setCurrTime(getProxyPlayer().getCurrentPosition());
                            switchLrcTH();
                            adjuestLyricMenuData.setEnabled(true);
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
                        } /*else if (position == 2) {
                            mLyricView.restoreTime();
                        }*/
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
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN && mSelectedMenuPosi == 0) {
                        mMenuDialog.getMenu().openSubMenu(true, mMenuList.get(0).getSelectedChildIndex());
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onSubMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    if (mSelectedMenuPosi == 0) {
                        mCurrentSubMenuList = playListSubMenuItems;
                    } else {
                        return false;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (mCurrentSubMenuPos == mCurrentSubMenuList.size() - 1) {
                            mMenuDialog.showSubMenuFocus(false);
                            mMenuDialog.getMenu().focusSubMenuItem2(mMenuList.get(0).getSelectedIndex(true));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mMenuDialog.showSubMenuFocus(true);
                                }
                            }, 500);
                            return true;
                        } else {
                            return false;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (mCurrentSubMenuPos == 0) {
                            mMenuDialog.showSubMenuFocus(false);
                            mMenuDialog.getMenu().focusSubMenuItem2(mMenuList.get(0).getSelectedIndex(false));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mMenuDialog.showSubMenuFocus(true);
                                }
                            }, 500);
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            });
        }
        if (mSelectedMenuPosi == 0) {
            mMenuList.get(0).setChildSelected(mCurPlayIndex);
            mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            mMenuDialog.getMenu().focusSubMenuItem2(mMenuList.get(0).getSelectedChildIndex());
        } else if (mSelectedMenuPosi == 1) {
            //修复OS-2736进入外接设备，播放本地音乐，在播放器左下角的播放模式中切换播放模式后，打开菜单播放模式选项后没有实时更新。
            if (currentMode != 5) {
                mMenuList.get(1).setChildSelected(currentMode);
                mMenuDialog.getMenu().focusSubMenuItem2(mMenuList.get(1).getSelectedChildIndex());
            }
        }
        //修复OS-3286进入本地播放的视频或音频时，第一次按菜单键呼出菜单栏时，焦点光标从最上方的空白处移动到正在播放的节目上
        mMenuDialog.showSubMenuFocus(false);
        if (mSelectedMenuPosi != 1) {
            MenuItem menuItemData = mMenuList.get(1);
            View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 1);
            if (menuItemView != null) {
                mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
            }
        }
        mMenuDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMenuDialog.showSubMenuFocus(true);
            }
        }, 1200);
    }

    public void hideMenuDialog() {
        if (mMenuDialog != null) {
            mMenuDialog.dismiss();
        }
    }

    private List<MenuItem> createMenuData() {
        List<MenuItem> menuList = new ArrayList<>();
        mCurrentSubMenuList = new ArrayList<>();

        MenuItem playListMenuItem = new MenuItem(getString(R.string.play_list));
        playListMenuItem.setType(MenuItem.TYPE_LIST);
        playListMenuItem.setSelected(true);
        playListSubMenuItems = new ArrayList<>();
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
        List<MenuItem> loadLyricSubMenuItems = new ArrayList<>();
        MenuItem menuItem2 = new MenuItem(getString(R.string.lrc_in), MenuItem.TYPE_SELECTOR);
        menuItem2.setParent(loadLyricMenuItem);
        menuItem2.setSelected(true);
        loadLyricSubMenuItems.add(menuItem2);
        loadLyricSubMenuItems.add(new MenuItem(getString(R.string.lrc_out), MenuItem.TYPE_SELECTOR));
        loadLyricMenuItem.setChildren(loadLyricSubMenuItems);
        menuList.add(loadLyricMenuItem);

        MenuItem adjustLyricMenuItem = new MenuItem(getString(R.string.adjust_lyric));
        List<MenuItem> adjustLyricSubMenuItems = new ArrayList<>();
        adjustLyricSubMenuItems.add(new MenuItem(getString(R.string.forward_seconds), MenuItem.TYPE_LIST));
        adjustLyricSubMenuItems.add(new MenuItem(getString(R.string.delay_seconds), MenuItem.TYPE_LIST));
        //去除还原按钮
        //adjustLyricSubMenuItems.add(new MenuItem("还原", MenuItem.TYPE_LIST));
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

    private void showOrHideLrc() {
        if (null != mLyricInfo) {
            mLyricView.setVisibility(View.VISIBLE);
            mNoLyricLayout.setVisibility(View.INVISIBLE);
        } else {
            mNoLyricLayout.setVisibility(View.VISIBLE);
            mLyricView.setVisibility(View.INVISIBLE);
        }

    }

    private void resetUI() {
        mPlayPauseBtn.setImageResource(R.drawable.selector_bg_play_btn);
        if (mDataList.get(mCurPlayIndex).isSharing) {   //共享会受网络影响
            mProgressBar.setMax(mDuration);
            mProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(1);
                }
            }, 500);
        } else {
            mProgressBar.setProgress(0);
        }
        mCDView.setCoverBitmap(null);
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
        //修复OS-2736进入外接设备，播放本地音乐，在播放器左下角的播放模式中切换播放模式后，打开菜单播放模式选项后没有实时更新。
        int nextIndex = (selectedChildIndex + 1) % children.size();
        changePlayModeByIndex(selectedChildIndex, nextIndex, playModeMenu);
    }

    private void changePlayModeByIndex(int selectedChildPosi, int nextPosi, MenuItem menuItemData) {
        menuItemData.setChildSelected(nextPosi);
        PlayModeMenuItem playModeItem = (PlayModeMenuItem) menuItemData.getSelectedChild();
        mPlayMode = playModeItem.getPlayMode();
        mPlayModeTv.setText(playModeItem.getTitle());
        mPlayModeIconIv.setImageResource(playModeItem.getDrawableResId());
        currentMode = nextPosi;
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

    //liuhao
    //OS-4932	【OS V1.2.0.1489506238 文件管理 必现】点击进入共享设备中的图片/音频/视频，未加载出来时按确认键自动退出显示/播放，提示“文件格式不支持或设备已移除”
    @Override
    public void onPrepared(MediaPlayer mp) {
        isOnPrepared = true;
    }
    //OS-4932	【OS V1.2.0.1489506238 文件管理 必现】点击进入共享设备中的图片/音频/视频，未加载出来时按确认键自动退出显示/播放，提示“文件格式不支持或设备已移除”

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
        if (!mUri.contains(":")) {  //非共享文件才进行获取封面
            //有些机器可能出现内存溢出
            final Bitmap icon = Audio.getAudioPicture(mUri, 800, 800);

            if (icon != null) {
                //耗时操作,在主线程中执行
//            final Drawable drawable = BitmapUtils.blurBitmap(icon, AudioPlayerActivity.this);
                final Bitmap bitmap = FastBlurUtil.toBlur(icon, 6);
                final BitmapDrawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), bitmap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mCDView) {
                            mCDView.setCoverBitmap(icon);
                        }
                        if (null != mContentBg) {
                            mContentBg.setBackground(drawable);
                            mContentBg.setImageResource(R.color.per40_black);
                        }
                    }
                });
            }
        }

        switchLrc();

    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        //修复OS-2387 影片播放完毕自动切换到下一个影片时，菜单显示播放影片依为前一个。
        if (mMenuDialog != null) {
            if (mMenuDialog.isShowing()) {
                mMenuDialog.dismiss();
            }
        }
        super.onCompletion(arg0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbMounted(UsbMounted usbMounted) {
        if (!usbMounted.mIsRemoved) {
            return;
        }
        Log.i("Mount", "audio ...");
        if (mDataList == null || mDataList.size() == 0) {
            return;
            //是共享就不用继续下去
        } else if (null != mDataList && mDataList.size() > 0 && mDataList.get(0).isSharing) {
            return;
        }

        //获取当前未移除的外接设备路径
        final List<Media> mediaList = new ArrayList<>();
        List<String> currPathList = MediaUtils.getCurrPathList();
        for (String path : currPathList) {
            File file = new File(path);
            Media fileInfo = FileUtil.getFileInfo(file, null, false);
            mediaList.add(fileInfo);
        }

        boolean isFinish = true;
        for (int i = 0; i < mediaList.size(); i++) {
            if (mDataList.get(mCurPlayIndex).mUri.contains(mediaList.get(i).mUri)) {
                isFinish = false;
                break;
            }
        }
        if (isFinish) {
            isPressback = true;
            finish();
        }
    }


    public void switchLrcTH() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                switchLrc();
            }
        }).start();
    }

    /**
     * loadUI方法里使用这个,其他使用switchLrcTH
     */
    public void switchLrc() {
        if (!mUri.contains(":")) {
            mLyricInfo = Audio.getInOrOutLrc(mUri, isShowOutLrc);  //这个比较耗时
        } else {
            return;
        }
        if (mLyricInfo == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showOrHideLrc();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showOrHideLrc();
                    mLyricView.setLyricInfo(mLyricInfo);
                }
            });
        }
    }

}
