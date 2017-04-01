package com.cantv.media.center.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.MediaOrientation;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Constant;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.data.YSourceType;
import com.cantv.media.center.ui.dialog.ConfirmDialog;
import com.cantv.media.center.ui.dialog.DoubleColumnMenu;
import com.cantv.media.center.ui.dialog.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.dialog.LoadingDialog;
import com.cantv.media.center.ui.dialog.MenuDialog;
import com.cantv.media.center.ui.dialog.MenuDialog.MenuAdapter;
import com.cantv.media.center.ui.directory.MediaGridView;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 文件列表页面
 */
public class GridViewActivity extends Activity {
    private static String TAG = "GridViewActivity";
    private RelativeLayout mContentView;
    private TextView mTitleTV;
    private MediaGridView mGridView;
    private MenuDialog mMenuDialog;
    private List<MenuItem> list;
    private MenuItem sortListMenuItem;
    private MenuItem viewModeMenuItem;
    private MenuItem deleteMenuItem;
    private MenuItem copyFileMenuItem;
    private MenuItem pasteFileMenuItem;
    private MenuItem sortMenuItem;
    private MenuItem viewItem;
    private MenuItem mSortMenu;
    private List<MenuItem> sortListSubMenuItems;
    private List<MenuItem> viewModeSubMenuItems;
    private List<MenuItem> mMenuList;
    private int mSelectedMenuPosi;
    private int mDeleteItem;
    public boolean isExternal; // 记录当前是否处于外接设备,true:处于外接设备(通过USB接口接入)
    public TextView mRTCountView; // 显示数量和当前选中position
    public int mCurrGridStyle; // 记录当前是什么排列方式
    private ConfirmDialog mConfirmDialog;
    private boolean mClickDelete = false;
    public boolean isStartAc;   //用来判断是否处于打开二级activity中
    public static String mType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        MyApplication.onFinishActivity();
        MyApplication.addActivity(this);
        EventBus.getDefault().register(this);
        mTitleTV = (TextView) findViewById(R.id.title_textview);
        mContentView = (RelativeLayout) findViewById(R.id.gridview_content);
        mCurrGridStyle = SharedPreferenceUtil.getGridStyle();
        mRTCountView = (TextView) findViewById(R.id.file_count);
        initData();
    }

    /**
     * 获取数据
     */
    private void initData() {
        getType();
        if (mGridView == null) {
            return;
        }
        mGridView.show();
        mContentView.removeAllViews();
        switch (SharedPreferenceUtil.getGridStyle()) {
            case 1:
                setGridStyle(MediaOrientation.THUMBNAIL);
                break;
            case 0:
                setGridStyle(MediaOrientation.LIST);
                break;
        }
        mContentView.addView(mGridView);
        //此处为了修复OS-3938的bug
        if (mType.equalsIgnoreCase(Constant.MEDIA_IMAGE_SPE)
                || mType.equalsIgnoreCase(Constant.MEDIA_VIDEO_SPE)
                || mType.equalsIgnoreCase(Constant.MEDIA_AUDIO_SPE)) {
            MyApplication.onFinishHomeActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartAc = false;

        StatisticsUtil.registerResume(this);
    }

    @Override
    protected void onPause() {
        StatisticsUtil.registerPause(this);
        super.onPause();
    }

    //修復OS-3825从发现设备弹窗入口和媒体中心入口进入外接设备浏览本地文件过程中按设置键，再按返回键退出设置后，文件管理器直接退出到入口界面
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initData();
    }

    /**
     * 得到类型
     */
    private void getType() {
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        if (Constant.MEDIA_VIDEO_SPE.equalsIgnoreCase(mType) || Constant.MEDIA_VIDEO.equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_movie);
            mGridView = new MediaGridView(this, SourceType.MOIVE);
            isExternal = true;
            StatisticsUtil.customEvent(GridViewActivity.this, "video_page");
        } else if (Constant.MEDIA_IMAGE_SPE.equalsIgnoreCase(mType) || Constant.MEDIA_IMAGE.equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_photo);
            mGridView = new MediaGridView(this, SourceType.PICTURE);
            isExternal = true;
            StatisticsUtil.customEvent(GridViewActivity.this, "picture_page");
        } else if (Constant.MEDIA_AUDIO_SPE.equalsIgnoreCase(mType) || Constant.MEDIA_AUDIO.equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_music);
            mGridView = new MediaGridView(this, SourceType.MUSIC);
            isExternal = true;
            StatisticsUtil.customEvent(GridViewActivity.this, "music_page");
        } else if ("app".equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_app);
            mGridView = new MediaGridView(this, SourceType.APP);
            isExternal = true;
            StatisticsUtil.customEvent(GridViewActivity.this, "install_page");
        } else if ("local".equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_file);
            mGridView = new MediaGridView(this, SourceType.LOCAL);
            isExternal = false;
            StatisticsUtil.customEvent(GridViewActivity.this, "local_page");
        } else if ("device1".equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_external);
            mGridView = new MediaGridView(this, SourceType.DEVICE);
            if (MediaUtils.getUSBNum() > 0 && MediaUtils.getUSBNum() < 3) {
                if (null != getIntent().getStringExtra("toListFlag")) {

                } else {
                    mGridView.setDevicePath(MediaUtils.getCurrPathList().get(0));
                }
            }
            isExternal = true;
            StatisticsUtil.customEvent(GridViewActivity.this, "usb_page");
        } else if ("device2".equalsIgnoreCase(mType)) {
            mTitleTV.setText(R.string.str_external);
            mGridView = new MediaGridView(this, SourceType.DEVICE);
            if (MediaUtils.getUSBNum() > 1) {
                mGridView.setDevicePath(MediaUtils.getCurrPathList().get(1));
            }
            isExternal = true;
            StatisticsUtil.customEvent(GridViewActivity.this, "usb_page");
        } else if ("share".equalsIgnoreCase(mType)) {
            mTitleTV.setText(intent.getStringExtra("title"));
            mGridView = new MediaGridView(this, SourceType.SHARE);
            mGridView.setDevicePath(intent.getStringExtra("path"));
            isExternal = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (8 == keyCode || 166 == keyCode) {
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (null == mMenuDialog || !mMenuDialog.isShowing()) {
                mGridView.setStyleFocus(R.drawable.unfocus);
            } else {
                mGridView.setDefaultStyle();
            }
            showMenuDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    public void setGridStyle(MediaOrientation mStyle) {
        switch (mStyle) {
            case LIST:
                mGridView.setVerticalSpacing((int) getResources().getDimension(R.dimen.px5));
                mGridView.setPadding(0, (int) getResources().getDimension(R.dimen.px30), 0, (int) getResources().getDimension(R.dimen.px5));
                mGridView.setStyle(MediaOrientation.LIST);
                mGridView.setNumColumns(1);
                break;
            case THUMBNAIL:
                mGridView.setVerticalSpacing((int) getResources().getDimension(R.dimen.px15));
                mGridView.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.px60));
                mGridView.setStyle(MediaOrientation.THUMBNAIL);
                mGridView.setNumColumns(5);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        MediaGridView childGridView = (MediaGridView) mContentView.getChildAt(0);
        if ((null != childGridView) && (!childGridView.onBack())) {
            finish();
        } else {
            return;
        }
    }

    private void showMenuDialog() {

        if (mMenuDialog == null) {
            mMenuDialog = new MenuDialog(this);
            list = createMenuData();
            mMenuDialog.setMenuList(list);
            mMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mClickDelete) {
                        mClickDelete = false;
                        mGridView.setStyleFocus(R.drawable.unfocus);
                    } else {
                        mGridView.setDefaultStyle();
                    }
                }
            });
            mMenuDialog.setOnItemFocusChangeListener(new DoubleColumnMenu.OnItemFocusChangeListener() {
                @Override
                public void onMenuItemFocusChanged(LinearLayout leftViewGroup, View view, int position, boolean hasFocus) {
                    if (position != 2) {
                        if (mSelectedMenuPosi == position) {
                            return;
                        }
                    }
                    if (position == 2 || position == 3 || position == 4) {
                        mMenuDialog.closeSubMenuItem();
                    }
                    if (position != 2 && position != 3 && position != 4) {
                        mMenuDialog.openSubMenuItem();
                    }
                    mMenuList.get(mSelectedMenuPosi).setSelected(false);

                    View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedMenuPosi);

                    mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, mMenuList.get(mSelectedMenuPosi));

                    mSelectedMenuPosi = position;
                    MenuItem menuItem = mMenuList.get(position);
                    menuItem.setSelected(true);
                    mMenuDialog.getMenuAdapter().updateMenuItem(view, menuItem);
                    mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();

                }

                @Override
                public void onSubMenuItemFocusChanged(LinearLayout rightViewGroup, View view, int position, boolean hasFocus) {

                }
            });

            mMenuDialog.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onSubMenuItemClick(LinearLayout parent, View view, int position) {
                    subMenuClick(position);
                }

                @Override
                public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
