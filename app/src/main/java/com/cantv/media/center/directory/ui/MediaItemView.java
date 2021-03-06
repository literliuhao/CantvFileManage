package com.cantv.media.center.directory.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cantv.media.center.data.Media;

@SuppressLint("NewApi")
public abstract class MediaItemView extends RelativeLayout {
    MediaItemView(Context context) {
        this(context, null);
    }

    MediaItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    MediaItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    abstract ImageView getFocusImage();

    public abstract void setMediaItem(Media media, int position);

    abstract void animateView(final float from, final float to);

    abstract boolean needReDraw();

    abstract void animateSelection(boolean select);

}
