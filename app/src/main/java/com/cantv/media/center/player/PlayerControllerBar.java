package com.cantv.media.center.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.core.sys.MainThread;
import com.app.core.utils.UiUtils;
import com.cantv.media.R;
import com.cantv.media.center.player.MediaProgressBar.MediaProgressBarDragListener;

public class PlayerControllerBar extends FrameLayout implements MediaProgressBarDragListener {
    private static final String TAG = "PlayerControllerBar";
    private final View mNextView;
    private final View mPrevView;
    private final View mRewindView;
    private final View mForwardView;
    private final TextView mTitleView;
    private final TextView mmenutip;

    private final ImageView mPlayOrPauseView;
    protected final MediaProgressBar mProgressBar;
    private PlayerCtrlBarContext mCtrlBarContext;
    private PlayerCtrlBarListener mCtrlBarListener;

    private boolean mShowing = false;
    private boolean mCanHide = true;
    private final Runnable mToHideRunnable;
    private final Runnable mRefreshProgressBarRunnable;
    private CoverFlowViewListener mCoverFlowViewListener;
    private ShowMediaTitle mShowMediaTitle;

    public interface PlayerCtrlBarContext {
        String getPlayerTitle();

        int getPlayerDuration();

        int getPlayerCurPosition();

        boolean isPlayerPaused();
    }

    public interface PlayerCtrlBarListener {
        void onPlayNext();

        void onPlayPrev();

        void onPlayRewind();

        void onPlayForwad();

        void onPlayerPlayOrPause();

        void onPlaySeekTo(int duration);

    }

    public interface CoverFlowViewListener {
        void scrollToNext();

        void scrollPre();
    }

    public interface ShowMediaTitle {
        void updateMediaTitle(String title);
    }

    public void setShowMediaTitle(ShowMediaTitle l) {
        mShowMediaTitle = l;
    }

    public void setCoverFlowViewListener(CoverFlowViewListener l) {
        mCoverFlowViewListener = l;
    }

    public PlayerControllerBar(Context context) {
        this(context, null);
    }