//                    if (position != 2) {
//                        if (mSelectedMenuPosi == position) {
//                            return false;
//                        }
//                    }

                    if (position == 2) {
                        String[] pathList = SharedPreferenceUtil.getDevicesPath().split("abc");
                        if (FileUtil.isListConOtListValue(FileUtil.getListFromList(mGridView.mListAdapter.getData()), FileUtil.arrayToList(pathList))) {
                            Toast.makeText(getApplicationContext(), "外接设备不能删除", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        mClickDelete = true;
                        mDeleteItem = mGridView.mSelectItemPosition;
                        List<Media> datas = mGridView.mListAdapter.getData();
                        mMenuDialog.dismiss();
                        //hideFocus();
                        deleteItem(datas);
                        return true;
                    } else if (position == 3) { //复制
                        mMenuDialog.dismiss();
                        if (null != mGridView) {
                            mGridView.copyPasteFile(true, new MediaGridView.YPasteListener() {
                                @Override
                                public void onStartPaste() {

                                }

                                @Override
                                public void onPasteFailed() {

                                }

                                @Override
                                public void onPasteSucceed() {

                                }

                                @Override
                                public void onRefreshList(String path) {

                                }
                            });
                        }
                        return true;
                    } else if (position == 4) { //粘贴
                        mMenuDialog.dismiss();
                        if (null != mGridView) {
                            mGridView.copyPasteFile(false, new MediaGridView.YPasteListener() {
                                @Override
                                public void onStartPaste() {
                                    showLoadingDialog();
                                }

                                @Override
                                public void onPasteFailed() {
                                    hideLoadingDialog();
                                }

                                @Override
                                public void onPasteSucceed() {
                                    hideLoadingDialog();
                                }

                                @Override
                                public void onRefreshList(String currPath) {
                                    refreshCurrList(currPath);
                                }
                            });
                        }
                        return true;
                    } else {
                        return false;
                    }
                    //return false;
                }
            });
        } else {

        }
        mMenuDialog.show();
    }

    private LoadingDialog mLoadingDialog;

    public void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setLoadingText("正在粘贴,请稍后...");
        }
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();

    }

    public void hideLoadingDialog() {
        if (null != mLoadingDialog) {
            mLoadingDialog.dismiss();
        }
    }


    private List<MenuItem> createMenuData() {
        mMenuList = new ArrayList<>();
        sortListMenuItem = new MenuItem(getString(R.string.sort));
        sortListMenuItem.setType(MenuItem.TYPE_SELECTOR);
        sortListMenuItem.setSelected(true);
        sortListSubMenuItems = new ArrayList<>();
        sortMenuItem = new MenuItem(getString(R.string.sort_date), MenuItem.TYPE_SELECTOR);
        sortListSubMenuItems.add(sortMenuItem);
        sortListSubMenuItems.add(new MenuItem(getString(R.string.sort_filesize), MenuItem.TYPE_SELECTOR));
        sortListSubMenuItems.add(new MenuItem(getString(R.string.sort_name), MenuItem.TYPE_SELECTOR));
        int sortType = SharedPreferenceUtil.getSortType();
        if (sortType == 1 || sortType == 2) {
            mSortMenu = sortListSubMenuItems.get(0);
        } else if (sortType == 3 || sortType == 4) {
            mSortMenu = sortListSubMenuItems.get(1);
        } else if (sortType == 5 || sortType == 6) {
            mSortMenu = sortListSubMenuItems.get(2);
        }
        mSortMenu.setParent(sortListMenuItem);
        mSortMenu.setSelected(true);
        sortListMenuItem.setChildren(sortListSubMenuItems);
        mMenuList.add(sortListMenuItem);
        viewModeMenuItem = new MenuItem(getString(R.string.view));
        viewModeMenuItem.setType(MenuItem.TYPE_SELECTOR);
        viewModeSubMenuItems = new ArrayList<MenuItem>();
        viewItem = new MenuItem(getString(R.string.view_list), MenuItem.TYPE_SELECTOR);
        viewModeSubMenuItems.add(viewItem);
        viewModeSubMenuItems.add(new MenuItem(getString(R.string.view_tile), MenuItem.TYPE_SELECTOR));
        int gridStyle = SharedPreferenceUtil.getGridStyle();
        MenuItem viewMenu = viewModeSubMenuItems.get(gridStyle);
        viewMenu.setParent(viewModeMenuItem);
        viewMenu.setSelected(true);
        viewModeMenuItem.setChildren(viewModeSubMenuItems);
        mMenuList.add(viewModeMenuItem);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if (!"share".equalsIgnoreCase(type)) {
            deleteMenuItem = new MenuItem(getString(R.string.delete));
            deleteMenuItem.setType(MenuItem.TYPE_NORMAL);
            mMenuList.add(deleteMenuItem);
        }
        if (!"share".equalsIgnoreCase(type)) {
            //复制item
            copyFileMenuItem = new MenuItem(getString(R.string.file_copy));
            copyFileMenuItem.setType(MenuItem.TYPE_NORMAL);
            mMenuList.add(copyFileMenuItem);
            //粘贴item
            pasteFileMenuItem = new MenuItem(getString(R.string.file_paste));
            pasteFileMenuItem.setType(MenuItem.TYPE_NORMAL);
            mMenuList.add(pasteFileMenuItem);
        }
        return mMenuList;
    }

    private void subMenuClick(int position) {
        boolean isRefreshed = false;
        MenuItem menuItemData = mMenuList.get(mSelectedMenuPosi);
        int lastSelectPosi = menuItemData.setChildSelected(position);
        if (mSelectedMenuPosi == 0) {
            int sortType = SharedPreferenceUtil.getSortType();
            if (position == 0) {
                if (sortType != FileComparator.SORT_TYPE_DATE_DOWN && sortType != FileComparator.SORT_TYPE_DATE_UP) {
                    sortType = FileComparator.SORT_TYPE_DATE_DOWN;
                }
                if (sortType == FileComparator.SORT_TYPE_DATE_DOWN) {
                    sortType = FileComparator.SORT_TYPE_DATE_UP;
                } else {
                    sortType = FileComparator.SORT_TYPE_DATE_DOWN;
                }
            } else if (position == 1) {
                if (sortType != FileComparator.SORT_TYPE_SIZE_DOWN && sortType != FileComparator.SORT_TYPE_SIZE_UP) {
                    sortType = FileComparator.SORT_TYPE_SIZE_DOWN;
                }
                if (sortType == FileComparator.SORT_TYPE_SIZE_DOWN) {
                    sortType = FileComparator.SORT_TYPE_SIZE_UP;

                } else {
                    sortType = FileComparator.SORT_TYPE_SIZE_DOWN;
                }
            } else if (position == 2) {
                if (sortType != FileComparator.SORT_TYPE_NAME_DOWN && sortType != FileComparator.SORT_TYPE_NAME_UP) {
                    sortType = FileComparator.SORT_TYPE_NAME_DOWN;
                }
                if (sortType == FileComparator.SORT_TYPE_NAME_DOWN) {
                    sortType = FileComparator.SORT_TYPE_NAME_UP;

                } else {
                    sortType = FileComparator.SORT_TYPE_NAME_DOWN;
                }
            }
            isRefreshed = FileUtil.sortList(mGridView.mListAdapter.getData(), sortType, false);
            if (isRefreshed) {
                mGridView.mListAdapter.notifyDataSetChanged();
            }
        } else if (mSelectedMenuPosi == 1) {
            if (position == 0) {
                setGridStyle(MediaOrientation.LIST);
                SharedPreferenceUtil.setGridStyle(0);
            } else if (position == 1) {
                setGridStyle(MediaOrientation.THUMBNAIL);
                SharedPreferenceUtil.setGridStyle(1);
            }
            mCurrGridStyle = position;
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
    protected void onDestroy() {
        if (mGridView != null && mGridView.fileServer != null) {
            mGridView.fileServer.release();
        }
        mConfirmDialog = null;
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public Bitmap getScreenShot() {
        getWindow().getDecorView().setDrawingCacheEnabled(false);
        getWindow().getDecorView().setDrawingCacheEnabled(true);
        return getWindow().getDecorView().getDrawingCache();
    }

    private void updateSDMounted(String usbPath) {

        if (!isExternal) { //不是外接设备就不用往下走了
            return;
        }
        final List<Media> mediaList = new ArrayList<>();
        List<String> currPathList = MediaUtils.getCurrPathList();
        if (mGridView.mMediaStack.isEmpty() && currPathList.size() == 1) {
            mGridView.show();
        } else {
            for (String path : currPathList) {
                File file = new File(path);
                Media fileInfo = FileUtil.getFileInfo(file, null, false);
                mediaList.add(fileInfo);
            }

            boolean isUpdate = true;    //
            if (!mGridView.mMediaStack.isEmpty())

            { //不在根目录下有可能会进行刷新(这是发生在移出外接存储时)

                if (mGridView.mMediaStack.size() > 1) { //取第二级目录中第一个地址,和还存在的外接设备地址进行比较
                    String uri = mGridView.mMediaStack.get(1).get(0).mUri;
                    for (int i = 0; i < mediaList.size(); i++) {
                        if (uri.contains(mediaList.get(i).mUri)) {
                            isUpdate = false;
                            break;
                        }
                    }
                }

            }

            if (isUpdate) {
                updateRootUI(mediaList);
            }

        }
    }

    /**
     * 更新外接设备列表
     */
    private void updateRootUI(List<Media> mediaList) {
        // 清除记录的上级目录
        mGridView.mMediaStack.clear();
        mGridView.mPosStack.clear();
        mGridView.mCurrMediaList = mediaList;
        mGridView.mListAdapter.bindData(mediaList);
        if (mediaList.size() < 1) {
            mRTCountView.setVisibility(View.GONE);
            mGridView.showNoDataPage();
        } else {
            mRTCountView.setVisibility(View.VISIBLE);
            mGridView.setTextRTview(mGridView.mSelectItemPosition + 1 + " / ", mediaList.size() + "");
        }
        if (null != mMenuDialog && mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }
    }

    /**
     * 删除弹框
     */
    private void deleteItem(final List<Media> datas) {
        if (mConfirmDialog == null) {
            mConfirmDialog = new ConfirmDialog(this);
        }
        mConfirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mGridView.setDefaultStyle();
            }
        });
        mConfirmDialog.setOnClickableListener(new ConfirmDialog.OnClickableListener() {
            @Override
            public void onConfirmClickable() {
                removeItem(datas);
            }

            @Override
            public void onCancelClickable() {
                return;
            }
        });
        mConfirmDialog.show();
    }

    /**
     * 删除实现
     *
     * @param datas
     */
    private void removeItem(List<Media> datas) {
        if (datas.size() > 0) { // 防止当前目录没有数据,进行删除操作发生异常
            Media media = datas.get(mDeleteItem);
            delete(datas, media);
        } else {
            ToastUtils.showMessageLong(GridViewActivity.this, R.string.null_data);
        }
    }

    /**
     * 删除文件
     */
    public void delete(final List<Media> datas, Media media) {
        Log.w("路径", media.isSharing ? media.sharePath : media.mUri);
        FileUtil.copyFileList(media);
        FileUtil.asnycExecute(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (final Media f : FileUtil.mCurFileNameList) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (FileUtil.deleteFile(f)) {
                                refreshAfterDel(datas);
                            } else {

                                //修复OS-3850偶现删除蓝牙接收的图片，删除失败
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!f.isSharing) {
                                            if (getFile(f.mUri)) {
                                                ToastUtils.showMessage(MyApplication.getContext(), "删除失败!", Toast.LENGTH_LONG);
                                            } else {
                                                refreshAfterDel(datas);
                                            }
                                        }
                                    }
                                }, 300);
                            }
                        }
                    });
                }
                FileUtil.clear();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //为了处理从不同的入口进入文件管理器,出现的类型错乱,如：从视频入口进入，按home键,再从图片进入,显示的还是视频类型
        /*if (!isStartAc && !(MyApplication.mHomeActivityList.size() > 0)) {
            finish();
        }*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbMounted(UsbMounted usbMounted) {
        if (usbMounted.mIsRemoved) {
            Log.i("Mount", "gridView unmounted...");
            //修复OS-4985进入文件管理，打开任意外接设备的文件夹，按菜单键选择删除，在删除提示框界面断开外接设备，删除提示框仍显示，选择确定后，界面弹出外接设备文件
            if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
                mConfirmDialog.dismiss();
            }
            updateSDMounted(usbMounted.mUsbPath);
        } else {
            Log.i("Mount", "gridView mounted...");
            if (mGridView.mMediaStack.isEmpty()) {
                // 有新设备插入
                updateSDMounted(usbMounted.mUsbPath);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResetType(YSourceType sourceType) {
        mTitleTV.setText(getResources().getString(sourceType.mTypeName));
        mGridView.resetSourceType(sourceType.mType);
        isExternal = true;
    }

    /**
     * 通过路径判断文件是否存在
     *
     * @param fileUri
     */
    private boolean getFile(final String fileUri) {
        if (!TextUtils.isEmpty(fileUri)) {
            File file = new File(fileUri);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除后刷新界面
     *
     * @param datas
     */
    private void refreshAfterDel(List<Media> datas) {
        int tempCount;
        datas.remove(mDeleteItem);
        if (datas.size() <= 0) {
            mGridView.setTextRTview("", "");
        } else {
            tempCount = 1;
            mGridView.setTextRTview(mGridView.mSelectItemPosition + tempCount + " / ", datas.size() + "");
        }
        mGridView.mListAdapter.bindData(datas);
        //刷新界面
        if (!(datas.size() > 0)) {
            mGridView.showNoDataPage();
            mRTCountView.setText("");
        }
        ToastUtils.showMessage(MyApplication.getContext(), "删除成功!", Toast.LENGTH_LONG);
    }

    /**
     * 刷新当前目录
     */
    private void refreshCurrList(String path) {
        FileUtil.getFileList(path, new FileUtil.OnFileListListener() {
            @Override
            public void findFileListFinish(final List<Media> list) {
                if (null != mGridView) {
                    boolean isSort = FileUtil.sortList(mGridView.mListAdapter.getData(), -1, true);//-1没有意义,起作用的是true
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGridView.mListAdapter.bindData(list);
                        }
                    });
                }
            }
        });
    }


}