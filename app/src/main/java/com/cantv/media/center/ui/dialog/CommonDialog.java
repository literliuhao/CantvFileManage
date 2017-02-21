package com.cantv.media.center.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;

/**
 * 包含两个按钮的通用对话框
 * Created by shenpx on 2017/1/4 0004.
 */
public class CommonDialog extends Dialog implements View.OnFocusChangeListener {

    private OnClickableListener listener;
    private View contentView;
    private FocusUtils mFocusUtils;
    private Button mConfirmBtn;
    private Button mCancelBtn;

    private boolean isFirst = true;
    private TextView mTitle;
    private TextView mText;

    public CommonDialog(Context context) {
        super(context, R.style.dialog_device_share);
        mFocusUtils = new FocusUtils(context, getWindow().getDecorView(), R.drawable.focus_full_content);
        setupLayout(context, R.layout.dialog_common);
    }

    public interface OnClickableListener {
        void onConfirmClickable();

        void onCancelClickable();
    }

    private void setupLayout(Context context, int layoutResId) {
        contentView = View.inflate(context, layoutResId, null);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mConfirmBtn = (Button) contentView.findViewById(R.id.btn_confirm);
        mCancelBtn = (Button) contentView.findViewById(R.id.btn_cancel);
        mTitle = (TextView) contentView.findViewById(R.id.tv_title);
        mText = (TextView) contentView.findViewById(R.id.et_text);

        mConfirmBtn.setOnFocusChangeListener(this);
        mCancelBtn.setOnFocusChangeListener(this);

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmClickable();
                }
                CommonDialog.this.dismiss();
            }

        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancelClickable();
                }
                CommonDialog.this.dismiss();
            }
        });
    }

    public void updateBackground(Drawable drawable) {
        contentView.setBackground(drawable);
    }

    public void reset() {
        mFocusUtils.startMoveFocus(mConfirmBtn, null, true, 0.96f, 0.80f, 0f, 0f);
        mConfirmBtn.requestFocus();
        if (isFirst) {
            isFirst = false;
            getWindow().getDecorView().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mFocusUtils.startMoveFocus(mConfirmBtn, null, true, 0.96f, 0.80f, 0f, 0f);
                }
            }, 500);
        }
    }

    public void setOnClickableListener(OnClickableListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v == mConfirmBtn) {
                mFocusUtils.startMoveFocus(v, null, true, 0.96f, 0.80f, 0f, 0f);
            } else if (v == mCancelBtn) {
                mFocusUtils.startMoveFocus(v, null, true, 0.96f, 0.80f, 0f, 0f);
            }
        }
    }

    /**
     * 是否响应返回键
     * @param isCancelable
     */
    public void setCancel(boolean isCancelable){
        CommonDialog.this.setCancelable(isCancelable);
    }

    /**
     * 设置标题
     * @param title
     * @return
     */
    public CommonDialog setTitle(String title) {
        this.mTitle.setText(title);
        return this;
    }

    /**
     * 设置内容
     * @param text
     * @param start 距离左侧距离
     * @return
     * MyApplication.mContext.getResources().getDimensionPixelSize(R.dimen.px62)
     */
    public CommonDialog setContent(String text,int start) {
        this.mText.setVisibility(View.VISIBLE);
        this.mText.setPadding(start,0,0,0);
        this.mText.setText(text);
        return this;
    }

    /**
     * 设置按钮内容
     * @param left
     * @param right
     * @return
     */
    public CommonDialog setButtonContent(String left,String right) {
        this.mConfirmBtn.setText(left);
        this.mCancelBtn.setText(right);
        return this;
    }

    /**
     * 设置内容大小
     * @param size
     * @return
     */
    public CommonDialog setContentSize(int size) {
        this.mText.setTextSize(size);
        return this;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mFocusUtils != null) {
            mFocusUtils.release();
        }
    }
}
