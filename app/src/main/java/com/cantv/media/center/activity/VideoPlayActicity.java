package com.cantv.media.center.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.TimedText;
import android.os.Bundle;
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

import com.cantv.liteplayer.core.audiotrack.AudioTrack;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.greendao.DaoOpenHelper;
import com.cantv.media.center.greendao.VideoPlayer;
import com.cantv.media.center.ui.DoubleColumnMenu;
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.DoubleColumnMenu.OnKeyEventListener;
import com.cantv.media.center.ui.ExternalSurfaceView;
import com.cantv.media.center.ui.ExternalSurfaceView.ShowType;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.ui.player.BasePlayer;
import com.cantv.media.center.ui.player.PlayerController;
import com.cantv.media.center.ui.player.SrcParser;
import com.cantv.media.center.utils.MediaUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayActicity extends BasePlayer implements OnVideoSizeChangedListener, OnTimedTextListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        if (mDataList == null || mDataList.size() == 0) {
            return;
        }
        acquireWakeLock();// 禁止屏保弹出
        initView();
        registerTimeReceiver();
        MyApplication.addActivity(this);
    }

    private void initView() {
        mSubTitle = (TextView) findViewById(R.id.media__video_view__subtitle1);
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
        //解决内置字幕切换后不消失
        initSrts();
        //解决内置字幕切换后不消失
        getProxyPlayer().setMovieSubTitle(0);
        getProxyPlayer().setMovieAudioTrack(0);
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
    protected void runProgressBar() {

        String path = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
        String srtUrl = checkSrt();
        mCtrBar.setPlayDuration();
        List<VideoPlayer> list = DaoOpenHelper.getInstance(this).queryInfo(path);

        if (list.size() != 0) {
            mRecord = list.get(0);
            final int positon = list.get(0).getPosition();
            if (positon > 1000) {
                if (positon < getPlayerDuration()) {
                    //当当前时长小于视频总时长时
                    mCtrBar.showContinuePaly(positon);
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
            MenuItem audioTrackMenuItem = VideoPlayActicity.this.list.get(1);
            audioTrackMenuItem.setChildren(createAudioTrackList());
            audioTrackMenuItem.setChildSelected(0);
            View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + 1);
            if (menuItemView != null) {
                mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, audioTrackMenuItem);
            }
            if (mSelectedPosi == 1) {
                mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            }
        }
    }

    public String checkSrt() {
        String url = mDataList.get(mCurPlayIndex).isSharing ? mDataList.get(mCurPlayIndex).sharePath : mDataList.get(mCurPlayIndex).mUri;
        final String srt = url.substring(0, url.indexOf(".")) + ".srt";

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

            default:
                break;
        }
        if (mCtrBar != null) {
            mCtrBar.onKeyDownEvent(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mCtrBar != null) {
            mCtrBar.onKeyUpEvent(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
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

    public void initSrts() {
        mSubTitle.setText("");
    }

    //字幕修改
    public void setSrts(int time) {

        if (!isSubTitle) {
            return;
        }

        time += mMoveTime;
        final String srtByTime = parser.getSrtByTime(time);
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
        if(mMenuDialog != null){
            if (mMenuDialog.isShowing() ) {
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
            super.onCompletion(arg0);
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
                    return false;
                }
            });
        }
        if (mSelectedPosi == 0) {
            list.get(0).setChildSelected(mCurPlayIndex);
            mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
            mMenuDialog.getMenu().focusSubMenuItem2(list.get(0).getSelectedChildIndex());
        }
        mMenuDialog.show();
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

    private void performTypeSelectedEvent(MenuItem mSubSelectedMenu, int positon) {

        //修复切换多个音轨无变化
        if (mSubSelectedMenu.getTitle().contains(MenuConstant.SUBMENU_AUDIOTRACKER)) {
            getProxyPlayer().setMovieAudioTrack(positon);
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
        List<MenuItem> menuList = new ArrayList<MenuItem>();
        // 播放列表
        MenuItem playListMenuItem = new MenuItem("播放列表");
        playListMenuItem.setType(MenuItem.TYPE_LIST);
        playListMenuItem.setSelected(true);
        List<MenuItem> playListSubMenuItems = new ArrayList<MenuItem>();
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
        MenuItem imageScaleSubMenuOriginal = new MenuItem(MenuConstant.SUBMENU_IMAGESCALE_ORIGINAL, MenuItem.TYPE_SELECTOR);
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
        MenuItem subtitlesLoadingSubMenuItemOpen = new MenuItem(MenuConstant.SUBMENU_LOADINGSUBTITLE_OPEN, MenuItem.TYPE_SELECTOR);
        subtitlesLoadingSubMenuItemOpen.setSelected(true);
        subtitlesLoadingMenuItem.setSelectedChild(subtitlesLoadingSubMenuItemOpen);
        MenuItem subtitlesLoadingSubMenuItemColse = new MenuItem(MenuConstant.SUBMENU_LOADINGSUBTITLE_CLOSE, MenuItem.TYPE_SELECTOR);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCtrBar != null) {
            mCtrBar.removeAllMessage();
        }
        if (mTimeReceiver != null) {
            unregisterReceiver(mTimeReceiver);
        }
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
        if (null == text) {
            return;
        }
        //如果同时打开两个字幕则会显示重复，所打开外挂后内置字幕默认为关闭状态
        if (!isSubTitle) {
            mSubTitle.setText(text.getText());
        }
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
    protected void onStop() {
        //为了处理从不同的入口进入文件管理器,出现的类型错乱,如：从视频入口进入，按home键,再从图片进入,显示的还是视频类型
        if (!isPressback && !(MyApplication.mHomeActivityList.size() > 0)) {
            MyApplication.onFinishActivity();
        }
        super.onStop();
    }
}
