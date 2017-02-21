package com.cantv.media.center.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.activity.GridViewActivity;
import com.cantv.media.center.utils.MediaUtils;

@SuppressLint("ResourceAsColor")
public class CustomDialog extends Dialog {
    private static float mDialogWidth = 0.85f;
    private static float mDialogHeight = 0.91f;

    private static CustomDialog customDialog;

    public static CustomDialog getInstance(Context context){
        if(null == customDialog){
            customDialog = new CustomDialog.Builder(context).create();
        }
        return customDialog;
    }

    private CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    private CustomDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder implements View.OnFocusChangeListener {

        private FocusScaleUtils mFocusScaleUtils;
        private FocusUtils mFocusUtils;
        private Context mContext;

        public Builder(Context context) {
            this.mContext = context;

        }

        /**
         * 创建这个自定义的对话框，填充数据
         */
        @SuppressLint("InflateParams")
        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog dialog = new CustomDialog(mContext, R.style.dialog_transparent);
            View layout = inflater.inflate(R.layout.dialog_mounted, null);

            int layoutWidth = (int) mContext.getResources().getDimension(R.dimen.px1920);
            int layoutHeight = (int) mContext.getResources().getDimension(R.dimen.px1080);

            dialog.addContentView(layout, new LayoutParams(layoutWidth, layoutHeight));
            mFocusUtils = new FocusUtils(mContext, layout, R.drawable.image_focus);
            mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.06f, null, null);

            FrameLayout dialogImage = (FrameLayout) layout.findViewById(R.id.dialog_image);
            dialogImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GridViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (MediaUtils.getCurrPathList().size() > 1) {
                        intent.putExtra("toListFlag", "ListFlag");
                    }
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });
            FrameLayout dialogVideo = (FrameLayout) layout.findViewById(R.id.dialog_video);
            dialogVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GridViewActivity.class);
                    intent.putExtra("type", "video");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (MediaUtils.getCurrPathList().size() > 1) {
                        intent.putExtra("toListFlag", "ListFlag");
                    }
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });
            FrameLayout dialogAudio = (FrameLayout) layout.findViewById(R.id.dialog_audio);
            dialogAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GridViewActivity.class);
                    intent.putExtra("type", "audio");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (MediaUtils.getCurrPathList().size() > 1) {
                        intent.putExtra("toListFlag", "ListFlag");
                    }
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });
            FrameLayout dialogFile = (FrameLayout) layout.findViewById(R.id.dialog_file);
            dialogFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GridViewActivity.class);
                    if (MediaUtils.getCurrPathList().size() > 1) {
                        intent.putExtra("toListFlag", "ListFlag");
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", "device1");
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });
            dialogVideo.setOnFocusChangeListener(this);
            dialogAudio.setOnFocusChangeListener(this);
            dialogImage.setOnFocusChangeListener(this);
            dialogFile.setOnFocusChangeListener(this);

            dialog.setContentView(layout);
            return dialog;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mFocusScaleUtils.scaleToLarge(v);
                mFocusUtils.startMoveFocus(v, null, true, mDialogWidth, mDialogHeight, 0f, 0f);
            } else {
                mFocusScaleUtils.scaleToNormal(v);
            }
        }


        public void release() {
            if (mFocusUtils != null) {
                mFocusUtils.release();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
