package com.cantv.media.center.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.TimedText;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.liteplayer.core.subtitle.StContent;
import com.cantv.liteplayer.core.subtitle.StDisplayCallBack;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.MenuConstant;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.greendao.DaoOpenHelper;
import com.cantv.media.center.greendao.VideoPlayer;
import com.cantv.media.center.dialog.DoubleColumnMenu;
import com.cantv.media.center.dialog.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.dialog.DoubleColumnMenu.OnKeyEventListener;
import com.cantv.media.center.dialog.MenuDialog;
import com.cantv.media.center.dialog.MenuDialog.MenuAdapter;
import com.cantv.media.center.player.ExternalSurfaceView.ShowType;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.StatisticsUtil;
import com.cantv.media.center.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.SmbFile;

import static com.cantv.media.center.app.MyApplication.mContext;

public class VideoPlayActivity extends BasePlayer implements OnVideoSizeChangedListener, StDisplayCallBack {
    private static final String TAG = "VideoPlayActivity";
    private PowerManager.WakeLock mWakeLock;
    private ExternalSurfaceView mSurfaceView;
    private TextView mSubTitle;
    private ImageView mBackgroundView;
    private PlayerController mCtrBar;
    private SrcParser parser;
    private MenuDialog mMenuDialog;
    private TimeReceiver mTimeReceiver;
    private IntentFilter mTimeFilter;
    private int curindex;
    private boolean isSubTitle = true;
    private boolean isSrtExist;
    private int mMoveTime = 0;
    private int mSelectedPosi;
    private List<MenuItem> list;
    private int mInsubTitleIndex;
    private int mOutsubTitleIndex;
    private boolean mOpenInSubtitle = true;    //是否开启内置字幕
    private boolean mOpenExternalSubtitle;    //是否开启内置字幕
    private String mLastStr;   //当前外置字幕的后缀
    private String subName = "";
    private ImageView mSubtitle_bt;
    private SubParser mSubParser;
    private List<MenuItem> playListSubMenuItems;
    //private List<MenuItem> mCurrentSubMenuList;
    //private int mCurrentSubMenuPos;//当前二级菜单位置
    public boolean isOnPrepared = false;
    private ImageView mDolbyImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        EventBus.getDefault().register(this);
        if (mDataList == null || mDataList.size() == 0) {
            return;
        }
        initView();
        registerTimeReceiver();
        MyApplication.addActivity(this);
        StatisticsUtil.customEvent(VideoPlayActivity.this, "video_player");
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticsUtil.registerResume(this);
        acquireWakeLock();// 禁止屏保弹出
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
                        initView();
                    }
                }
            }, SourceType.MOIVE);

        }
    }

    private void initView() {
        getProxyPlayer().setSubTitleDisplayCallBack(this);
        mSubTitle = (TextView) findViewById(R.id.media__video_view__subtitle1);
        mDolbyImg = (ImageView) findViewById(R.id.dolby_img);

        mSubtitle_bt = (ImageView) findViewById(R.id.subtitle_bt);  //sub字幕
        mSurfaceView = (ExternalSurfaceView) findViewById(R.id.media__video_view__surface);
        mBackgroundView = (ImageView) findViewById(R.id.media__video_view__background);
        mCtrBar = (PlayerController) findViewById(R.id.media__video_view__ctrlbar);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder arg0) {
                curindex = mCurPlayIndex;
            }

            /**
             * onResume()会走到这
             * @param arg0
             */
            @Override
            public void surfaceCreated(SurfaceHolder arg0) {
                getProxyPlayer().setPlayerDisplay(arg0);

                if (curindex < mDataList.size()) {
                    final Media media = mDataList.get(curindex);
                    if (media.isSharing) {
                        new Thread(new Runnable() { //防止共享无响应
                            @Override
                            public void run() {
                                try {
                                    SmbFile file = new SmbFile(media.mUri);
                                    Log.w("文件大小", file.getContentLength() + "");
                                    if (file.getContentLength() > 0) {
                                        if (curindex != 0) {
                                            playMedia(curindex);
                                        } else {
                                            playDefualt();
                                        }
                                    } else {
                                        VideoPlayActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtils.showMessage(mContext, getResources().getString(R.string.network_device_abnormality));
                                                finish();
                                            }
                                        });
                                    }
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                    finish();
                                }
                            }
                        }).start();
                    } else {
                        if (curindex != 0) {
                            playMedia(curindex);
                        } else {
                            playDefualt();
                        }
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int arg1, int width, int height) {
            }
        });

        getProxyPlayer().setOnVideoSizeChangedListener(this);
        mCtrBar.setPlayerCtrlBarListener(this);
        mCtrBar.setPlayerControllerBarContext(this);
        mCtrBar.setPlayerCoverFlowViewListener(this);

        //暂停的时候切换画面比例
        mSurfaceView.setChangeScreenListener(new ExternalSurfaceView.ChangeScreenListener() {

            @Override
            public void changeAfter() {
                Log.w("changeAfter", "");
                if (null != mMenuDialog) {
                    mMenuDialog.dismiss();
                }
            }
        });
    }


    @Override
    protected void showDolbyView(boolean isShow) {
        if (isShow) {
            mDolbyImg.setVisibility(View.VISIBLE);
            mDolbyImg.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDolbyImg.setVisibility(View.INVISIBLE);
                }
            }, 15000);
        } else {
            mDolbyImg.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void runAfterPlay(boolean isFirst) {
        //解决内置字幕切换后不消失
        initSrts();
        //解决内置字幕切换后不消失
//        getProxyPlayer().setMovieSubTitle(0);
//        getProxyPlayer().setMovieAudioTrack(0);

        //保存当前播放的路径
        String path = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
        SharedPreferenceUtil.saveMediaPath(path);

        //给外挂字幕关闭
        mOpenExternalSubtitle = false;
        //开启内置字幕
        mOpenInSubtitle = true;
        if (isFirst) {
            mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_16_9);

            mSurfaceView.setWidthHeightRate(getProxyPlayer().getVideoWidthHeightRate());
        }
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
    public void onVideoSizeChanged(MediaPlayer mp, int arg1, int arg2) {
        boolean showBg = (arg1 == 0 || arg2 == 0);
        mBackgroundView.setVisibility(showBg ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void runProgressBar() {
        String path = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
        Log.w("path", path);
        String srtUrl = checkSrt();
        mCtrBar.setPlayDuration();
        List<VideoPlayer> list = DaoOpenHelper.getInstance(this).queryInfo(path);

        if (list.size() != 0) {
            mRecord = list.get(0);
            int position = list.get(0).getPosition();
            if (position > 1000) {
                if (position < getPlayerDuration()) {
                    //当前时长小于视频总时长时
                    mCtrBar.showContinuePlay(position);
                } else {
                    //当发生当前时长超过视频总时长时(名称替换可能会造成这种结果,或者其他未知意外情况)
                    mCtrBar.seekToDuration(0);
                }
            }
        } else {
            mCtrBar.seekToDuration(0);  //数据库中没有保存过
        }

        if (isSrtExist) {
            parseSrts(srtUrl);
        }

        if (mMenuDialog != null) {
            MenuItem audioTrackMenuItem = VideoPlayActivity.this.list.get(1);
            audioTrackMenuItem.setChildren(createAudioTrackList());
            audioTrackMenuItem.setChildSelected(0);
            View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 1);
            if (menuItemView != null) {
                mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, audioTrackMenuItem);
            }
            if (mSelectedPosi == 1) {
                mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            }
            //添加字幕列表
            MenuItem inSubTitleMenuItem = VideoPlayActivity.this.list.get(3);
            inSubTitleMenuItem.setChildren(getInSubtitleList());
            if (getInSubtitleList().size() > 1) {
                inSubTitleMenuItem.setChildSelected(1);
            } else {
                inSubTitleMenuItem.setChildSelected(0);
            }
            View inMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 3);
            if (inMenuItemView != null) {
                mMenuDialog.getMenuAdapter().updateMenuItem(inMenuItemView, inSubTitleMenuItem);
            }
            if (mSelectedPosi == 3) {
                mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            }

            MenuItem outSubTitleMenuItem = VideoPlayActivity.this.list.get(4);
            outSubTitleMenuItem.setChildren(getOutSubtitleList());
            outSubTitleMenuItem.setChildSelected(0);
            View outMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 4);
            if (outMenuItemView != null) {
                mMenuDialog.getMenuAdapter().updateMenuItem(outMenuItemView, outSubTitleMenuItem);
            }
            if (mSelectedPosi == 4) {
                mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            }


        }
    }


    public String checkSrt() {
        String url = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
        final String srt = url.substring(0, url.lastIndexOf(".")) + ".srt";
        File file = new File(srt);
        if (file.exists() && file.canRead()) {
            isSrtExist = true;
            Log.e("sunyanlong", "isSrtExist=" + isSrtExist);
        } else {
            isSrtExist = false;
            Log.e("sunyanlong", "isSrtExist=" + isSrtExist);
        }
        return srt;
    }

    public void parseSrts(final String srtUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                parser = new SrcParser();
                parser.parseFromPath(srtUrl);
            }
        }).start();
    }

    public void parseSub(final String srtUrl) {
        Log.w("url ", srtUrl);
        mSubParser = new SubParser();
        mSubParser.onlySubFromPath(srtUrl);
    }

    public boolean isSrtExist() {
        return isSrtExist;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (mDataList != null && mDataList.size() != 0) {
                    showMenuDialog();
                }
                break;
        }
        //OS-4932	【OS V1.2.0.1489506238 文件管理 必现】点击进入共享设备中的图片/音频/视频，未加载出来时按确认键自动退出显示/播放，提示“文件格式不支持或设备已移除”
        if (mCtrBar != null && isOnPrepared) {
            mCtrBar.onKeyDownEvent(keyCode, event);
        }
        //OS-4932	【OS V1.2.0.1489506238 文件管理 必现】点击进入共享设备中的图片/音频/视频，未加载出来时按确认键自动退出显示/播放，提示“文件格式不支持或设备已移除”
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mCtrBar != null) {
            mCtrBar.onKeyUpEvent(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }


    public void initSrts() {
        mSubTitle.setText("");
    }

    //字幕修改
    public void setSrts(int time) {
//        Log.w("setSrts", "----setSrts");
        if (!mOpenExternalSubtitle || !mLastStr.toLowerCase().contains("srt")) {
            return;
        }

        time += mMoveTime;
        final String srtByTime = parser.getSrtByTime(time);
        mSubTitle.setText(srtByTime);
    }

    //字幕修改
    public void setSub(int time) {
//        Log.w("setSub", "----sub");
        if (!mOpenExternalSubtitle || !mLastStr.toLowerCase().endsWith("sub") || null == mSubParser) {
            return;
        }

        time += mMoveTime;
        final String srtByTime = mSubParser.getSrtByTime(time);
        Log.w("currTime--content", time + "--" + srtByTime);
        mSubTitle.setText(srtByTime);
    }


    @Override
    public void onBackPressed() {
        isPressback = true;
        storeDuration();
        if (mCtrBar != null) {
            mCtrBar.onBackPressed(this);
        }
    }

    public void storeDuration() {
        if (mDataList == null || mCurPlayIndex >= mDataList.size()) {
            return;
        }

        String path = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;

        long position = getPlayerCurPosition();
//        Log.w("当前进度  ", position + "");
//        Log.w("总进度  ", getPlayerDuration() + "");
        //当前进度是0或者进度和总时长差距在5秒内就没有意思,不进行保存
        if (position == 0 || getPlayerDuration() - position < 5000) {
            return;
        }

        List<VideoPlayer> list = DaoOpenHelper.getInstance(this).queryInfo(path);
        if (mRecord == null) {
            if (list.size() == 0) {
                VideoPlayer info = new VideoPlayer();
                info.setName(path);
                info.setPosition((int) position);
                DaoOpenHelper.getInstance(this).execInsert(info);
            } else {
                mRecord = list.get(0);
                mRecord.setPosition((int) position);
                DaoOpenHelper.getInstance(this).update(mRecord);
            }
        } else {
            mRecord.setPosition((int) position);
            DaoOpenHelper.getInstance(this).update(mRecord);
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (mCtrBar != null) {
            mCtrBar.setFullProgress();
            mCtrBar.removeAllMessage();
        }
        //修复OS-2387 影片播放完毕自动切换到下一个影片时，菜单显示播放影片依为前一个。
        if (mMenuDialog != null) {
            if (mMenuDialog.isShowing()) {
                mMenuDialog.dismiss();
            }
        }

        //差值5秒作为间隔
        if (getPlayerDuration() - getPlayerCurPosition() > 5000) {
            storeDuration();
            if (mCtrBar != null) {
                mCtrBar.onBackPressed(this);
            }
        } else {
            if (mRecord != null) {
                DaoOpenHelper.getInstance(this).deleteInfo(mRecord);
            }
//            Log.w("getPlayerDuration", getPlayerDuration() + "");
            if (getPlayerDuration() > 3000) { //可能出现不支持格式,直接调用onCompletion方法而造成播放下个视频的情况(暂时这么改,不建议这么做)
                super.onCompletion(arg0);
            } else {
                ToastUtils.showMessage(MyApplication.getContext(), getResources().getString(R.string.format_not_support));
                finish();
            }
        }
    }

    private String[] getCurVideoAudioTracks() {
        final List<AudioTrack> audioTracks = getProxyPlayer().getAudioTracks();
        String[] titles = new String[audioTracks.size()];
        for (int i = 0; i < audioTracks.size(); i++) {
            int num = i + 1;
            titles[i] = "音轨" + num;
        }
        return titles;
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new MenuDialog(this);
            list = createMenuData();
            mMenuDialog.setMenuList(list);
            mMenuDialog.setOnItemFocusChangeListener(new DoubleColumnMenu.OnItemFocusChangeListener() {
                @Override
                public void onMenuItemFocusChanged(LinearLayout leftViewGroup, View view, int position, boolean hasFocus) {
                    if (mSelectedPosi == position) {
                        return;
                    }
                    list.get(mSelectedPosi).setSelected(false);
                    View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedPosi);
                    mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, list.get(mSelectedPosi));
                    mSelectedPosi = position;
                    MenuItem menuItem = list.get(position);
                    menuItem.setSelected(true);
                    mMenuDialog.getMenuAdapter().updateMenuItem(view, menuItem);
                    //修复菜单焦点问题
                    if (position == 0) {
                        list.get(0).setChildSelected(mCurPlayIndex);
                        mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
                    } else {
                        mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
                    }
                    return;
                }

                @Override
                public void onSubMenuItemFocusChanged(LinearLayout rightViewGroup, View view, int position, boolean hasFocus) {
                    //修复OS-4061播放共享设备中视频，按菜单键，播放列表中，将焦点移动到名称较长的视频上，视频名称没有实现滚动显示
                    //mCurrentSubMenuPos = position;
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
                    MenuItem menuItemData = list.get(mSelectedPosi);
                    int lastSelectPosi = menuItemData.setChildSelected(position);
                    View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + lastSelectPosi);
                    if (oldSubMenuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateSubMenuItem(oldSubMenuItemView, menuItemData.getChildAt(lastSelectPosi));
                    }

                    View subMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + position);
                    if (subMenuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateSubMenuItem(subMenuItemView, menuItemData.getSelectedChild());
                    }
                    View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedPosi);
                    if (menuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
                    }
                    performSubmenuClickEvent(menuItemData.getSelectedChild(), position);
                    if (mSelectedPosi == 3 || mSelectedPosi == 4) {
                        if (mSelectedPosi == 3) {
                            if (position != 0) {
                                MenuItem menuItem = list.get(4);
                                int outSelectPos = menuItem.setChildSelected(0);
                                View outMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 4);
                                if (outMenuItemView != null) {
                                    mMenuDialog.getMenuAdapter().updateSubTitle(outMenuItemView, menuItemData, false);
                                }
                            }
                        } else {
                            if (position != 0) {
                                MenuItem menuItem = list.get(3);
                                int inSelectPos = menuItem.setChildSelected(0);
                                View inMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 3);
                                if (inMenuItemView != null) {
                                    mMenuDialog.getMenuAdapter().updateSubTitle(inMenuItemView, menuItemData, true);
                                }
                            }
                        }
                    }
                }

                @Override
                public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
                    if (mSelectedPosi == position) {
                        return false;
                    }
                    return false;
                }
            });
            mMenuDialog.setOnItemKeyEventListener(new OnKeyEventListener() {

                @Override
                public boolean onMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN && mSelectedPosi == 0) {
                        mMenuDialog.getMenu().openSubMenu(true, list.get(0).getSelectedChildIndex());
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onSubMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    //修复OS-3900浏览音频视频和图片文件时，点击菜单按钮，在显示的文件列表页，不支持循环选择
                    //去掉二级菜单列表循环选择，修复OS-4989在本地播放列表中，光标在顶端按上，光标会消失一下然后移动到最下面
                    Log.i(TAG, "onKeyDown: " + TAG);
                    /*if (mSelectedPosi == 0) {
                        mCurrentSubMenuList = playListSubMenuItems;
                    } else {
                        return false;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (mCurrentSubMenuPos == mCurrentSubMenuList.size() - 1) {
                            mMenuDialog.showSubMenuFocus(false);
                            mMenuDialog.getMenu().focusSubMenuItem2(list.get(0).getSelectedIndex(true));
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
                            mMenuDialog.getMenu().focusSubMenuItem2(list.get(0).getSelectedIndex(false));
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
                    }*/
                    return false;
                }
            });
        }
        //修复OS-3286进入本地播放的视频或音频时，第一次按菜单键呼出菜单栏时，焦点光标从最上方的空白处移动到正在播放的节目上
        mMenuDialog.showSubMenuFocus(false);
        if (mSelectedPosi == 0) {
            list.get(0).setChildSelected(mCurPlayIndex);
            mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            mMenuDialog.getMenu().focusSubMenuItem2(list.get(0).getSelectedChildIndex());
        }
        mMenuDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMenuDialog.showSubMenuFocus(true);
            }
        }, 1200);
    }

    private void performSubmenuClickEvent(MenuItem mSubSelectedMenu, int position) {
        switch (mSubSelectedMenu.getParent().getType()) {
            case MenuItem.TYPE_LIST:
                performTypeListEvent(mSubSelectedMenu, position);
                break;
            case MenuItem.TYPE_NORMAL:
                performTypeNormalEvent(mSubSelectedMenu);
                break;
            case MenuItem.TYPE_SELECTOR:
                performTypeSelectedEvent(mSubSelectedMenu, position);
                break;
            case MenuItem.TYPE_SELECTOR_MARQUEE:
                performTypeSelectedEvent(mSubSelectedMenu, position);
                break;
        }

    }

    private void performTypeNormalEvent(MenuItem mSubSelectedMenu) {
        switch (mSubSelectedMenu.getTitle()) {
            case MenuConstant.SUBMENU_ADJUSTSUBTITLE_FORWORD:
                mMoveTime += 200;
                ToastUtils.showMessage(MyApplication.getContext(), "提前0.2秒");
                break;
            case MenuConstant.SUBMENU_ADJUSTSUBTITLE_BACKWORD:
                mMoveTime -= 200;
                ToastUtils.showMessage(MyApplication.getContext(), "延迟0.2秒");
                break;

        }

    }

    private void performTypeListEvent(MenuItem mSubSelectedMenu, int position) {
        if (position == mCurPlayIndex) {
            return;
        }
        storeDuration();    //切换视频前先将上个视频记录保存
        playMedia(position);
    }

    private void performTypeSelectedEvent(MenuItem mSubSelectedMenu, int position) {
        //修复切换多个音轨无变化
        if (mSubSelectedMenu.getTitle().contains(MenuConstant.SUBMENU_AUDIOTRACKER)) {
            getProxyPlayer().setMovieAudioTrack(position);
            return;
        }
        if (mSelectedPosi == 3) {
            mInsubTitleIndex = position;
            //添加内嵌字幕控制
            if (position == 0) {
                //关闭内嵌字幕
                openOrCloseSubTitle(false, -1, null, -1);
            } else {
                //打开内嵌字幕，关闭外挂字幕
                openOrCloseSubTitle(true, mInsubTitleIndex, null, -1);
            }
            return;
        }

        if (mSelectedPosi == 4) {
            mOutsubTitleIndex = position;
            //添加外挂字幕控制
            if (position == 0) {
                //关闭外挂字幕
                openOrCloseSubTitle(null, -1, false, -1);
            } else {
                //打开外挂字幕，关闭内嵌字幕
                openOrCloseSubTitle(null, -1, true, mOutsubTitleIndex);
            }
            return;
        }

        switch (mSubSelectedMenu.getTitle()) {
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
            case MenuConstant.SUBMENU_IMAGESCALE_21_9:
                mSurfaceView.setShowType(ShowType.WIDTH_HEIGHT_21_9);
                break;
            case MenuConstant.SUBMENU_LOADINGSUBTITLE_CLOSE:
                isSubTitle = false;
                mSubTitle.setText("");
                if (list.get(4) != null && "字幕调整".equals(list.get(4).getTitle())) {
                    list.get(4).setEnabled(isSubTitle);

                    View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 4);
                    if (oldSubMenuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateMenuItem(oldSubMenuItemView, list.get(4));
                        mMenuDialog.getMenuAdapter().updateVideoMenuItem(oldSubMenuItemView, list.get(4), true);
                    }
                }
                break;
            case MenuConstant.SUBMENU_LOADINGSUBTITLE_OPEN:
                isSubTitle = true;
                mSubTitle.setText("");
                if (list.get(4) != null && "字幕调整".equals(list.get(4).getTitle())) {
                    list.get(4).setEnabled(isSubTitle);
                    View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 4);
                    if (oldSubMenuItemView != null) {
                        mMenuDialog.getMenuAdapter().updateMenuItem(oldSubMenuItemView, list.get(4));
                        mMenuDialog.getMenuAdapter().updateVideoMenuItem(oldSubMenuItemView, list.get(4), false);
                    }
                }
                break;
            default:
                break;
        }
    }

    private List<MenuItem> createMenuData() {
        List<MenuItem> menuList = new ArrayList<>();
        //mCurrentSubMenuList = new ArrayList<>();
        // 播放列表
        MenuItem playListMenuItem = new MenuItem("播放列表");
        playListMenuItem.setType(MenuItem.TYPE_LIST);
        playListMenuItem.setSelected(true);
        playListSubMenuItems = new ArrayList<>();
        for (int i = 0; i < mDataList.size(); i++) {
            MenuItem item = new MenuItem(mDataList.get(i).mName);
            item.setType(MenuItem.TYPE_SELECTOR_MARQUEE);
            playListSubMenuItems.add(item);
        }
        playListMenuItem.setChildren(playListSubMenuItems);
        menuList.add(playListMenuItem);
        playListMenuItem.setChildSelected(mCurPlayIndex);

        // 音轨设置
        MenuItem audioTrackMenuItem = new MenuItem("音轨设置");
        audioTrackMenuItem.setType(MenuItem.TYPE_SELECTOR);
        List<MenuItem> audioTrackMenuItems = new ArrayList<MenuItem>();
        String[] tracks = getCurVideoAudioTracks();
        for (int i = 0; i < tracks.length; i++) {
            MenuItem item = new MenuItem(tracks[i]);
            item.setType(MenuItem.TYPE_SELECTOR);
            audioTrackMenuItems.add(item);
            if (i == 0) {
                item.setSelected(true);
                audioTrackMenuItem.setSelectedChild(item);
            }
        }

        audioTrackMenuItem.setChildren(audioTrackMenuItems);
        menuList.add(audioTrackMenuItem);

        // 画面比例
        MenuItem imageScaleMenuItem = new MenuItem("画面比例", MenuItem.TYPE_SELECTOR);
        List<MenuItem> imageScaleMenuItems = new ArrayList<>();
        imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_ORIGINAL, MenuItem.TYPE_SELECTOR));
        imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_FULL, MenuItem.TYPE_SELECTOR));
        imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_4_3, MenuItem.TYPE_SELECTOR));
        //修复OS-3901播放本地视频时，画面比例默认为16:9，目前默认为原始比例
        MenuItem imageScaleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_16_9, MenuItem.TYPE_SELECTOR);
        imageScaleMenuItem.setSelectedChild(imageScaleSubMenuOriginal);
        imageScaleSubMenuOriginal.setSelected(true);
        imageScaleMenuItems.add(imageScaleSubMenuOriginal);
        imageScaleMenuItems.add(new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_21_9, MenuItem.TYPE_SELECTOR));
        imageScaleMenuItem.setChildren(imageScaleMenuItems);
        menuList.add(imageScaleMenuItem);

        //内嵌字幕
        MenuItem inSubtitleMenuItem = new MenuItem("内嵌字幕", MenuItem.TYPE_SELECTOR);
        List<MenuItem> inSubtitleMenuItems = new ArrayList<MenuItem>();
        List<String> inSubList = getProxyPlayer().getINSubList();
        for (int i = 0; i < inSubList.size(); i++) {
            String substring = inSubList.get(i).substring(2);
            MenuItem item = null;
            if (substring.equals("und")) {
                subName = MenuConstant.SUBMENU_INSUBTITLE;
                item = new MenuItem(subName + (i + 1));
            } else {
                subName = substring;
                item = new MenuItem(subName);
            }
            item.setType(MenuItem.TYPE_SELECTOR);
            inSubtitleMenuItems.add(item);
            if (i == 0) {
                item.setSelected(true);
                inSubtitleMenuItem.setSelectedChild(item);
            }
        }
        MenuItem inSubtitleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_SUBTITLE, MenuItem.TYPE_SELECTOR);
        if (null != inSubList && inSubList.size() == 0) {
            inSubtitleMenuItem.setSelectedChild(inSubtitleSubMenuOriginal);
            inSubtitleSubMenuOriginal.setSelected(true);
        }
        inSubtitleMenuItems.add(0, inSubtitleSubMenuOriginal);
        inSubtitleMenuItem.setChildren(inSubtitleMenuItems);
        menuList.add(inSubtitleMenuItem);

        //外挂字幕
        MenuItem outSubtitleMenuItem = new MenuItem("外挂字幕", MenuItem.TYPE_SELECTOR);
        MenuItem outSubtitleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_SUBTITLE, MenuItem.TYPE_SELECTOR);
        outSubtitleMenuItem.setSelectedChild(outSubtitleSubMenuOriginal);
        outSubtitleSubMenuOriginal.setSelected(true);
        List<MenuItem> outSubtitleMenuItems = new ArrayList<MenuItem>();
        outSubtitleMenuItems.add(0, outSubtitleSubMenuOriginal);
        List<String> externalSubList = getExternalSubList();
        for (int i = 0; i < externalSubList.size(); i++) {
            MenuItem item = new MenuItem(MenuConstant.SUBMENU_OUTSUBTITLE + (i + 1));
            item.setType(MenuItem.TYPE_SELECTOR);
            outSubtitleMenuItems.add(item);
        }
        outSubtitleMenuItem.setChildren(outSubtitleMenuItems);
        menuList.add(outSubtitleMenuItem);

        // 调整字幕
        MenuItem adjustSubtitleMenuItem = new MenuItem("字幕调整", MenuItem.TYPE_NORMAL);
        List<MenuItem> adjustSubtitlesSubMenus = new ArrayList<MenuItem>();
        adjustSubtitlesSubMenus.add(new MenuItem(MenuConstant.SUBMENU_ADJUSTSUBTITLE_FORWORD, MenuItem.TYPE_NORMAL));
        adjustSubtitlesSubMenus.add(new MenuItem(MenuConstant.SUBMENU_ADJUSTSUBTITLE_BACKWORD, MenuItem.TYPE_NORMAL));
        adjustSubtitleMenuItem.setChildren(adjustSubtitlesSubMenus);
        menuList.add(adjustSubtitleMenuItem);

        return menuList;
    }

    private List<MenuItem> createAudioTrackList() {
        String[] tracks = getCurVideoAudioTracks();
        List<MenuItem> audioTrackMenuItems = new ArrayList<MenuItem>();
        for (int i = 0; i < tracks.length; i++) {
            MenuItem item = new MenuItem(tracks[i]);
            item.setType(MenuItem.TYPE_SELECTOR);
            audioTrackMenuItems.add(item);
        }
        return audioTrackMenuItems;
    }

    private List<MenuItem> getInSubtitleList() {
        List<MenuItem> inSubtitleMenuItems = new ArrayList<MenuItem>();
        List<String> inSubList = getProxyPlayer().getINSubList();
        for (int i = 0; i < inSubList.size(); i++) {
            String substring = inSubList.get(i).substring(2);
            MenuItem item = null;
            if (substring.equals("und")) {
                subName = MenuConstant.SUBMENU_INSUBTITLE;
                item = new MenuItem(subName + (i + 1));
            } else {
                subName = substring;
                item = new MenuItem(subName);
            }
            item.setType(MenuItem.TYPE_SELECTOR);
            inSubtitleMenuItems.add(item);
        }
        MenuItem inSubtitleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_SUBTITLE, MenuItem.TYPE_SELECTOR);
        inSubtitleMenuItems.add(0, inSubtitleSubMenuOriginal);
        return inSubtitleMenuItems;
    }

    private List<MenuItem> getOutSubtitleList() {
        MenuItem outSubtitleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_SUBTITLE, MenuItem.TYPE_SELECTOR);
        List<MenuItem> outSubtitleMenuItems = new ArrayList<MenuItem>();
        outSubtitleMenuItems.add(0, outSubtitleSubMenuOriginal);
        List<String> externalSubList = getExternalSubList();
        for (int i = 0; i < externalSubList.size(); i++) {
            MenuItem item = new MenuItem(MenuConstant.SUBMENU_OUTSUBTITLE + (i + 1));
            item.setType(MenuItem.TYPE_SELECTOR);
            outSubtitleMenuItems.add(item);
        }
        return outSubtitleMenuItems;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCtrBar != null) {
            mCtrBar.removeAllMessage();
        }
        if (mTimeReceiver != null) {
            unregisterReceiver(mTimeReceiver);
        }
        EventBus.getDefault().unregister(this);
        MyApplication.removeActivity(this);
    }

    private void registerTimeReceiver() {
        mTimeReceiver = new TimeReceiver();
        mTimeFilter = new IntentFilter();
        mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, mTimeFilter);
    }

    //内置字幕监听，现在可以支持内置、外挂字幕了
    @Override
    public void onTimedText(MediaPlayer mp, TimedText text) {
        mSubtitle_bt.setVisibility(View.GONE);
        mSubTitle.setVisibility(View.VISIBLE);
        if (null == text) {
            return;
        }
        //如果同时打开两个字幕则会显示重复，所打开外挂后内置字幕默认为关闭状态
        if (mOpenInSubtitle) {
            mSubTitle.setText(text.getText());
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isOnPrepared = true;
    }


    //内置字幕监听，现在可以支持内置、外挂字幕了

    class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mCtrBar.refreshTime(); // 更新时间的方法
            }
        }
    }

    @Override
    protected void onPause() {
        StatisticsUtil.registerPause(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        releaseWakeLock();  //释放屏保
        storeDuration();    //保存进度
        //修复os-5051,5010.4211,列表视频名称出现重复问题
        if (mMenuDialog != null && mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }
        //为了处理从不同的入口进入文件管理器,出现的类型错乱,如：从视频入口进入，按home键,再从图片进入,显示的还是视频类型
//        if (!isPressback && !(MyApplication.mHomeActivityList.size() > 0)) {
//            MyApplication.onFinishActivity();
//        }
        super.onStop();
    }


    @Override
    public void onSubTitleChanging() {

    }

    @Override
    public void showSubTitleText(final String text) {
        if (!mOpenExternalSubtitle || !mLastStr.toLowerCase().contains("ass")) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSubtitle_bt.setVisibility(View.GONE);
                mSubTitle.setVisibility(View.VISIBLE);
                mSubTitle.setText(text);
            }
        });
    }

    @Override
    public void showSubTitleBit(final StContent stContent) {
        if (!mOpenExternalSubtitle || !mLastStr.toLowerCase().contains("sub")) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSubTitle.setVisibility(View.GONE);
                mSubtitle_bt.setVisibility(View.VISIBLE);
                mSubtitle_bt.setImageBitmap(stContent.getSubtitleBmp());
            }
        });
    }

    /**
     * @param inOpen       true,内置字幕开;false,内置字幕关
     * @param subIndex     内置字幕开时,传入一个索引(内置字幕集合对应的值);内置字幕关闭时,不再检测这个值
     * @param ExternalOpen true,外置字幕开;false,外置字幕关
     * @param index
     */
    private void openOrCloseSubTitle(Boolean inOpen, int subIndex, Boolean ExternalOpen, int index) {
        if (null != inOpen) {
            mOpenInSubtitle = inOpen;
            mSubTitle.setText("");
            mSubtitle_bt.setVisibility(View.GONE);
            if (mOpenInSubtitle) {
                mOpenExternalSubtitle = false;
                getProxyPlayer().selectTrackInfo(Integer.parseInt(getProxyPlayer().getINSubList().get(subIndex - 1).substring(0, 1)));
            }
            return;
        }
        if (null != ExternalOpen) {
            mOpenExternalSubtitle = ExternalOpen;
            mSubTitle.setText("");
            mSubtitle_bt.setVisibility(View.GONE);
            if (mOpenExternalSubtitle) {
                mOpenInSubtitle = false;
                mLastStr = getExternalSubList().get(index - 1);
                if (!mLastStr.endsWith("srt")) {
                    if (mLastStr.endsWith(".sub")) {
                        String idxPath = mLastStr.substring(0, mLastStr.lastIndexOf(".")) + ".idx";
                        File file1 = new File(idxPath);
                        if (file1.exists() && file1.canRead()) {
                            getProxyPlayer().setSubPath(mLastStr);
                        } else {
                            Log.w("设置sub字幕", "");
                            parseSub(mLastStr);
                        }
                    } else {
                        getProxyPlayer().setSubPath(mLastStr);
                    }
                }
            }
        }

    }

    /**
     * 获取外置字幕
     *
     * @return
     */
    private List<String> getExternalSubList() {
        String path = mDataList.get(mCurPlayIndex).isSharing ? "" : mDataList.get(mCurPlayIndex).mUri;
        ArrayList<String> savePathList = new ArrayList<>();
        Log.i("shen", "getExternalSubList: " + path.length());
        if (TextUtils.isEmpty(path)) {
            return savePathList;
        }
        String stPath = path.substring(0, path.lastIndexOf("."));
        List<String> pathList = Arrays.asList(stPath + ".srt", stPath + ".ass", stPath + ".ssa", stPath + ".sub");   // stPath + ".smi", stPath + ".sub"
        //savePathList.add("无");
        for (int i = 0; i < pathList.size(); i++) {
            File file = new File(pathList.get(i));
            if (file.exists() && file.canRead()) {
//                if (pathList.get(i).endsWith(".sub")) { //sub需要和idx一起使用
//                    File file1 = new File(stPath + ".idx");
//                    if (file1.exists() && file1.canRead()) {
//                        savePathList.add(pathList.get(i));
//                    }
//                } else {
                savePathList.add(pathList.get(i));
//                }
            }
        }
        return savePathList;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * 移除外接设备的监听
     *
     * @param usbMounted
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbMounted(UsbMounted usbMounted) {
        if (!usbMounted.mIsRemoved) {
            return;
        }

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

}
