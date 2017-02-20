package com.cantv.media.center.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.utils.NetworkUtils;

public class DeviceAddDialog extends Dialog implements OnFocusChangeListener {
    private OnIpConfirmedListener listener;
    private View contentView;
    private EditText mIpEt;
    private FocusUtils mFocusUtils;
    private Button mConfirmBtn;
    private Button mCancelBtn;
    private Context mContext;
    private boolean isFirst = true;
    private final int IP_LENGHT = 0;

    public DeviceAddDialog(final Context context) {
        super(context, R.style.dialog_device_share);
        mFocusUtils = new FocusUtils(context, getWindow().getDecorView(), R.drawable.focus_full_content);
        setupLayout(context, R.layout.dialog_device_add);
    }

    public interface OnIpConfirmedListener {
        void onConfirmed(String ip);
    }

    private void setupLayout(Context context, int layoutResId) {
        mContext = context;
        contentView = View.inflate(context, layoutResId, null);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mIpEt = (EditText) contentView.findViewById(R.id.et_ip);
        mConfirmBtn = (Button) contentView.findViewById(R.id.btn_confirm);
        mCancelBtn = (Button) contentView.findViewById(R.id.btn_cancel);

        mIpEt.setOnFocusChangeListener(this);
        mConfirmBtn.setOnFocusChangeListener(this);
        mCancelBtn.setOnFocusChangeListener(this);

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmed(mIpEt.getText().toString());
                }
            }

        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DeviceAddDialog.this.dismiss();
            }
        });
    }

    public void updateBackground(Drawable drawable) {
        contentView.setBackground(drawable);
    }

    public void reset() {
        //产品新需求-----------------------
        //1.文件共享添加设备时输入框默认显示当前已连接网络IP的前三项，例如192.168.1.
        //2.添加设备时软键盘应自动调起，光标在输入框内容的最后闪动显示。
        //3.软键盘调起时，保证不会遮挡输入框。
        int type = NetworkUtils.getNetInfo(mContext).getType();
        String strIP;
        if (type == ConnectivityManager.TYPE_WIFI) {
            strIP = NetworkUtils.getWiFiIp(mContext.getApplicationContext());
        } else {
            strIP = NetworkUtils.getEthernetIp(mContext.getApplicationContext());
        }
        mIpEt.setText(strIP.substring(IP_LENGHT, strIP.lastIndexOf(".") + 1));
        mIpEt.requestFocus();
        mIpEt.setSelection(mIpEt.length());
        InputMethodManager imm = (InputMethodManager) mIpEt.getContext().getSystemService(mContext.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
        //End------------------------------
        mFocusUtils.setFocusLayout(mIpEt, false, 0);
        if (isFirst) {
            isFirst = false;
            getWindow().getDecorView().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mFocusUtils.startMoveFocus(mIpEt, false, 0);
                }
            }, 500);
        }
    }

    public void setOnIpConfirmedListener(OnIpConfirmedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(final View v, boolean hasFocus) {
        if (hasFocus) {
            if (v == mIpEt) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFocusUtils.startMoveFocus(v, false, 0);
                    }
                }, 100);
            } else if (v == mConfirmBtn || v == mCancelBtn) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFocusUtils.startMoveFocus(v, null, true, 0.97f, 0.91f, 0f, -8f);
                    }
                }, 100);
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mFocusUtils.release();
    }
}
