package com.cantv.media.center.ui.share;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cantv.media.R;

public class DeviceShareItemView extends RelativeLayout {
    public static final int TYPE_ADD_DEVICE = 1;
    public static final int TYPE_DEVICE = 2;

    private int mViewType;
    private ImageView mLogoIv;
    private TextView mIpTv;

    public DeviceShareItemView(Context context) {
        this(context, null, 0);
    }

    public DeviceShareItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeviceShareItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DeviceShareItemView);
        int viewType = ta.getInt(R.styleable.DeviceShareItemView_viewType, 0);
        ta.recycle();
        initUI(viewType);
        setFocusable(true);
    }

    private void initUI(int viewType) {
        View view = View.inflate(getContext(), R.layout.layout_device_share_item, null);
        mLogoIv = (ImageView) view.findViewById(R.id.iv_Logo);
        mIpTv = (TextView) view.findViewById(R.id.tv_ip);
        addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        setViewType(viewType);
    }

    public void setViewType(int viewType) {
        if (viewType != TYPE_ADD_DEVICE && viewType != TYPE_DEVICE) {
            return;
        }
        if (viewType == mViewType) {
            return;
        }
        mViewType = viewType;
        if (viewType == TYPE_DEVICE) {
            mLogoIv.setImageResource(R.drawable.icon_share);
            mIpTv.setText("");
        } else if (viewType == TYPE_ADD_DEVICE) {
            mLogoIv.setImageResource(R.drawable.icon_add);
            mIpTv.setText(getResources().getString(R.string.add_device));
        }
    }

    public void setIp(String ip) {
        if (mViewType != TYPE_DEVICE) {
            return;
        }
        mIpTv.setText(ip);
    }

}
