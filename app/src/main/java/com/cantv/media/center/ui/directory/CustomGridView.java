package com.cantv.media.center.ui.directory;

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
import com.cantv.liteplayer.core.focus.GridFocusView;
import com.cantv.media.R;

@SuppressLint("ResourceAsColor")
public class CustomGridView extends FrameLayout implements OnItemSelectedListener {
    public GridView mGridView;
    private GridFocusView mFocusView;
    private Point mLastFocusPoint = new Point(-1, -1);
    private OnItemSelectedListener mSelectedListener = null;
    private Runnable mRefreshSelectedViewRunnable;
    private View mEmptyView;
    private TextView mDataTip;
    private TextView mTextType;
    private Boolean animate = true;

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
        mGridView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                MainThread.cancel(mRefreshSelectedViewRunnable);
                mFocusView.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
                if (hasFocus) {
                    MainThread.runLater(new Runnable() {
                        public void run() {
                            animateFocus(mGridView.getSelectedView());
                        }
                    });
                } else {
                    MainThread.runLater(mRefreshSelectedViewRunnable);
                }
            }
        });
        mGridView.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mEmptyView = inflater.inflate(R.layout.gridview_empty_view, this, false);
        mTextType = (TextView) mEmptyView.findViewById(R.id.data_tip);

        mDataTip = (TextView) findViewById(R.id.data_tip);
        addView(mGridView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mFocusView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mEmptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mGridView.setOnItemSelectedListener(this);
        mRefreshSelectedViewRunnable = new Runnable() {
            @Override
            public void run() {
                if (null != mGridView.getSelectedView()) {
                    mGridView.getSelectedView().setSelected(false);
                }
            }
        };
    }

    public void setStyleFocus(int style) {
        mFocusView.setFocusFrameImageResourceAndMeasure(style);
    }

    public void setDefaultStyle() {
        mFocusView.setFocusFrameImageResourceAndMeasure(R.drawable.focus);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        mSelectedListener = l;
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter.getCount() == 0) {
            if (mDataTip != null) {
                mDataTip.setVisibility(View.VISIBLE);
            }
            mGridView.setEmptyView(mEmptyView);
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
        animate = false;
        mGridView.setSelection(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
        if (null != mSelectedListener) {
            mSelectedListener.onItemSelected(arg0, view, arg2, arg3);
        }
        animateFocus(view);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        if (mSelectedListener != null) {
            mSelectedListener.onNothingSelected(arg0);
        }
    }

    protected void animateFocus(View v) {
        if (v != null) {
            animateFocus(v.getLeft(), v.getTop(), v.getWidth(), v.getHeight());
        }
    }

    protected void animateFocus(int left, int top, int width, int height) {
        if (mGridView.hasFocus()) {
            int oldLeft = mLastFocusPoint.x < 0 ? left : mLastFocusPoint.x;
            int oldTop = mLastFocusPoint.y < 0 ? top : mLastFocusPoint.y;
            mFocusView.move(oldLeft, oldTop, left, top, width, height, animate);
            animate = true;
            mLastFocusPoint.set(left, top);
        }
    }

    /**
     * 展示空白页
     *
     * @param
     */
    public void showNoDataPage(String type) {
        mTextType.setText(type);
        mGridView.setEmptyView(mEmptyView);
    }

    public void showNoDataPage() {
        mGridView.setEmptyView(mEmptyView);
    }

    public void syncType(String type) {
        mTextType.setText(type);
    }
}