package com.cantv.media.center.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
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

public class DialogActivity extends Activity implements View.OnFocusChangeListener, IMediaListener, View.OnClickListener {
    private static float mDialogWidth = 0.85f;
    private static float mDialogHeight = 0.91f;
    private FocusScaleUtils mFocusScaleUtils;
    private FocusUtils mFocusUtils;
    private final String SETTING = "android.intent.action.LINK_NETWORK_TOAST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mounted);
        MediaBroadcastReceiver.getInstance().setListener(this);
        mFocusUtils = new FocusUtils(DialogActivity.this, getWindow().getDecorView(), R.drawable.image_focus);
        mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.06f, null, null);
        FrameLayout dialogImage = (FrameLayout) this.findViewById(R.id.dialog_image);
        FrameLayout dialogVideo = (FrameLayout) this.findViewById(R.id.dialog_video);
        FrameLayout dialogAudio = (FrameLayout) this.findViewById(R.id.dialog_audio);
        FrameLayout dialogFile = (FrameLayout) this.findViewById(R.id.dialog_file);
        dialogImage.setOnClickListener(this);
        dialogVideo.setOnClickListener(this);
        dialogAudio.setOnClickListener(this);
        dialogFile.setOnClickListener(this);
        dialogVideo.setOnFocusChangeListener(this);
        dialogAudio.setOnFocusChangeListener(this);
        dialogImage.setOnFocusChangeListener(this);
        dialogFile.setOnFocusChangeListener(this);
        acquireWakeLock();
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        wl.acquire();
    }

    private void startSetting() {
        Intent intent = new Intent(SETTING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        DialogActivity.this.startActivity(intent);
    }

    private void startGridView(String type, int typeInt) {
        EventBus.getDefault().post(new YSourceType(SourceType.PICTURE, typeInt));
        Intent intent = new Intent(DialogActivity.this, GridViewActivity.class);
        intent.putExtra("type", type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (MediaUtils.getCurrPathList().size() > 1) {
            intent.putExtra("toListFlag", "ListFlag");
        }
        DialogActivity.this.startActivity(intent);
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

    @Override
    public void onFinish() {
//        if (mWakeLock == null) {
//            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
//        }
//        mWakeLock.release();
        this.finish();
    }

    @Override
    public void onClick(View v) {
        try {
//            if (!SystemCateUtil.getServerData().equals("1")) {
//                startSetting();
//            } else {
            switch (v.getId()) {
                case R.id.dialog_image:
                    startGridView("image", R.string.str_photo);
                    break;
                case R.id.dialog_video:
                    startGridView("video", R.string.str_movie);
                    break;
                case R.id.dialog_audio:
                    startGridView("audio", R.string.str_music);
                    break;
                case R.id.dialog_file:
                    startGridView("device1", R.string.str_external);
                    break;
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DialogActivity.this.finish();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}