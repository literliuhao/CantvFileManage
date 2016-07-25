package com.cantv.media.center.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.cantv.media.R;

public class CoverFlowImageView extends ImageView {
    private Drawable mFocusDrawable;
    private boolean mIsFocus;
    private MediaPlayerControl mmediaPlayerControl;

    public CoverFlowImageView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public CoverFlowImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public CoverFlowImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mFocusDrawable = getResources().getDrawable(R.drawable.f);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mIsFocus = gainFocus;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // if (mIsFocus) {
        // mFocusDrawable.setBounds(this.getLeft()-32, this.getTop()+20,
        // this.getRight()+32, this.getBottom()-20);
        // mFocusDrawable.draw(canvas);
        // }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 下一首
            if (mmediaPlayerControl != null) {
                mmediaPlayerControl.onCoverFlowNext();
            }

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 上一首
            if (mmediaPlayerControl != null) {
                mmediaPlayerControl.onCoverFlowPrev();
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mmediaPlayerControl != null) {
                mmediaPlayerControl.onCoverFlowPlayOrPause();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setMediaPlayerControl(MediaPlayerControl mediaPlayerControl) {
        mmediaPlayerControl = mediaPlayerControl;
    }

    public interface MediaPlayerControl {
        void onCoverFlowNext();

        void onCoverFlowPrev();

        void onCoverFlowPlayOrPause();
    }

}
