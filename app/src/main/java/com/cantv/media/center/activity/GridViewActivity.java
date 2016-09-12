package com.cantv.media.center.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.ui.DoubleColumnMenu;
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.MediaGridView;
import com.cantv.media.center.ui.MediaOrientation;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private MenuItem mSortMenu;
    private List<MenuItem> sortListSubMenuItems;
    private List<MenuItem> viewModeSubMenuItems;
    private List<MenuItem> mMenuList;
    private int mSelectedMenuPosi;
    private int mDeleteItem;
    public boolean isExternal; // 记录当前是否处于外接设备,true:处于外接设备(通过USB接口接入)
    public TextView mRTCountView; // 显示数量和当前选中position
    public int mCurrGridStyle; // 记录当前是什么排列方式

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        mTitleTV = (TextView) findViewById(R.id.title_textview);
        mContentView = (RelativeLayout) findViewById(R.id.gridview_content);
        mCurrGridStyle = SharedPreferenceUtil.getGridStyle();
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
            if (MediaUtils.getUSBNum() > 0 && MediaUtils.getUSBNum() < 3) {
                if (null != getIntent().getStringExtra("toListFlag")) {

                } else {
                    mGridView.setDevicePath(MediaUtils.getCurrPathList().get(0));
                }
            }
            isExternal = true;
        } else if ("device2".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_external);
            mGridView = new MediaGridView(this, SourceType.DEVICE);
            if (MediaUtils.getUSBNum() > 1) {
                mGridView.setDevicePath(MediaUtils.getCurrPathList().get(1));
            }
            isExternal = true;
        } else if ("share".equalsIgnoreCase(type)) {
            mTitleTV.setText(intent.getStringExtra("title"));
            mGridView = new MediaGridView(this, SourceType.SHARE);
            mGridView.setDevicePath(intent.getStringExtra("path"));
            isExternal = false;
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
                    mGridView.setDefaultStyle();
                }
            });
            mMenuDialog.setOnItemFocusChangeListener(new DoubleColumnMenu.OnItemFocusChangeListener() {
                @Override
                public void onMenuItemFocusChanged(LinearLayout leftViewGroup, View view, int position, boolean hasFocus) {
                    if (position != 2) {
                        if (mSelectedMenuPosi == position) {
                            return ;
                        }
                    }
                    if(position==2){
                        mMenuDialog.closeSubMenuItem();
                    }
                    if(position!=2){
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
                    if (position != 2) {
                        if (mSelectedMenuPosi == position) {
                            return false;
                        }
                    }

                    if (position == 2) {
                        String[] pathList = SharedPreferenceUtil.getDevicesPath().split("abc");
                        if (FileUtil.isListConOtListValue(FileUtil.getListFromList(mGridView.mListAdapter.getData()), FileUtil.arrayToList(pathList))) {
                            Toast.makeText(getApplicationContext(), "外接设备不能删除", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        mDeleteItem = mGridView.mSelectItemPosition;
                        List<Media> datas = mGridView.mListAdapter.getData();
                        if (datas.size() > 0) { // 防止当前目录没有数据,进行删除操作发生异常
                            Media media = datas.get(mDeleteItem);
                            boolean deleteSuccessed = FileUtil.delete(media);
                            if (deleteSuccessed) {
                                datas.remove(mDeleteItem);
                                mGridView.mListAdapter.bindData(datas);
                            } else {
                                Toast.makeText(GridViewActivity.this, R.string.deleteFailed, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(GridViewActivity.this, R.string.null_data, Toast.LENGTH_SHORT).show();
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
        String[] pathList = SharedPreferenceUtil.getDevicesPath().split("abc");
        if (!"share".equalsIgnoreCase(type)) {
            deleteMenuItem = new MenuItem(getString(R.string.delete));
            deleteMenuItem.setType(MenuItem.TYPE_NORMAL);
            mMenuList.add(deleteMenuItem);
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
        String[] pathList = SharedPreferenceUtil.getDevicesPath().split("abc");
        List<Media> mediaes = new ArrayList<>();

        for (String s : pathList) {
            if (null == s || s.trim().equals("")) {
                continue;
            }

            // 获取路径对应设备的总容量
            if (null != MediaUtils.getTotal(s)) {
                File file = new File(s);
                Media fileInfo = FileUtil.getFileInfo(file, null, false);
                mediaes.add(fileInfo);
            }
        }

        // 清除记录的上级目录
        mGridView.mMediaStack.clear();
        mGridView.mPosStack.clear();
        mGridView.mCurrMediaList = mediaes;
        mGridView.mListAdapter.bindData(mediaes);
        if (mediaes.size() < 1) {
            mRTCountView.setVisibility(View.GONE);
            mGridView.showNoDataPage();
        } else {
            mRTCountView.setVisibility(View.VISIBLE);
            mGridView.setTextRTview(mGridView.mSelectItemPosition + 1 + " / ", mediaes.size() + "");
        }
        if (null != mMenuDialog && mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (mGridView != null && mGridView.fileServer != null) {
            mGridView.fileServer.release();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public Bitmap getScreenShot() {
        getWindow().getDecorView().setDrawingCacheEnabled(false);
        getWindow().getDecorView().setDrawingCacheEnabled(true);
        return getWindow().getDecorView().getDrawingCache();
    }

}