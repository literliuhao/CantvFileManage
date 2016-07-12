package com.cantv.media.center.activity;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.cantv.media.center.ui.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.ui.MediaGridView;
import com.cantv.media.center.ui.MediaOrientation;
import com.cantv.media.center.ui.MenuDialog;
import com.cantv.media.center.ui.MenuDialog.MenuAdapter;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;

import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.ui.MediaGridView;
import com.cantv.media.center.utils.MediaUtils;

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
    private int mDeleteItem ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        mTitleTV = (TextView) findViewById(R.id.title_textview);
        mContentView = (RelativeLayout) findViewById(R.id.gridview_content);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String type = null;
        if (uri != null) {
            type = uri.getHost();
        } else {
            type = intent.getStringExtra("type");
        }
        if ("video".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_movie);
            mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(),
                    SourceType.MOIVE);
        } else if ("image".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_photo);
            mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(),
                    SourceType.PICTURE);
        } else if ("audio".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_music);
            mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(),
                    SourceType.MUSIC);
        } else if ("app".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_app);
            mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(),
                    SourceType.APP);
        } else if ("local".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_file);
            mGridView = new MediaGridView(this, MediaUtils.getUsbRootPath(),
                    SourceType.LOCAL);
        } else if ("device1".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_external);
            String filePath = getIntent().getStringExtra("filePath");
            mGridView = new MediaGridView(this, filePath, SourceType.DEVICE);
        } else if ("device2".equalsIgnoreCase(type)) {
            mTitleTV.setText(R.string.str_external);
            String filePath = getIntent().getStringExtra("filePath");
            mGridView = new MediaGridView(this, filePath, SourceType.DEVICE);
        }
        mGridView.show();
        mContentView.removeAllViews();
        switch (SharedPreferenceUtil.getGridStyle()) {
		case 0:
			setGridStyle(MediaOrientation.THUMBNAIL);
			break;
		case 1:
			setGridStyle(MediaOrientation.LIST);
			break;
		}
        mContentView.addView(mGridView);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (8 == keyCode || 166 == keyCode) {
            setGridStyle(MediaOrientation.THUMBNAIL);
            SharedPreferenceUtil.setGridStyle(0);
        } else if (9 == keyCode || 167 == keyCode) {
        	SharedPreferenceUtil.setGridStyle(1);
            setGridStyle(MediaOrientation.LIST);
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            showMenuDialog();
        }
        return super.onKeyDown(keyCode, event);
    }
    public void setGridStyle(MediaOrientation mStyle) {
        switch (mStyle) {
        case LIST:
            mGridView.setVerticalSpacing((int) getResources().getDimension(
                    R.dimen.px31));
            mGridView.setPadding(0, 0, 0, 0);
            mGridView.setStyle(MediaOrientation.LIST);
            mGridView.setNumColumns(1);
            break;
        case THUMBNAIL:
            mGridView.setVerticalSpacing((int) getResources().getDimension(
                    R.dimen.px0));
            mGridView.setPadding(0, 0, 0, 22);
            mGridView.setStyle(MediaOrientation.THUMBNAIL);
            mGridView.setNumColumns(5);
            break;
        }
    }
    @Override
    public void onBackPressed() {
        // MediaGridView childGridView = (MediaGridView)
        // mContentView.getFocusedChild();
        MediaGridView childGridView = (MediaGridView) mContentView
                .getChildAt(0);
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
            mMenuDialog.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onSubMenuItemClick(LinearLayout parent, View view,
                        int position) {
                    subMenuClick(position);
                }
                @Override
                public boolean onMenuItemClick(LinearLayout parent, View view,
                        int position) {
                    if(position != 2){
                        if(mSelectedMenuPosi == position){
                            return false;
                        }
                    }
                    mMenuList.get(mSelectedMenuPosi).setSelected(false);
                    mSelectedMenuPosi = position;
                    MenuItem menuItem = mMenuList.get(position);
                    menuItem.setSelected(true);
                    mMenuDialog.getMenuAdapter().notifySubMenuDataSetChanged();
                    if(position == 2){
                        mDeleteItem = mGridView.mSelectItemPosition ; 
                        List<Media> datas = mGridView.mListAdapter.getData();
                        Media media = datas.get(mDeleteItem);
                        boolean deleteSuccessed = FileUtil.delete(media);
                        if (deleteSuccessed) {
                            datas.remove(mDeleteItem);
                            mGridView.mListAdapter.bindData(datas);
                            Log.i("shen", "mDeleteItem:" + mDeleteItem);
                        } else {
                            Toast.makeText(GridViewActivity.this, R.string.deleteFailed, Toast.LENGTH_SHORT).show();
                        }
                        
                        return true ;
                    }else{
                    	return false ;
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
        MenuItem sortMenu = sortListSubMenuItems.get(2);
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
        MenuItem viewMenu = viewModeSubMenuItems.get(1);
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
                isRefreshed = FileUtil.sortList(
                        mGridView.mListAdapter.getData(),
                        FileComparator.SORT_TYPE_DATE_DOWN, false);
            } else if (position == 1) {
                isRefreshed = FileUtil.sortList(
                        mGridView.mListAdapter.getData(),
                        FileComparator.SORT_TYPE_SIZE_DOWN, false);
            } else if (position == 2) {
                isRefreshed = FileUtil.sortList(
                        mGridView.mListAdapter.getData(),
                        FileComparator.SORT_TYPE_NAME_UP, false);
            }
            if (isRefreshed) {
                mGridView.mListAdapter.notifyDataSetChanged();
            }
        } else if (mSelectedMenuPosi == 1) {
            if (position == 0) {
                setGridStyle(MediaOrientation.LIST);
            } else if (position == 1) {
                setGridStyle(MediaOrientation.THUMBNAIL);
            }
        } 
        
        View oldSubMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + lastSelectPosi);
        if(oldSubMenuItemView != null){
            mMenuDialog.getMenuAdapter().updateSubMenuItem(oldSubMenuItemView, menuItemData.getChildAt(lastSelectPosi));
        }
        View subMenuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_SUB_MENU_VIEW + position);
        if(subMenuItemView != null){
            mMenuDialog.getMenuAdapter().updateSubMenuItem(subMenuItemView, menuItemData.getSelectedChild());
        }
        View menuItemView = mMenuDialog.getMenu().findViewWithTag(MenuAdapter.TAG_MENU_VIEW + mSelectedMenuPosi);
        if(menuItemView != null){
            mMenuDialog.getMenuAdapter().updateMenuItem(menuItemView, menuItemData);
        }
    }
}