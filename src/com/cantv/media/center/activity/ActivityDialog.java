package com.cantv.media.center.activity;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by liuhao on 16/7/14.
 */
public class ActivityDialog extends Activity {
    private FocusScaleUtils mFocusScaleUtils;
    private Context mContext;

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
        final FocusUtils focusUtils = new FocusUtils(this, window.getDecorView(), R.drawable.focus);
        ImageView dialogImage = (ImageView) window.findViewById(R.id.dialog_image);
        dialogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "image");
                // if (mUsbRootPaths.size() > 1) {
                // intent.putExtra("toListFlag", "ListFlag");
                // }
                startActivity(intent);
            }
        });
        ImageView dialogVideo = (ImageView) window.findViewById(R.id.dialog_video);
        dialogVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "video");
                // if (mUsbRootPaths.size() > 1) {
                // intent.putExtra("toListFlag", "ListFlag");
                // }
                startActivity(intent);
            }
        });
        ImageView dialogAudio = (ImageView) window.findViewById(R.id.dialog_audio);
        dialogAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "audio");
                // if (mUsbRootPaths.size() > 1) {
                // intent.putExtra("toListFlag", "ListFlag");
                // }
                startActivity(intent);
            }
        });
        ImageView dialogFile = (ImageView) window.findViewById(R.id.dialog_file);
        dialogFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GridViewActivity.class);
                // if (mUsbRootPaths.size() > 1) {
                // intent.putExtra("toListFlag", "ListFlag");
                // }
                intent.putExtra("type", "device1");
                startActivity(intent);
            }
        });
        dialogVideo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFocusScaleUtils.scaleToLarge(v);
                    focusUtils.startMoveFocus(v, true, 0.85f);
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
                    focusUtils.startMoveFocus(v, true, 0.85f);
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
                    focusUtils.startMoveFocus(v, true, 0.85f);
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
                    focusUtils.startMoveFocus(v, true, 0.85f);
                } else {
                    mFocusScaleUtils.scaleToNormal(v);
                }
            }
        });
    }
}
