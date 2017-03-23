package com.cantv.media.center.ui.image;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by shenpx on 2017/3/15 0015.
 */

public class ImageViewPager extends ViewPager {

    private long moveTime;

    public ImageViewPager(Context context) {
        super(context);
    }

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                if (System.currentTimeMillis() - moveTime > 300) {
                    moveTime = System.currentTimeMillis();
                } else {
                    return true;
                }
        }
        return super.dispatchKeyEvent(event);
    }
}
