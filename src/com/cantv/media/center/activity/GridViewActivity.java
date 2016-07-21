package com.cantv.media.center.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.MediaGridView;
import com.cantv.media.center.ui.MediaOrientation;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private MenuItem sortMenuItem;
    private MenuItem viewItem;
    private List<MenuItem> sortListSubMenuItems;
    private List<MenuItem> viewModeSubMenuItems;
    private List<MenuItem> mMenuList;
    private int mSelectedMenuPosi;
    private int mDeleteItem;
    public boolean isExternal; // 记录当前是否处于外接设备,true:处于外接设备(通过USB接口接入)
    public TextView mFocusName; //选中显示的名称
    public TextView mRTCountView; //显示数量和当前选中position
    public View mBg_view; //上部阴影
    public int mCurrGridStyle; //记录当前是什么排列方式

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        mTitleTV = (TextView) findViewById(R.id.title_textview);
        mContentView = (RelativeLayout) findViewById(R.id.gridview_content);
//        mContentView.mar(0,0,0,200);
        mBg_view = findViewById(R.id.bg_view);
        mCurrGridStyle = SharedPreferenceUtil.getGridStyle();
        mFocusName = (TextView) findViewById(R.id.focusview_name);
        mRTCountView = (TextView) findViewById(R.id.file_count);
        Intent intent = getIntent();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);
        String type = intent.getStringExtra("type");
        if ("video".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_movie);
            mGridView = new MediaGridView(this, SourceType.MOIVE);
            isExternal = true;
        } else if ("image".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_photo);
            mGridView = new MediaGridView(this, SourceType.PICTURE);
            isExternal = true;
        } else if ("audio".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_music);
            mGridView = new MediaGridView(this, SourceType.MUSIC);
            isExternal = true;
        } else if ("app".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_app);
            mGridView = new MediaGridView(this, SourceType.APP);
            isExternal = true;
        } else if ("local".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_file);
            mGridView = new MediaGridView(this, SourceType.LOCAL);
            isExternal = false;
        } else if ("device1".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_external);
            mGridView = new MediaGridView(this, SourceType.DEVICE);
            if (MediaUtils.getUSBNum() > 0) {
                mGridView.setDevicePath(MediaUtils.getUsbRootPaths().get(0));
            }
            isExternal = true;
        } else if ("device2".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_external);
            mGridView = new MediaGridView(this, SourceType.DEVICE);
            if (MediaUtils.getUSBNum() > 1) {
                mGridView.setDevicePath(MediaUtils.getUsbRootPaths().get(1));
            }
            isExternal = true;
        }
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
        mGridView.setOnFocusChangedListener(new MediaGridView.OnFocusChangedListener() {
            @Override
            public void focusPosition(Media media, int position) {
                mFocusName.setText(media.mName);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (8 == keyCode || 166 == keyCode) {
//            setGridStyle(MediaOrientation.THUMBNAIL);
//            SharedPreferenceUtil.setGridStyle(0);
//        } else if (9 == keyCode || 167 == keyCode) {
//            SharedPreferenceUtil.setGridStyle(1);
//            setGridStyle(MediaOrientation.LIST);
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
                mGridView.setVerticalSpacing((int) getResources().getDimension(R.dimen.px16));
                mGridView.setPadding(0, 0, 0, 2);
                mGridView.setStyle(MediaOrientation.LIST);
                mGridView.setNumColumns(1);
                break;
            case THUMBNAIL:
                mGridView.setVerticalSpacing((int) getResources().getDimension(R.dimen.px0));
                mGridView.setPadding(0, 0, 0, 60);
                mGridView.setStyle(MediaOrientation.THUMBNAIL);
                mGridView.setNumColumns(5);
//                mGridView.setOutlineProvider();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // MediaGridView childGridView = (MediaGridView)
        // mContentView.getFocusedChild();
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
                    mGridView.setDefaultStyle();
                }
            });
            mMenuDialog.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onSubMenuItemClick(LinearLayout parent, View view, int position) {
                    subMenuClick(position);
                }

                @Override
                public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
                    if (position != 2) {
                        if (mSelectedMenuPosi == position) {
                            return false;
                        }
                    }
                    mMenuList.get(mSelectedMenuPosi).setSelected(false);
                    mSelectedMenuPosi = position;
                    MenuItem menuItem = mMenuList.get(position);
                    menuItem.setSelected(true);
                    mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
                    if (position == 2) {
                        mDeleteItem = mGridView.mSelectItemPosition;
                        List<Media> datas = mGridView.mListAdapter.getData();
                        if (datas.size() > 0) { // 防止当前目录没有数据,进行删除操作发生异常
                            Media media = datas.get(mDeleteItem);
                            boolean deleteSuccessed = FileUtil.delete(media);
                            if (deleteSuccessed) {
                                datas.remove(mDeleteItem);
                                mGridView.mListAdapter.bindData(datas);
                                Log.i("shen", "mDeleteItem:" + mDeleteItem);
                            } else {
                                Toast.makeText(GridViewActivity.this, R.string.deleteFailed, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(GridViewActivity.this, "没有数据!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        mMenuDialog.show();
    }

    private List<MenuItem> createMenuData() {
        mMenuList = new ArrayList<MenuItem>();
        sortListMenuItem = new MenuItem(getString(R.string.sort));
        sortListMenuItem.setType(MenuItem.TYPE_SELECTOR);
        sortListMenuItem.setSelected(true);
        sortListSubMenuItems = new ArrayList<MenuItem>();
        sortMenuItem = new MenuItem(getString(R.string.sort_date), MenuItem.TYPE_SELECTOR);
        sortListSubMenuItems.add(sortMenuItem);
        sortListSubMenuItems.add(new MenuItem(getString(R.string.sort_filesize), MenuItem.TYPE_SELECTOR));
        sortListSubMenuItems.add(new MenuItem(getString(R.string.sort_name), MenuItem.TYPE_SELECTOR));
        int sortType = SharedPreferenceUtil.getSortType();
        MenuItem sortMenu = sortListSubMenuItems.get(sortType);
        sortMenu.setParent(sortListMenuItem);
        sortMenu.setSelected(true);
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
        deleteMenuItem = new MenuItem(getString(R.string.delete));
        deleteMenuItem.setType(MenuItem.TYPE_NORMAL);
        mMenuList.add(deleteMenuItem);
        return mMenuList;
    }

    private void subMenuClick(int position) {
        boolean isRefreshed = false;
        MenuItem menuItemData = mMenuList.get(mSelectedMenuPosi);
        int lastSelectPosi = menuItemData.setChildSelected(position);
        if (mSelectedMenuPosi == 0) {
            if (position == 0) {
                isRefreshed = FileUtil.sortList(mGridView.mListAdapter.getData(), FileComparator.SORT_TYPE_DATE_DOWN, false);
            } else if (position == 1) {
                isRefreshed = FileUtil.sortList(mGridView.mListAdapter.getData(), FileComparator.SORT_TYPE_SIZE_DOWN, false);
            } else if (position == 2) {
                isRefreshed = FileUtil.sortList(mGridView.mListAdapter.getData(), FileComparator.SORT_TYPE_NAME_UP, false);
            }
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

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                // 有新设备插入
                openRootDir();
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                // 移除设备
                openRootDir();
            }
        }
    };

    /**
     * 到根目录: 适用在当前处于外接设备目录
     */
    private void openRootDir() {
        if (!isExternal) {
            return;
        }
        List<String> usbRootPaths = MediaUtils.getUsbRootPaths();
        List<Media> mediaes = new ArrayList<>();
        for (int i = 0; i < usbRootPaths.size(); i++) {
            File file = new File(usbRootPaths.get(i));
            Media fileInfo = FileUtil.getFileInfo(file, null, false);
            mediaes.add(fileInfo);
        }
        // 清除记录的上级目录
        mGridView.mMediaStack.clear();
        mGridView.mPosStack.clear();
        mGridView.mListAdapter.bindData(mediaes);
        if (mediaes.size() < 1) {
            mGridView.showNoDataPage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}