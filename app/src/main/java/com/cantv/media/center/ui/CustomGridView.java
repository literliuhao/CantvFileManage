package com.cantv.media.center.ui;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.app.core.sys.MainThread;
import com.cantv.media.R;
@SuppressLint("ResourceAsColor")
public class CustomGridView extends FrameLayout implements OnItemSelectedListener {
    public GridView mGridView;
    private GridFocusView mFocusView;
    private Point mLastFocusPoint = new Point(-1, -1);
    private OnItemSelectedListener mSelectedListener = null;
    private Runnable mRefreshSelectedViewRunnable;
    private View memptyView;
    private TextView mdata_tip;
    private TextView textType;
    public CustomGridView(Context context) {
        this(context, null);
    }
    public CustomGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFocusView = new GridFocusView(context);
        mFocusView.setFocusable(false);
        mFocusView.setFocusFrameImageResourceAndMeasure(R.drawable.focus);
        mGridView = new GridView(context) {
            @Override
            public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
                if (previouslyFocusedRect != null) {
                    previouslyFocusedRect.right = previouslyFocusedRect.left = mLastFocusPoint.x;
//                    return super.requestFocus(direction, previouslyFocusedRect);
                }
                return super.requestFocus(direction, previouslyFocusedRect);
            }
        };
        // mGridView.setClipChildren(false);
        // mGridView.setClipToPadding(false);
        mGridView.setFocusable(true);
        mGridView.setFocusableInTouchMode(true);
        mGridView.setVerticalScrollBarEnabled(false);
        mGridView.setNumColumns(GridView.AUTO_FIT);
//		mGridView.setColumnWidth(280);
        mGridView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                MainThread.cancel(mRefreshSelectedViewRunnable);
                mFocusView.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
                if (hasFocus) {
                    MainThread.runLater(new Runnable() {
                        public void run() {
                            animateFoucs(mGridView.getSelectedView());
                        }
                    });
                } else {
                    MainThread.runLater(mRefreshSelectedViewRunnable);
                }
            }
        });
        mGridView.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        memptyView = inflater.inflate(R.layout.gridview_empty_view, this, false);
        textType = (TextView) memptyView.findViewById(R.id.data_tip);

        mdata_tip = (TextView) findViewById(R.id.data_tip);
        addView(mGridView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mFocusView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(memptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mGridView.setOnItemSelectedListener(this);
        mRefreshSelectedViewRunnable = new Runnable() {
            @Override
            public void run() {
                if (mGridView.getSelectedView() != null) {
                    mGridView.getSelectedView().setSelected(false);
                }
            }
        };
    }
    public void setStyleFocus(int sytle) {
        mFocusView.setFocusFrameImageResourceAndMeasure(sytle);
    }
    public void setDefaultStyle() {
        mFocusView.setFocusFrameImageResourceAndMeasure(R.drawable.focus);
    }
    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        mSelectedListener = l;
    }
    public void setAdapter(ListAdapter adapter) {
        if (adapter.getCount() == 0) {
            if (mdata_tip != null) {
                mdata_tip.setVisibility(View.VISIBLE);
            }
            mGridView.setEmptyView(memptyView);
        }
        mGridView.setAdapter(adapter);
    }
    public void setHorizontalSpacing(int horizontalSpacing) {
        mGridView.setHorizontalSpacing(horizontalSpacing);
    }
    public void setVerticalSpacing(int verticalSpacing) {
        mGridView.setVerticalSpacing(verticalSpacing);
    }
    public void setGridViewSelector(Drawable sel) {
        mGridView.setSelector(sel);
    }
    public void setNumColumns(int num) {
        mGridView.setNumColumns(num);
    }
    public void setOnItemClickListener(OnItemClickListener l) {
        mGridView.setOnItemClickListener(l);
    }
    public void setSelection(int position) {
        mGridView.setSelection(position);
    }
    @Override
    public void onItemSelected(AdapterView<?> arg0, View v, int arg2, long arg3) {
        if (mSelectedListener != null) {
            mSelectedListener.onItemSelected(arg0, v, arg2, arg3);
        }
        animateFoucs(v);
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        if (mSelectedListener != null) {
            mSelectedListener.onNothingSelected(arg0);
        }
    }
    protected void animateFoucs(View v) {
        if (v != null) {
            animateFoucs(v.getLeft(), v.getTop(), v.getWidth(), v.getHeight());
        }
    }
    protected void animateFoucs(int left, int top, int width, int height) {
        if (mGridView.hasFocus()) {
            int oldLeft = mLastFocusPoint.x < 0 ? left : mLastFocusPoint.x;
            int oldTop = mLastFocusPoint.y < 0 ? top : mLastFocusPoint.y;
            mFocusView.move(oldLeft, oldTop, left, top, width, height);
            mLastFocusPoint.set(left, top);
        }
    }
    /**
     * 展示空白页
     *
     * @param
     */
    public void showNoDataPage(String type) {
        textType.setText(type);
        mGridView.setEmptyView(memptyView);
    }

    public void showNoDataPage() {
        mGridView.setEmptyView(memptyView);
    }

    public void syncType(String type){
        textType.setText(type);
    }
}