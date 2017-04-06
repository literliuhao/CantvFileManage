package com.cantv.media.center.index.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.directory.ui.GridViewActivity;

/**
 * Created by liuhao on 16/7/14.
 * 插入外接设备时的弹窗
 */
public class ActivityDialog extends Activity {
    private FocusScaleUtils mFocusScaleUtils;
    private Context mContext;
    private FocusUtils mFocusUtils;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.05f, null, null);
        mContext = this;
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        window.setContentView(R.layout.dialog_mounted);
        window.setBackgroundDrawableResource(R.color.transparent);
        mFocusUtils = new FocusUtils(this, window.getDecorView(), R.drawable.focus);
        View dialogImage = window.findViewById(R.id.dialog_image);
        dialogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "image");
                startActivity(intent);
            }
        });
        View dialogVideo = window.findViewById(R.id.dialog_video);
        dialogVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "video");
                startActivity(intent);
            }
        });
        View dialogAudio = window.findViewById(R.id.dialog_audio);
        dialogAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "audio");
                startActivity(intent);
            }
        });
        View dialogFile = window.findViewById(R.id.dialog_file);
        dialogFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "device1");
                startActivity(intent);
            }
        });
        dialogVideo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusScaleUtils.scaleToLarge(v);
                    mFocusUtils.startMoveFocus(v, true, 0.85f);
                } else {
                    mFocusScaleUtils.scaleToNormal(v);
                }
            }
        });
        dialogAudio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusScaleUtils.scaleToLarge(v);
                    mFocusUtils.startMoveFocus(v, true, 0.85f);
                } else {
                    mFocusScaleUtils.scaleToNormal(v);
                }
            }
        });
        dialogImage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusScaleUtils.scaleToLarge(v);
                    mFocusUtils.startMoveFocus(v, true, 0.85f);
                } else {
                    mFocusScaleUtils.scaleToNormal(v);
                }
            }
        });
        dialogFile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusScaleUtils.scaleToLarge(v);
                    mFocusUtils.startMoveFocus(v, true, 0.85f);
                } else {
                    mFocusScaleUtils.scaleToNormal(v);
                }
            }
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFocusUtils.release();
    }
}
