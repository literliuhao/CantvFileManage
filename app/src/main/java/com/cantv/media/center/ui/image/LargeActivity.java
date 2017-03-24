package com.cantv.media.center.ui.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.cantv.media.R;
import com.cantv.media.center.utils.ToastUtils;

import java.io.File;

public class LargeActivity extends Activity {
    private LargeImageView mLargeImageView;
    public final int UP = 19, DOWN = 20, LEFT = 21, RIGHT = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.large_layout);
        Intent intent = getIntent();
        if (null != intent) {
            String path = intent.getStringExtra("path");
            if (!"".equals(path) && null != path) {
                mLargeImageView = (LargeImageView) findViewById(R.id.iv_photo);
                mLargeImageView.setInputStream(new File(path).getPath());
            } else {
                ToastUtils.showMessage(this, "本地图片地址有误，请重试");
                this.finish();
            }
        } else {
            ToastUtils.showMessage(this, "本地图片地址有误，请重试");
            this.finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.i("LargeActivity", "keyCode " + keyCode);
        switch (keyCode) {
            case UP:
                mLargeImageView.moveEvent(EKeyEvent.UP);
                break;
            case DOWN:
                mLargeImageView.moveEvent(EKeyEvent.DOWN);
                break;
            case LEFT:
                mLargeImageView.moveEvent(EKeyEvent.LEFT);
                break;
            case RIGHT:
                mLargeImageView.moveEvent(EKeyEvent.RIGHT);
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
