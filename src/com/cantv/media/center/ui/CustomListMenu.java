package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cantv.media.R;

@SuppressLint("NewApi")
public class CustomListMenu extends Dialog {
    private RelativeLayout mRoot;
    private ListView mListView;
    private TextView mTextView;
    private ImageView mImageView;
    private ListMenuAdapter mAdapter;
    private ListMenuListener mListener;
    private MediaMenuListRadiaoItem mLastItemView;
    private int mLastX;
    private int mLastY;
    private int mCurX;
    private int mCurY;
    private FocusFrame mFocusFrame;
    private Context mContext;

    public CustomListMenu(Context context) {
        super(context, R.style.general__shared__full_screen_dialog);
        mContext = context;
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
        setContentView(R.layout.media_dialog_item);
        mRoot = (RelativeLayout) findViewById(R.id.root);
        mListView = (ListView) findViewById(R.id.lst_dialog);
        mTextView = (TextView) findViewById(R.id.txt_title);
        mImageView = (ImageView) findViewById(R.id.iv_titleimage);
        mFocusFrame = new FocusFrame(context);
        mFocusFrame.setFocusFrameImageResourceAndMeasure(R.drawable.framfocus);
        mFocusFrame.setVisibility(View.VISIBLE);
        mFocusFrame.setFocusable(false);
        ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mFocusFrame.setLayoutParams(lp2);
        mRoot.addView(mFocusFrame);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onMenuItemClicked(position);
                }
                dismiss();
            }
        });
        mListView.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                move(view, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // mLastItemView.getImageView().setBackground(mContext.getResources().getDrawable(
                // R.drawable.radiaonomal));
            }
        });
        mAdapter = new ListMenuAdapter();
        mListView.setAdapter(mAdapter);
    }

    public void setMenuTitle(String title) {
        mTextView.setText(title);
    }

    public void setMenuTitleIcon(Drawable icon) {
        mImageView.setBackground(icon);
    }

    public void setMenuItems(String[] items) {
        mAdapter.setData(items);
    }

    public void setListMenuListener(ListMenuListener l) {
        mListener = l;
    }

    protected View getMenuItemView(String itemText, View convertView, ViewGroup parent) {
        MenuItemView mediaItemView;
        if (convertView == null) {
            mediaItemView = new MenuItemView(parent.getContext());
        } else {
            mediaItemView = (MenuItemView) convertView;
        }
        mediaItemView.setText(itemText);
        return mediaItemView;
    }

    public interface ListMenuListener {
        void onMenuItemClicked(int position);
    }

    private class ListMenuAdapter extends BaseAdapter {
        private String[] mdataArray;

        public void setData(String[] data) {
            mdataArray = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mdataArray == null ? 0 : mdataArray.length;
        }

        @Override
        public Object getItem(int position) {
            return mdataArray == null ? null : mdataArray[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getMenuItemView(mdataArray[position], convertView, parent);
        }
    }

    public void move(View v, boolean hasFocus) {
        CancelSelected();
        if (!(v instanceof MediaMenuListRadiaoItem)) {
            return;
        }
        if (null == mLastItemView) {
            mLastItemView = (MediaMenuListRadiaoItem) v;
        }

        int[] location = new int[2];
        mLastItemView.getLocationInWindow(location);
        mLastX = location[0];
        mLastY = location[1];
        v.getLocationInWindow(location);
        mCurX = location[0];
        mCurY = location[1];
        if (v instanceof MediaMenuListRadiaoItem) {
            mFocusFrame.move(mLastX + v.getWidth() / 2, mLastY + v.getHeight() / 2, mCurX + v.getWidth() / 2, mCurY + v.getHeight() / 2);
        }
        mLastItemView = (MediaMenuListRadiaoItem) v;
        mLastItemView.postDelayed(new Runnable() {

            @Override
            public void run() {
                mLastItemView.getImageView().setBackground(mContext.getResources().getDrawable(R.drawable.radiaofocus));
            }
        }, 200);

    }

    private void CancelSelected() {
        int count = mListView.getCount();
        for (int n = 0; n < count; n++) {
            MediaMenuListRadiaoItem item = (MediaMenuListRadiaoItem) mListView.getChildAt(n);
            if (item != null) {
                item.getImageView().setBackground(mContext.getResources().getDrawable(R.drawable.radiaonomal));
            }
        }
    }
}
