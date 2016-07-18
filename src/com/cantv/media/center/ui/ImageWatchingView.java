package com.cantv.media.center.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.app.core.ui.ZoomView;
import com.app.core.utils.UiUtils;

public class ImageWatchingView extends ZoomView {
    private View mFrameView;

    public ImageWatchingView(Context context) {
        this(context, null);
    }

    public ImageWatchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setContentView(View contentView, LayoutParams layoutParams) {
        removeAllViews();
        mFrameView = contentView;
        if (mFrameView != null) {
            addView(mFrameView, layoutParams == null ? new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) : layoutParams);
        }
        makeContentToStaticStatus(false);
    }

    public void reset() {
        makeContentToStaticStatus(false);
    }

    public void onZoomIn() {
        float newfactor = getZoomFactor() + 0.50f;
        if (newfactor - clacFitScreenScale() > 1.0f) {
            newfactor = clacFitScreenScale() + 1.0f;
        }
        zoomAndCenterChildSmoothlyTo(mFrameView.getWidth() / 2, mFrameView.getHeight() / 2, newfactor, null, null);
    }

    public void onZoomOut() {
        float newfactor = getZoomFactor() - 0.50f;
        if (newfactor - clacFitScreenScale() > 1.0f) {
            newfactor = clacFitScreenScale() + 1.0f;
        }
        zoomAndCenterChildSmoothlyTo(mFrameView.getWidth() / 2, mFrameView.getHeight() / 2, newfactor, null, null);
    }

    public void onRotationChanged(final int newRotation, final boolean restZoomFactor) {
        final View view = mFrameView;
        final Point oldCenter = new Point(getScrollX() + getWidth() / 2, getScrollY() + getHeight() / 2);
        UiUtils.transformPoint(oldCenter, this, view);
        final float oldFactor = getZoomFactor();
        UiUtils.runAfterLayout(view, new Runnable() {
            @Override
            public void run() {
                final float newfactor = restZoomFactor ? clacFitScreenScale() : getZoomFactor();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        setMinZoomFactor(newfactor);
                    }
                };
                forceZoomAndCenterChildTo(oldCenter.x, oldCenter.y, oldFactor, newRotation + getZoomAngle());
                forceZoomAndCenterChildSmoothlyTo(view.getWidth() / 2, view.getHeight() / 2, newfactor, 0, runnable, runnable);
            }
        });
    }

    private float clacFitScreenScale() {
        if (getWidth() > mFrameView.getWidth() && getHeight() > mFrameView.getHeight()) {
            return 1.0f;
        }
        return Math.min((float) getWidth() / mFrameView.getWidth(), (float) getHeight() / mFrameView.getHeight());
    }

    private void makeContentToStaticStatus(final boolean smoothly) {
        UiUtils.runAfterLayout(mFrameView, new Runnable() {
            @Override
            public void run() {
                Point center = getContentStaticCenter();
                setMinZoomFactor(clacFitScreenScale());
                if (smoothly == false) {
                    zoomAndCenterChildTo(center.x, center.y, clacFitScreenScale());
                } else {
                    zoomAndCenterChildSmoothlyTo(center.x, center.y, clacFitScreenScale(), null, null);
                }
            }
        });
    }

    private Point getContentStaticCenter() {
        Point point = new Point(0, 0);
        if (mFrameView != null) {
            point.set(mFrameView.getWidth() / 2, mFrameView.getHeight() / 2);
        }
        return point;
    }

    @Override
    protected long getAnimateDuration() {
        return super.getAnimateDuration() * 2;
    }
}
