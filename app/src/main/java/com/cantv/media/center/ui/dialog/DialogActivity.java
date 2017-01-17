package com.cantv.media.center.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.liteplayer.core.interfaces.IMediaListener;
import com.cantv.media.R;
import com.cantv.media.center.activity.GridViewActivity;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.YSourceType;
import com.cantv.media.center.receiver.MediaBroadcastReceiver;
import com.cantv.media.center.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;

public class DialogActivity extends Activity implements View.OnFocusChangeListener, IMediaListener {
    private static float mDialogWidth = 0.85f;
    private static float mDialogHeight = 0.91f;
    private FocusScaleUtils mFocusScaleUtils;
    private FocusUtils focusUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mounted);
        MediaBroadcastReceiver.getInstance().setListener(this);
        focusUtils = new FocusUtils(DialogActivity.this, getWindow().getDecorView(), R.drawable.image_focus);
        mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.06f, null, null);
        FrameLayout dialogImage = (FrameLayout) this.findViewById(R.id.dialog_image);
        dialogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogActivity.this, GridViewActivity.class);
                intent.putExtra("type", "image");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (MediaUtils.getCurrPathList().size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                EventBus.getDefault().post(new YSourceType(SourceType.PICTURE, "图片"));
                DialogActivity.this.startActivity(intent);
                DialogActivity.this.finish();
            }
        });
        FrameLayout dialogVideo = (FrameLayout) this.findViewById(R.id.dialog_video);
        dialogVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogActivity.this, GridViewActivity.class);
                intent.putExtra("type", "video");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (MediaUtils.getCurrPathList().size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }

                EventBus.getDefault().post(new YSourceType(SourceType.MOIVE, "视频"));

                DialogActivity.this.startActivity(intent);
                DialogActivity.this.finish();
            }
        });
        FrameLayout dialogAudio = (FrameLayout) this.findViewById(R.id.dialog_audio);
        dialogAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogActivity.this, GridViewActivity.class);
                intent.putExtra("type", "audio");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (MediaUtils.getCurrPathList().size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                EventBus.getDefault().post(new YSourceType(SourceType.MUSIC, "音频"));
                DialogActivity.this.startActivity(intent);
                DialogActivity.this.finish();
            }
        });
        FrameLayout dialogFile = (FrameLayout) this.findViewById(R.id.dialog_file);
        dialogFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogActivity.this, GridViewActivity.class);
                if (MediaUtils.getCurrPathList().size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", "device1");
                EventBus.getDefault().post(new YSourceType(SourceType.DEVICE, "外接设备"));
                DialogActivity.this.startActivity(intent);
                DialogActivity.this.finish();
            }
        });
        dialogVideo.setOnFocusChangeListener(this);
        dialogAudio.setOnFocusChangeListener(this);
        dialogImage.setOnFocusChangeListener(this);
        dialogFile.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mFocusScaleUtils.scaleToLarge(v);
            focusUtils.startMoveFocus(v, null, true, mDialogWidth, mDialogHeight, 0f, 0f);
        } else {
            mFocusScaleUtils.scaleToNormal(v);
        }
    }

    @Override
    public void onFinish() {
        this.finish();
    }
}