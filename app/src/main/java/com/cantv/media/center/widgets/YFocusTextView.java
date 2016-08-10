package com.cantv.media.center.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by yibh on 2016/8/10  11:36.
 * 用来自动获取焦点的TextView,用在轮播
 */

public class YFocusTextView extends TextView {


    public YFocusTextView(Context context) {
        super(context);
    }

    public YFocusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public YFocusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}
