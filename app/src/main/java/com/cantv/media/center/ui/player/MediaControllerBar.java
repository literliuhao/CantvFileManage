package com.cantv.media.center.ui.player;

import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.core.sys.MainThread;
import com.app.core.utils.UiUtils;
import com.cantv.liteplayer.core.LitePlayer;
import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.player.MediaProgressBar.MediaProgressBarDragListener;

public class MediaControllerBar extends FrameLayout implements MediaProgressBarDragListener {
    private final View mNextView;
    private final View mPrevView;
    private final View mRewindView;
    private final View mForwardView;
    private final TextView mMediaTitle;
    private boolean mCanHide = true;

    private final ImageView mPlayOrPauseView;
    private final MediaProgressBar mProgressBar;

    private LitePlayer mPlayer;
    private Runnable mRefreshProgressRunnable;
    private int mToHideCount = 5;
    private int mCurHideCount = 0;
    private boolean mBarShowing = false;

    private AlphaAnimation mAnimation = null;
    private Transformation mDrawingTransform = new Transformation();
    private CustomGallery mCustomGallery;
    private int mCurrentPlayIndex = 0;
    private boolean mPaused = false;
    private MediaPlayListener mplayListener;

    public MediaControllerBar(Context context) {
        this(context, null);
    }

    public MediaControllerBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaControllerBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.media__ctrl_view, this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setDrawingCacheEnabled(false);
        mNextView = findViewById(R.id.media__ctrl_view__next);
        mPrevView = findViewById(R.id.media__ctrl_view__prev);
        mRewindView = findViewById(R.id.media__ctrl_view__rewind);
        mForwardView = findViewById(R.id.media__ctrl_view__forward);
        mMediaTitle = (TextView) findViewById(R.id.media__ctrl_view__name);
        mProgressBar = (MediaProgressBar) findViewById(R.id.media__ctrl_view__progress);
        mPlayOrPauseView = (ImageView) findViewById(R.id.media__ctrl_view__pauseorplay);
        mProgressBar.setMediaProgressBarDragListener(this);
        mRefreshProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mBarShowing) {
                    refresh();
                    if ((++mCurHideCount > mToHideCount) && mCanHide) {
                        toHide();
                    } else {
                        startRefreshProgress();
                    }
                }
            }
        };
        mPlayOrPauseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    if (mPaused == false) {
                        mPaused = true;
                        stopRefreshProgress();
                        mProgressBar.setProgress(mPlayer.getCurrentPosition(), mPlayer.getDuration());
                    } else {
                        mPaused = false;
                        startRefreshProgress();
                    }
                    mplayListener.setPause();
                    int resId = mPaused ? R.drawable.general__share__play : R.drawable.general__share__pause;
                    mPlayOrPauseView.setImageResource(resId);
                }
            }
        });
        mPrevView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    int index = mCurrentPlayIndex - 1;
                    runWhenPlayerContinuePlay();
                    mplayListener.playMedia(index, false);
                    if (mCustomGallery != null) {
                        mCustomGallery.setSelection(index);
                    }
                }
            }
        });
        mNextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    int index = mCurrentPlayIndex + 1;
                    runWhenPlayerContinuePlay();
                    mplayListener.playMedia(index, false);
                    if (mCustomGallery != null) {
                        mCustomGallery.setSelection(index);
                    }
                }
            }
        });
        mRewindView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    long maxDuration = Math.max(mPlayer.getDuration(), mProgressBar.getMaxProgress());
                    long curDuration = Math.max(mPlayer.getCurrentPosition(), mProgressBar.getCurrentProgress());
                    long next = Math.max(0, curDuration - 5000);
                    mProgressBar.setProgress(next, maxDuration);
                    Log.i("", "@wh...next.." + next);
                    runWhenPlayerContinuePlay();
                    mplayListener.seekTo((int) next);
                    // mPlayer.seekTo((int) next);
                }
            }
        });
        mForwardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null && mPlayer.getDuration() > 0) {
                    long maxDuration = Math.max(mPlayer.getDuration(), mProgressBar.getMaxProgress());
                    long curDuration = Math.max(mPlayer.getCurrentPosition(), mProgressBar.getCurrentProgress());
                    long next = Math.min(maxDuration, curDuration + 5000);
                    mProgressBar.setProgress(next, maxDuration);
                    Log.i("", "@wh...next.." + next);
                    runWhenPlayerContinuePlay();
                    mplayListener.seekTo((int) next);
                }
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toShow(true);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        toHide();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mCurHideCount = 0;
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        int drawHeight = getHeight();
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (mAnimation != null && mAnimation.hasEnded() == false) {
            if (mAnimation.hasStarted() == false) mAnimation.setStartTime(currentTime);
            mAnimation.getTransformation(currentTime, mDrawingTransform);
            drawHeight = (int) (mDrawingTransform.getAlpha() * getHeight());
            invalidate();
        }
        canvas.save();
        canvas.translate(0, getHeight() - drawHeight);
        super.draw(canvas);
        canvas.restore();
    }

    @Override
    public void onProgressDragged(long current, long max) {
        runWhenPlayerContinuePlay();
        Log.i("", "@wh...onProgressDragged:  " + current + "/" + max);
        mPlayer.seekTo((int) current);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 按左键后退
            if (mPlayer != null) {
                long maxDuration = Math.max(mPlayer.getDuration(), mProgressBar.getMaxProgress());
                long curDuration = Math.max(mPlayer.getCurrentPosition(), mProgressBar.getCurrentProgress());
                long next = Math.max(0, curDuration - 5000);
                mProgressBar.setProgress(next, maxDuration);
                Log.i("", "@wh...next.." + next);
                runWhenPlayerContinuePlay();
                mplayListener.seekTo((int) next);
                // mPlayer.seekTo((int) next);
            }

            return true;

        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 按右键快进
            if (mPlayer != null && mPlayer.getDuration() > 0) {
                long maxDuration = Math.max(mPlayer.getDuration(), mProgressBar.getMaxProgress());
                long curDuration = Math.max(mPlayer.getCurrentPosition(), mProgressBar.getCurrentProgress());
                long next = Math.min(maxDuration, curDuration + 5000);
                mProgressBar.setProgress(next, maxDuration);
                Log.i("", "@wh...next.." + next);
                runWhenPlayerContinuePlay();
                mplayListener.seekTo((int) next);
            }

            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    public void setCurPlayIndex(int index) {
        mCurrentPlayIndex = index;
    }

    public void setPlayer(LitePlayer player, boolean playByLoop, int index, boolean ispaused, MediaPlayListener playListener) {
        mplayListener = playListener;
        mCurrentPlayIndex = index;
        mPaused = ispaused;
        mPlayer = player;
        if (playByLoop) {
            mPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mPlayer != null) {
                        int index = mCurrentPlayIndex + 1;
                        runWhenPlayerContinuePlay();
                        mProgressBar.setProgress(0, 0);
                        mplayListener.playMedia(index, true);
                        if (mCustomGallery != null) {
                            mCustomGallery.setSelection(index);
                        }
                    }
                }
            });
        }
    }

    public void setMediaGalary(CustomGallery cGallery) {
        mCustomGallery = cGallery;
    }

    public void setCanHide(boolean hide) {
        mCanHide = hide;
    }

    public void toShow(boolean withAnimate) {
        if (mBarShowing == false) {
            mBarShowing = true;
            startRefreshProgress();
            mPlayOrPauseView.requestFocus();
            getChildAt(0).setVisibility(View.VISIBLE);
            refresh();
            flyView(0f, 1f, UiUtils.ANIM_DURATION_SHORT, null);
        }
    }

    public void toHide() {
        if (mBarShowing && mCanHide) {
            mBarShowing = false;
            stopRefreshProgress();
            mCurHideCount = 0;
            flyView(1f, 0f, UiUtils.ANIM_DURATION_SHORT, new Runnable() {
                @Override
                public void run() {
                    getChildAt(0).setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public boolean onBackPressed() {
        if (mCanHide && mBarShowing) {
            toHide();
            return true;
        }
        return false;
    }

    public boolean onKeyPressedContinuity(int keyCode, KeyEvent event) {
        if (mProgressBar.isFocused()) {
//			return mProgressBar.onKeyPressedContinuity(keyCode, event);
        }
        return false;
    }

    public void runWhenActivityPause() {
        if (mPlayer != null && mPaused == false) {
            stopRefreshProgress();
            mProgressBar.setProgress(mPlayer.getCurrentPosition(), mPlayer.getDuration());
            mplayListener.setPause();
            int resId = mPaused ? R.drawable.general__share__play : R.drawable.general__share__pause;
            mPlayOrPauseView.setImageResource(resId);
        }
    }

    public void runWhenActivityfinish() {
        stopRefreshProgress();
        mRefreshProgressRunnable = null;
    }

    public void runWhenActivityResume() {
        toShow(false);
    }

    private void runWhenPlayerContinuePlay() {
        if (mPlayer != null && mPaused) {
            startRefreshProgress();
            mPlayOrPauseView.setImageResource(R.drawable.general__share__play);
        }
    }

    private void refresh() {
        if (mPlayer == null) return;
        Media data = mplayListener.getCurMediaPlay();
        if (data == null) return;
        if (data.getSourceType() == SourceType.MUSIC) {
            mMediaTitle.setVisibility(View.GONE);
        } else {
            mMediaTitle.setVisibility(View.VISIBLE);
        }
        mMediaTitle.setText(data.getName());
        mProgressBar.setProgress(mPlayer.getCurrentPosition(), mPlayer.getDuration());
    }

    private void startRefreshProgress() {
        if (mBarShowing == false) return;
        stopRefreshProgress();
        MainThread.runLater(mRefreshProgressRunnable, 1000);
    }

    private void stopRefreshProgress() {
        MainThread.cancel(mRefreshProgressRunnable);
    }

    private void flyView(float from, float to, int duration, final Runnable onFinish) {
        mAnimation = new AlphaAnimation(from, to);
        mAnimation.setDuration(duration);
        mAnimation.setFillAfter(false);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onFinish != null) onFinish.run();
            }
        });
        invalidate();
    }

    public interface MediaPlayListener {
        void playMedia(int index, boolean isplayloop);

        void setPause();

        void seekTo(int next);

        Media getCurMediaPlay();

    }

}