    public PlayerControllerBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerControllerBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.media__ctrl_view, this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setDrawingCacheEnabled(false);
        mmenutip = (TextView) findViewById(R.id.media__ctrl_view__tip);
        mNextView = findViewById(R.id.media__ctrl_view__next);
        mPrevView = findViewById(R.id.media__ctrl_view__prev);
        mRewindView = findViewById(R.id.media__ctrl_view__rewind);
        mForwardView = findViewById(R.id.media__ctrl_view__forward);
        mTitleView = (TextView) findViewById(R.id.media__ctrl_view__name);
        mProgressBar = (MediaProgressBar) findViewById(R.id.media__ctrl_view__progress);
        mPlayOrPauseView = (ImageView) findViewById(R.id.media__ctrl_view__pauseorplay);
        mProgressBar.setMediaProgressBarDragListener(this);
        mNextView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mNextView.setFocusable(true);
                    mNextView.setFocusableInTouchMode(true);
                    return true;
                }
                return false;
            }
        });
        mPrevView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mPrevView.setFocusable(true);
                    mPrevView.setFocusableInTouchMode(true);
                    return true;
                }
                return false;
            }
        });
        mPlayOrPauseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrlBarListener != null) mCtrlBarListener.onPlayerPlayOrPause();
                refreshView();
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);

            }
        });
        mPrevView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrlBarListener != null) mCtrlBarListener.onPlayPrev();
                refreshView();
                if (mCoverFlowViewListener != null) {
                    mCoverFlowViewListener.scrollPre();
                }
            }
        });
        mNextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrlBarListener != null) mCtrlBarListener.onPlayNext();
                refreshView();
                if (mCoverFlowViewListener != null) {
                    mCoverFlowViewListener.scrollToNext();
                }
            }
        });
        mRewindView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrlBarListener != null) {
                    mCtrlBarListener.onPlayRewind();
                    mCtrlBarListener.onPlayerPlayOrPause();
                }
                // if (mCtrlBarListener != null)
                // mCtrlBarListener.onPlayRewind();

                refreshView();

            }
        });
        mForwardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCtrlBarListener != null) {
                    mCtrlBarListener.onPlayForwad();
                    mCtrlBarListener.onPlayerPlayOrPause();

                }
                // if (mCtrlBarListener != null)
                // mCtrlBarListener.onPlayForwad();

                refreshView();
            }
        });
        mToHideRunnable = new Runnable() {
            @Override
            public void run() {
                toHideView();
            }
        };
        mRefreshProgressBarRunnable = new Runnable() {
            @Override
            public void run() {
                refreshView();
                MainThread.runLater(this, 1000);
            }
        };
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toShowView();
            }
        });
        toShowView();
    }

    @Override
    public void onProgressDragged(long current, long max) {
        if (mCtrlBarListener != null) {
            mCtrlBarListener.onPlaySeekTo((int) current);
            mCtrlBarListener.onPlayerPlayOrPause();
        }
        refreshView();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            MainThread.cancel(mToHideRunnable);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            MainThread.cancel(mToHideRunnable);
            MainThread.runLater(mToHideRunnable, 5 * 1000);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
            toShowView();
            refreshView();
            mProgressBar.requestFocus();
            return true;

        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
            toShowView();
            refreshView();
            mProgressBar.requestFocus();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        forceHideView();
    }

    public TextView getMenuTip() {
        return mmenutip;
    }

    public void setCanHideView(boolean allow) {
        mCanHide = allow;
    }

    public void setPlayerCtrlBarListener(PlayerCtrlBarListener listener) {
        mCtrlBarListener = listener;
    }

    public void setPlayerControllerBarContext(PlayerCtrlBarContext context) {
        mCtrlBarContext = context;
    }

    public boolean onBackPressed() {
        if (mCanHide && mShowing) {
            forceHideView();
            return true;
        }
        return false;
    }

    private void toHideView() {
        if (mShowing == true && mCanHide) {
            if (!mCtrlBarContext.isPlayerPaused()) {
                mShowing = false;
                forceHideView();
            }
        }
    }

    private void forceHideView() {
        mShowing = false;
        MainThread.cancel(mToHideRunnable);
        MainThread.cancel(mRefreshProgressBarRunnable);
        requestFocus();
        toFlyView(0, 0, 0, 1, true);
    }

    private void toShowView() {
        if (mShowing) return;
        mShowing = true;
        MainThread.runLater(mToHideRunnable, 5 * 1000);
        MainThread.runLater(mRefreshProgressBarRunnable);
        mPlayOrPauseView.requestFocus();
        toFlyView(0, 0, 1, 0, true);
    }

    private void refreshView() {
        if (mShowMediaTitle != null) {
            mShowMediaTitle.updateMediaTitle(mCtrlBarContext.getPlayerTitle());
        }
        mTitleView.setText(mCtrlBarContext.getPlayerTitle());
        int resId = mCtrlBarContext.isPlayerPaused() ? R.drawable.general__share__play : R.drawable.general__share__pause;
        mPlayOrPauseView.setImageResource(resId);
        mProgressBar.setProgress(mCtrlBarContext.getPlayerCurPosition(), mCtrlBarContext.getPlayerDuration());
    }

    private void toFlyView(float fromXValue, float toXValue, float fromYValue, float toYValue, boolean fillAfter) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue, Animation.RELATIVE_TO_SELF, toYValue);
        animation.setDuration(UiUtils.ANIM_DURATION_LONG);
        animation.setFillAfter(fillAfter);
        clearAnimation();
        startAnimation(animation);
    }

    public void coverFlowNext() {
        if (mCtrlBarListener != null) mCtrlBarListener.onPlayNext();
        refreshView();
        if (mCoverFlowViewListener != null) {
            mCoverFlowViewListener.scrollToNext();
        }
    }

    public void coverFlowPre() {
        if (mCtrlBarListener != null) mCtrlBarListener.onPlayPrev();
        refreshView();
        if (mCoverFlowViewListener != null) {
            mCoverFlowViewListener.scrollPre();
        }
    }

    public void coverFlowPlayorPause() {
        if (mCtrlBarListener != null) mCtrlBarListener.onPlayerPlayOrPause();
        refreshView();
        MainThread.cancel(mToHideRunnable);
        MainThread.runLater(mToHideRunnable, 5 * 1000);
    }

}
