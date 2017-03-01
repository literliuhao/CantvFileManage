package com.cantv.media.center.ui.upgrade;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cantv.media.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ResourceAsColor")
public class CustomDialog extends Dialog implements OnShowListener {

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom upgrade_dialog
     */
    public static class Builder {

        private static final String TextView = null;
        private Button mPosBtn;
        private Context context;
        private String title;
        private String newcode;

        private List<String> list = new ArrayList<>();

        private String positiveButtonText;

        private OnClickListener positiveButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置新的版本号
         *
         * @param newcode
         * @return
         */
        public Builder setNewcode(String newcode) {
            this.newcode = newcode;
            return this;
        }

        /**
         * 设置新版本信息
         *
         * @param list
         * @return
         */
        public Builder setList(List list) {
            this.list = list;
            return this;
        }

        /**
         * 设置对话框标题
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set a custom content view for the Dialog. If a message is set, the contentView is not added to the
         * Dialog...
         *
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            return this;
        }

        /**
         * 设置升级按钮的文本和监听事件
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText, OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setStyle(int style) {
            return this;
        }

        /**
         * 创建这个自定义的对话框，填充数据
         */
        @SuppressLint("InflateParams")
        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog dialog = new CustomDialog(context, R.style.upgrade_dialog);
            View layout = inflater.inflate(R.layout.upgrade_dialog, null);
            dialog.addContentView(layout, new LayoutParams(1000, 680));

            // 设置标题文本
            android.widget.TextView tv_title = ((android.widget.TextView) layout.findViewById(R.id.update_title));
            tv_title.setText(title);
            // 设置标题字体加粗
            tv_title.getPaint().setFakeBoldText(true);

            // 设置新版本
            ((android.widget.TextView) layout.findViewById(R.id.update_newcode)).setText(newcode);

            // 设置"关于本次升级"的字体加粗
            android.widget.TextView about = (android.widget.TextView) layout.findViewById(R.id.update_about);
            about.getPaint().setFakeBoldText(true);

            // 得到LinearLayout控件，添加更新的版本信息
            LinearLayout ll_textinfo = (LinearLayout) layout.findViewById(R.id.update_ll_text);
            if (list != null && list.size() > 0) {

                for (int i = 0; i < list.size(); i++) {
                    String upgradeInfo = list.get(i);
                    if (TextUtils.isEmpty(upgradeInfo) || "null".equalsIgnoreCase(upgradeInfo)) {
                        continue;
                    }
                    LinearLayout ll = new LinearLayout(context);
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    ImageView img = new ImageView(context);
                    img.setBackgroundResource(R.drawable.upgrade_point);
                    ll.addView(img);
                    android.widget.TextView tv = new TextView(context);
                    tv.setText(upgradeInfo);
                    tv.setTextSize((float) 24);
                    ll.addView(tv);
                    ll_textinfo.addView(ll);
                }
            } else {
                list.add("优化了部分功能");
                list.add("修复了一些问题");
                list.add("提高了设备的稳定性");
                for (int i = 0; i < list.size(); i++) {
                    LinearLayout ll = new LinearLayout(context);
                    ll.setOrientation(LinearLayout.HORIZONTAL);
                    ImageView img = new ImageView(context);
                    img.setBackgroundResource(R.drawable.upgrade_point);
                    ll.addView(img);
                    android.widget.TextView tv = new TextView(context);
                    tv.setText(list.get(i));
                    tv.setTextSize((float) 24);
                    ll.addView(tv);
                    ll_textinfo.addView(ll);
                }
            }

            // 设置升级按钮
            if (positiveButtonText != null) {
                mPosBtn = ((Button) layout.findViewById(R.id.positiveButton));
                mPosBtn.setText(positiveButtonText);

                if (positiveButtonClickListener != null) {
                    mPosBtn.setFocusable(true);
                    mPosBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
                layout.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }
            dialog.setContentView(layout);
            return dialog;
        }

    }

    /**
     * 升级按钮获取焦点
     */
    @Override
    public void onShow(DialogInterface dialog) {
    }

}
