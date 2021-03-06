package com.cantv.media.center.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cantv.media.R;
import com.cantv.media.center.data.MenuItem;
import com.cantv.media.center.dialog.DoubleColumnMenu.OnItemClickListener;
import com.cantv.media.center.dialog.DoubleColumnMenu.OnItemFocusChangeListener;
import com.cantv.media.center.dialog.DoubleColumnMenu.OnKeyEventListener;

import java.util.List;

public class MenuDialog extends Dialog {

    private DoubleColumnMenu mMenuView;
    private MenuAdapter mAdpter;

    private DoubleColumnMenu.OnItemClickListener mItemClickListener;
    private DoubleColumnMenu.OnItemFocusChangeListener mItemFocusListener;
    private DoubleColumnMenu.OnKeyEventListener mOnKeyListener;
    private long moveTime;

    public MenuDialog(Context context) {
        super(context, R.style.dialog_menu);
        setupLayout();
    }

    public DoubleColumnMenu getMenu() {
        return mMenuView;
    }

    public MenuAdapter getMenuAdapter() {
        return mAdpter;
    }

    private void setupLayout() {
        View view = View.inflate(getContext(), R.layout.dialog_menu, null);
        mMenuView = (DoubleColumnMenu) view.findViewById(R.id.dcm);

        View headerView = View.inflate(getContext(), R.layout.layout_menu_header, null);
        LinearLayout.LayoutParams headerViewLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getContext().getResources().getDimensionPixelSize(R.dimen.px160));
        headerView.setLayoutParams(headerViewLp);
        mMenuView.setMenuHeader(headerView);
        setContentView(view);

        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        this.getWindow().setGravity(Gravity.TOP | Gravity.END);
        this.getWindow().setAttributes(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && isShowing()) {
            dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 限制按键移动速度
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getAction()) {
            //控制按键响应的速度
            case KeyEvent.ACTION_DOWN:
                if (System.currentTimeMillis() - moveTime > 200) {
                    moveTime = System.currentTimeMillis();
                } else {
                    return true;
                }
        }
        return super.dispatchKeyEvent(event);

    }

    public void setMenuList(List<MenuItem> list) {
        if (mAdpter == null) {
            mAdpter = new MenuAdapter(getContext(), list);
            mMenuView.setAdapter(mAdpter);
            mMenuView.setOnItemsClickListener(new OnItemClickListener() {

                @Override
                public void onSubMenuItemClick(LinearLayout parent, View view, int position) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onSubMenuItemClick(parent, view, position);
                    }
                }

                @Override
                public boolean onMenuItemClick(LinearLayout parent, View view, int position) {
                    if (mItemClickListener != null) {
                        return mItemClickListener.onMenuItemClick(parent, view, position);
                    }
                    return false;
                }
            });
            mMenuView.setOnItemsFocusChangeListener(new OnItemFocusChangeListener() {

                @Override
                public void onSubMenuItemFocusChanged(LinearLayout parent, View view, int position, boolean hasFocus) {
                    if (mItemFocusListener != null) {
                        mItemFocusListener.onSubMenuItemFocusChanged(parent, view, position, hasFocus);
                    }
                }

                @Override
                public void onMenuItemFocusChanged(LinearLayout parent, View view, int position, boolean hasFocus) {
                    if (mItemFocusListener != null) {
                        mItemFocusListener.onMenuItemFocusChanged(parent, view, position, hasFocus);
                    }
                }
            });
            mMenuView.setOnItemKeyEventListener(new OnKeyEventListener() {

                @Override
                public boolean onSubMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    if (mOnKeyListener != null) {
                        return mOnKeyListener.onSubMenuItemKeyEvent(position, v, keyCode, event);
                    }
                    return false;
                }

                @Override
                public boolean onMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event) {
                    if (mOnKeyListener != null) {
                        return mOnKeyListener.onMenuItemKeyEvent(position, v, keyCode, event);
                    }
                    return false;
                }
            });
        } else {
            mAdpter.setData(list);
            mAdpter.notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(DoubleColumnMenu.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnItemFocusChangeListener(DoubleColumnMenu.OnItemFocusChangeListener listener) {
        mItemFocusListener = listener;
    }

    public void setOnItemKeyEventListener(DoubleColumnMenu.OnKeyEventListener listener) {
        mOnKeyListener = listener;
    }

    public static class MenuAdapter extends DoubleColumnMenu.BaseAdapter {

        public static final String TAG_MENU_VIEW = "menu";
        public static final String TAG_SUB_MENU_VIEW = "subMenu";

        private List<MenuItem> list;
        private String mStrTemplate;

        public MenuAdapter(Context context, List<MenuItem> list) {
            this.list = list;
            mStrTemplate = context.getResources().getString(R.string.total_num);
        }

        public List<MenuItem> getData() {
            return list;
        }

        public void setData(List<MenuItem> list) {
            if (this.list == null) {
                this.list = list;
            } else {
                this.list.clear();
                this.list.addAll(list);
            }
        }

        @Override
        public int getMenuCount() {
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        @Override
        public Object getMenuItem(int position) {
            if (list == null) {
                return null;
            }
            return list.get(position);
        }

        @Override
        public View getMenuItemView(LinearLayout parent, View convertView, int position) {
            View view = null;
            if (convertView == null) {
                MenuViewHolder holder = new MenuViewHolder();
                view = View.inflate(parent.getContext(), R.layout.layout_menu_item, null);
                holder.arrowIv = (ImageView) view.findViewById(R.id.iv_arrow);
                holder.titleTv = (TextView) view.findViewById(R.id.tv_title);
                holder.subTitleTv = (TextView) view.findViewById(R.id.tv_subTitle);
                view.setTag(R.id.tag_id_holder_key, holder);
            } else {
                view = convertView;
            }
            view.setTag(TAG_MENU_VIEW + position);
            updateMenuItem(view, (MenuItem) getMenuItem(position));

            ViewGroup.LayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.getResources().getDimensionPixelOffset(R.dimen.px114));
            view.setLayoutParams(lp);
            return view;
        }

        @Override
        public int getSubMenuCount() {
            List<MenuItem> selctedSubMenuItems = getSelectedSubMenuItems();
            return selctedSubMenuItems == null ? 0 : selctedSubMenuItems.size();
        }

        @Override
        public Object getSubMenuItem(int position) {
            List<MenuItem> selctedSubMenuItems = getSelectedSubMenuItems();
            return selctedSubMenuItems == null ? null : selctedSubMenuItems.get(position);
        }

        @Override
        public int getSubItemViewType(int position) {
            return ((MenuItem) getSubMenuItem(position)).getType();
        }

        @Override
        public View getSubMenuItemView(LinearLayout parent, View convertView, int position) {
            MenuItem data = (MenuItem) getSubMenuItem(position);
            int dataType = data.getType();
            View view = null;
            if (dataType == MenuItem.TYPE_LIST || dataType == MenuItem.TYPE_NORMAL) {
                if (convertView == null) {
                    ListSubMenuViewHolder holder = new ListSubMenuViewHolder();
                    view = View.inflate(parent.getContext(), R.layout.layout_submenu_list_item, null);
                    holder.titleTv = (TextView) view;
                    view.setTag(R.id.tag_id_holder_key, holder);
                } else {
                    view = convertView;
                }
            } else if (dataType == MenuItem.TYPE_SELECTOR) {
                if (convertView == null) {
                    SubMenuViewHolder holder = new SubMenuViewHolder();
                    view = View.inflate(parent.getContext(), R.layout.layout_submenu_selector_item, null);
                    holder.titleTv = (TextView) view.findViewById(R.id.tv_title);
                    holder.selector = (ImageView) view.findViewById(R.id.iv_selector);
                    view.setTag(R.id.tag_id_holder_key, holder);
                } else {
                    view = convertView;
                }
            } else if (dataType == MenuItem.TYPE_SELECTOR_MARQUEE) {
                if (convertView == null) {
                    SubMenuViewHolder holder = new SubMenuViewHolder();
                    view = View.inflate(parent.getContext(), R.layout.layout_marquee_selector_item, null);
                    holder.titleTv = (TextView) view.findViewById(R.id.tv_title);
                    holder.selector = (ImageView) view.findViewById(R.id.iv_selector);
                    view.setTag(R.id.tag_id_holder_key, holder);
                } else {
                    view = convertView;
                }
            }
            updateSubMenuItem(view, data);
            view.setTag(TAG_SUB_MENU_VIEW + position);
            ViewGroup.LayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.getResources().getDimensionPixelOffset(R.dimen.px90));
            view.setLayoutParams(lp);
            return view;
        }

        public void updateMenuItem(View view, MenuItem data) {
            MenuViewHolder holder = (MenuViewHolder) view.getTag(R.id.tag_id_holder_key);
            view.setEnabled(data.isEnabled());
            view.setFocusable(data.isEnabled());
            view.setSelected(data.isSelected());
            holder.arrowIv.setVisibility(data.getChildrenCount() > 0 ? (data.isSelected() ? View.VISIBLE : View.INVISIBLE) : View.INVISIBLE);
            holder.titleTv.setText(data.getTitle());
            int type = data.getType();
            if (type == MenuItem.TYPE_LIST) {
                holder.subTitleTv.setVisibility(View.VISIBLE);
                holder.subTitleTv.setText(String.format(mStrTemplate, data.getChildrenCount()));
            } else if (type == MenuItem.TYPE_SELECTOR || type == MenuItem.TYPE_SELECTOR_MARQUEE) {
                holder.subTitleTv.setVisibility(View.VISIBLE);
                MenuItem selectedChild = data.getSelectedChild();
                holder.subTitleTv.setText(selectedChild != null ? selectedChild.getTitle() : "");
            } else {
                holder.subTitleTv.setVisibility(View.GONE);
                holder.subTitleTv.setText("");
            }
        }

        public void updateSubTitle(View view, MenuItem data, boolean b) {
            MenuViewHolder holder = (MenuViewHolder) view.getTag(R.id.tag_id_holder_key);
            view.setEnabled(data.isEnabled());
            view.setFocusable(data.isEnabled());
            view.setSelected(data.isSelected());
            holder.arrowIv.setVisibility(View.INVISIBLE);
            holder.titleTv.setText(b ? "内嵌字幕" : "外挂字幕");
            int type = data.getType();
            if (type == MenuItem.TYPE_LIST) {
                holder.subTitleTv.setVisibility(View.VISIBLE);
                holder.subTitleTv.setText(String.format(mStrTemplate, data.getChildrenCount()));
            } else if (type == MenuItem.TYPE_SELECTOR || type == MenuItem.TYPE_SELECTOR_MARQUEE) {
                holder.subTitleTv.setVisibility(View.VISIBLE);
                holder.subTitleTv.setText("无");
            } else {
                holder.subTitleTv.setVisibility(View.GONE);
                holder.subTitleTv.setText("");
            }
        }

        public void updateVideoMenuItem(View view, MenuItem data, boolean change) {
            MenuViewHolder holder = (MenuViewHolder) view.getTag(R.id.tag_id_holder_key);
            view.setEnabled(data.isEnabled());
            view.setFocusable(data.isEnabled());
            view.setSelected(data.isSelected());
            holder.arrowIv.setVisibility(data.getChildrenCount() > 0 ? (data.isSelected() ? View.VISIBLE : View.INVISIBLE) : View.INVISIBLE);
            holder.titleTv.setText(data.getTitle());
            holder.titleTv.setTextColor(change ? Color.GRAY : 0xCCFFFFFF);
            int type = data.getType();
            if (type == MenuItem.TYPE_LIST) {
                holder.subTitleTv.setVisibility(View.VISIBLE);
                holder.subTitleTv.setText(String.format(mStrTemplate, data.getChildrenCount()));
            } else if (type == MenuItem.TYPE_SELECTOR || type == MenuItem.TYPE_SELECTOR_MARQUEE) {
                holder.subTitleTv.setVisibility(View.VISIBLE);
                MenuItem selectedChild = data.getSelectedChild();
                holder.subTitleTv.setText(selectedChild != null ? selectedChild.getTitle() : "");
            } else {
                holder.subTitleTv.setVisibility(View.GONE);
                holder.subTitleTv.setText("");
            }
        }

        public void updateSubMenuItem(View view, MenuItem data) {
            int dataType = data.getType();
            view.setEnabled(data.isEnabled());
            view.setFocusable(data.isEnabled());
            view.setSelected(data.isSelected());
            if (dataType == MenuItem.TYPE_LIST || dataType == MenuItem.TYPE_NORMAL) {
                ListSubMenuViewHolder holder = (ListSubMenuViewHolder) view.getTag(R.id.tag_id_holder_key);
                holder.titleTv.setText(data.getTitle());

            } else if (dataType == MenuItem.TYPE_SELECTOR || dataType == MenuItem.TYPE_SELECTOR_MARQUEE) {
                SubMenuViewHolder holder = (SubMenuViewHolder) view.getTag(R.id.tag_id_holder_key);
                holder.titleTv.setText(data.getTitle());
                holder.selector.setVisibility(data.isSelected() ? View.VISIBLE : View.INVISIBLE);
            }
        }

        private List<MenuItem> getSelectedSubMenuItems() {
            if (list == null || list.size() == 0) {
                return null;
            }
            List<MenuItem> menuItems = null;
            for (int i = 0, itemCount = list.size(); i < itemCount; i++) {
                MenuItem item = list.get(i);
                if (item.isSelected()) {
                    menuItems = item.getChildren();
                    break;
                }
            }
            return menuItems;
        }

        static class MenuViewHolder {
            ImageView arrowIv;
            TextView titleTv;
            TextView subTitleTv;
        }

        static class SubMenuViewHolder {
            TextView titleTv;
            ImageView selector;
        }

        class ListSubMenuViewHolder {
            TextView titleTv;
        }
    }

    public void openSubMenuItem() {
        mMenuView.openSubMenu(false);
    }

    public void closeSubMenuItem() {
        mMenuView.closeSubMenu();
    }

    public void showSubMenuFocus(boolean isshow) {
        mMenuView.showSubMenuFocus(isshow);
    }
}
