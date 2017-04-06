package com.cantv.media.center.player;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cantv.media.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PlayerController extends RelativeLayout {

    private static final int CHANG_PROGRESS = 0;
    private static final int CHANG_PLAYIMAGE = 1;
    private static final int CHANG_VISIBLE = 2;
    private static final int STORE_DURATION = 3;
    private static final int CONTINUE_PLAY = 4;
    private static final int CHANG_SRT = 5;
    private static final int SEEK_DURATION = 6;
    private static final int CHANGE_PLAY_VISIBILITY = 7;
    private int mDuration;
    private boolean isHasDefinition;
    private boolean isFirstEnter = true;
    private boolean isShowTip = false;
    private boolean isSrtExist;

    private Context mContext;
    private TimeProgressBar mProgressBar;
    private ImageView mPlayImage;
    private TextView mTitle;
    private TextView mTime, mDefinitionTv;
    private TextView mMovieTimeTv;
    private SimpleDateFormat format;
    private PlayerCtrlBarContext mCtrlBarContext;
    private PlayerCtrlBarListener mCtrlBarListener;
    private CoverFlowViewListener mCoverFlowViewListener;
    private TextView mTip, mContinueText;
    private ImageView mTipImage;
    private LinearLayout mContinuePlay;
    private long currentTime = 0;
    private long lastClickTime = 0;
    public static final int MIN_CLICK_DELAY_TIME = 2000;
    /**
     * 长按步长
     */
    private int mStepSize;
    /**
     * 默认步长
     */
    private static final int DEFAULT_STEP_SIZE = 5000;
    /**
     * 是否是到达最大速度
     */
    private boolean reachMaxG = false;

    private int mTmpSecondProgress;

    private int mKeyDownRepeatCount = 0;

    private boolean preIsPause;   //快进/退之前是否处于暂停状态

    private static final String TAG = "PlayerController";
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case CHANG_PROGRESS:
                    mProgressBar.setProgress(mCtrlBarContext.getPlayerCurPosition());
                    mMovieTimeTv.setText(time2String(mCtrlBarContext.getPlayerCurPosition()) + " / " + time2String(mDuration));
                    handler.sendEmptyMessageDelayed(PlayerController.CHANG_PROGRESS, 1000);
                    break;

                case CHANG_PLAYIMAGE:
                    if (mCtrlBarContext.isPlayerPaused()) {
                        mPlayImage.setVisibility(VISIBLE);
                        mPlayImage.setBackgroundResource(R.drawable.play_play);
                    }
                    break;

                case CHANG_VISIBLE:
                    if (mCtrlBarContext.isPlayerPaused()) {
                        showPause(false);
                    } else {
                        setVisibility(INVISIBLE);
                    }
                    break;

                case STORE_DURATION:

                    ((VideoPlayActivity) mContext).storeDuration();
                    handler.sendEmptyMessageDelayed(STORE_DURATION, 60 * 1000);

                    break;

                case CONTINUE_PLAY:

                    isShowTip = false;
                    mContinuePlay.setVisibility(INVISIBLE);

                    break;
                case CHANG_SRT:
                    ((VideoPlayActivity) mContext).setSrts(mCtrlBarContext.getPlayerCurPosition());
                    ((VideoPlayActivity) mContext).setSub(mCtrlBarContext.getPlayerCurPosition());

                    handler.sendEmptyMessageDelayed(PlayerController.CHANG_SRT, 1000);
                    break;

                case SEEK_DURATION:
                    mProgressBar.setSecondProgressEnable(false);

                    seekToDuration(mTmpSecondProgress);
                    break;
                case CHANGE_PLAY_VISIBILITY:
                    mPlayImage.setVisibility(INVISIBLE);
                    break;
                default:
                    break;
            }

        }

    };
    private RelativeLayout mRlProgress;

    public interface PlayerCtrlBarContext {
        String getPlayerTitle();

        int getPlayerDuration();

        int getPlayerCurPosition();

        boolean isPlayerPaused();

        String getDefinition();
    }

    public interface PlayerCtrlBarListener {

        void onPlayerPlayOrPause();

        void onPlaySeekTo(int duration, OnSeekCompleteListener listener);

    }

    public interface CoverFlowViewListener {
        boolean scrollToNext(OnCompletionListener listener);

        boolean scrollPre(OnCompletionListener listener);
    }

    public PlayerController(Context context) {
        super(context);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        inflate(context, R.layout.player_controller, this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setDrawingCacheEnabled(false);

        mRlProgress = (RelativeLayout) findViewById(R.id.rl_progress);
        mProgressBar = (TimeProgressBar) findViewById(R.id.pb_progress);
        mTime = (TextView) findViewById(R.id.tv_time);
        mTitle = (TextView) findViewById(R.id.tv_name);
        mPlayImage = (ImageView) findViewById(R.id.iv_play);
        mPlayImage.setFocusable(false);
        mDefinitionTv = (TextView) findViewById(R.id.tv_definiton);
        mTip = (TextView) findViewById(R.id.tv_menu);
        mTipImage = (ImageView) findViewById(R.id.iv_menu);
        mContinueText = (TextView) findViewById(R.id.tv_continue_play);
        mContinuePlay = (LinearLayout) findViewById(R.id.rl_continue);
        mMovieTimeTv = (TextView) findViewById(R.id.player_txt_time);

        TextPaint tp = mContinueText.getPaint();
        tp.setFakeBoldText(true);
    }

    public void setPlayerCtrlBarListener(PlayerCtrlBarListener listener) {
        mCtrlBarListener = listener;
    }

    public void setPlayerControllerBarContext(PlayerCtrlBarContext context) {
        mCtrlBarContext = context;
    }

    public void setPlayerCoverFlowViewListener(CoverFlowViewListener listener) {
        mCoverFlowViewListener = listener;
    }

    public void setPlayDuration() {

        isHasDefinition = TextUtils.isEmpty(mCtrlBarContext.getDefinition());

        if (!isHasDefinition) {
            mDefinitionTv.setText(mCtrlBarContext.getDefinition());
            mDefinitionTv.setVisibility(View.VISIBLE);
        } else {
            mDefinitionTv.setVisibility(View.INVISIBLE);
        }

        if (isFirstEnter) {
            isFirstEnter = false;
        } else {
            //mProgressBar.initProgress();
        }
        mDuration = mCtrlBarContext.getPlayerDuration();
        mProgressBar.setDuration(mCtrlBarContext.getPlayerDuration());
        handler.removeMessages(PlayerController.CHANG_PROGRESS);
        handler.sendEmptyMessage(PlayerController.CHANG_PROGRESS);
        handler.sendEmptyMessage(PlayerController.CHANG_PLAYIMAGE);
        handler.sendEmptyMessageDelayed(PlayerController.CHANG_VISIBLE, 5000);
        mTitle.setText(mCtrlBarContext.getPlayerTitle());
        setVisibility(VISIBLE);
        mTitle.bringToFront();
        mTime.bringToFront();
        // 设置当前时间
        refreshTime();
        //如果有字幕，开始获取字幕
        isSrtExist = ((VideoPlayActivity) mContext).isSrtExist();
        if (isSrtExist) {
            handler.sendEmptyMessageDelayed(PlayerController.CHANG_SRT, 2000);
        }
        handler.sendEmptyMessage(STORE_DURATION);

        mMovieTimeTv.setText(time2String(0) + " / " + time2String(mDuration));
    }

    public void showContinuePlay(int position) {
        //大于一千毫秒才有意义,否则time2String(position)转化的也是0
        if (!(position > 1000)) {
            return;
        }
        seekToDuration(position);
        mPlayImage.setVisibility(INVISIBLE);
        mContinuePlay.setVisibility(VISIBLE);
        mContinueText.setText("从" + time2String(position) + "开始，继续为您播放");
        isShowTip = true;
        handler.sendEmptyMessageDelayed(CONTINUE_PLAY, 5000);
    }

    @SuppressLint("SimpleDateFormat")
    public void refreshTime() {
        ContentResolver cv = mContext.getContentResolver(); // 获取当前系统设置
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        if ("12".equals(strTimeFormat)) {
            Log.i("activity", "当前是12制式");
            //strTimeFormat.equals("12")
            format = new SimpleDateFormat("hh:mm");
        } else {
            format = new SimpleDateFormat("HH:mm");
        }
        String time = format.format(new Date(System.currentTimeMillis()));
        mTime.setText(time);
    }

    public void onKeyUpEvent(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handler.removeMessages(SEEK_DURATION);
                handler.sendEmptyMessageDelayed(SEEK_DURATION, 500);
                break;
            default:
                break;
        }
    }

    private Boolean isContuine() {
        currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            return true;
        }
        return false;
    }

    public void onKeyDownEvent(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (!isShowTip) {
                    togglePlayImgvi();
                    mCtrlBarListener.onPlayerPlayOrPause();
                    delayHidePlayImgvi();
                }
                showController();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                preIsPause = mCtrlBarContext.isPlayerPaused();
                handler.removeMessages(CHANG_PLAYIMAGE);
                mProgressBar.setSecondProgressEnable(true);
                handler.removeMessages(SEEK_DURATION);
                if (event.getRepeatCount() == 0) {
                    mKeyDownRepeatCount++;
                    if (mKeyDownRepeatCount == 1) {
                        mTmpSecondProgress = mCtrlBarContext.getPlayerCurPosition();
                    }
                }
                seekPosition(event);
                toggleSeekImgvi(keyCode);
                showController();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                showController();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                showController();
                if (isShowTip) {
                    seekToDuration(0);
                    ((VideoPlayActivity) mContext).initSrts();
                    isShowTip = false;
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                if (isContuine()) {
                    mPlayImage.setVisibility(INVISIBLE);
                    boolean isPre = mCoverFlowViewListener.scrollPre(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mTmpSecondProgress = 0;
                        }
                    });
                    if (isPre) {
                        handler.removeMessages(PlayerController.CHANG_PROGRESS);
                        showController();
                    }
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                if (isContuine()) {
                    mPlayImage.setVisibility(INVISIBLE);
                    boolean isNext = mCoverFlowViewListener.scrollToNext(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mTmpSecondProgress = 0;
                        }
                    });

                    if (isNext) {
                        handler.removeMessages(PlayerController.CHANG_PROGRESS);
                        showController();
                    }
                }

                break;
            case KeyEvent.KEYCODE_MENU:
                mRlProgress.setVisibility(INVISIBLE);
            default:
                break;
        }

    }

    public void seekToDuration(int duration) {
        handler.removeMessages(PlayerController.CHANG_PROGRESS);
        handler.removeMessages(PlayerController.CHANG_SRT);
//        Log.w("onSeekComplete", duration + " ~~~");
        mCtrlBarListener.onPlaySeekTo(duration, new OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer arg0) {
//                Log.w("onSeekComplete~~ ", mCtrlBarContext.getPlayerDuration() + " ~~~");
                delayHidePlayImgvi();
                handler.sendEmptyMessageDelayed(PlayerController.CHANG_PLAYIMAGE, 200);
                handler.sendEmptyMessage(PlayerController.CHANG_PROGRESS);
                if (isSrtExist) {
                    handler.sendEmptyMessage(PlayerController.CHANG_SRT);
                }
                mProgressBar.setProgress(mCtrlBarContext.getPlayerCurPosition());
                mKeyDownRepeatCount = 0;
            }
        });

//        if (preIsPause) {    //之前是暂停状态就依然保持暂停(新需求快进快退后继续播放)
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mCtrlBarListener.onPlayerPlayOrPause();
//                    handler.sendEmptyMessage(PlayerController.CHANG_PLAYIMAGE);
//                }
//            }, 200);
//        }

    }

    public void removeAllMessage() {
        handler.removeCallbacksAndMessages(null);
    }

    private void showPause(boolean isVisible) {
        if (isVisible) {
            mTitle.setVisibility(VISIBLE);
            mTime.setVisibility(VISIBLE);
            mTitle.bringToFront();
            mTime.bringToFront();
            mProgressBar.setVisibility(VISIBLE);
            mTip.setVisibility(VISIBLE);
            mTipImage.setVisibility(VISIBLE);
            mMovieTimeTv.setVisibility(View.VISIBLE);
            if (!isHasDefinition) {
                mDefinitionTv.setVisibility(VISIBLE);
            }

        } else {
            mTitle.setVisibility(INVISIBLE);
            mDefinitionTv.setVisibility(INVISIBLE);
            mTime.setVisibility(INVISIBLE);
            mProgressBar.setVisibility(INVISIBLE);
            mTip.setVisibility(INVISIBLE);
            mTipImage.setVisibility(INVISIBLE);
            mMovieTimeTv.setVisibility(View.INVISIBLE);
        }

    }

    private void toggleSeekImgvi(int keyCode) {
        if (View.INVISIBLE == mPlayImage.getVisibility() && !isShowTip) {
            mPlayImage.setVisibility(View.VISIBLE);
        }
        if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
            mPlayImage.setBackgroundResource(R.drawable.play_kt);
        } else if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
            mPlayImage.setBackgroundResource(R.drawable.play_kj);
        }
    }

    public void showController() {
        handler.removeMessages(PlayerController.CHANG_VISIBLE);
        handler.sendEmptyMessageDelayed(PlayerController.CHANG_VISIBLE, 5000);
        setVisibility(VISIBLE);
        mRlProgress.setVisibility(VISIBLE);
        showPause(true);
    }

    public void setFullProgress() {
        mProgressBar.setProgress(mDuration);
    }

    public void onBackPressed(VideoPlayActivity context) {
        if (VISIBLE == getVisibility()) {
            if (INVISIBLE == mProgressBar.getVisibility() && mCtrlBarContext.isPlayerPaused()) {
                mPlayImage.setVisibility(INVISIBLE);
                context.finish();
            } else if (VISIBLE == mProgressBar.getVisibility() && mCtrlBarContext.isPlayerPaused()) {
                mProgressBar.setVisibility(INVISIBLE);
                mPlayImage.setVisibility(VISIBLE);
            } else {
                setVisibility(INVISIBLE);
            }
        } else {
            context.finish();
        }
    }

    private void togglePlayImgvi() {
        if (mCtrlBarContext.isPlayerPaused()) {
            handler.removeMessages(CHANGE_PLAY_VISIBILITY);
            handler.sendEmptyMessageDelayed(CHANGE_PLAY_VISIBILITY, 1000);
        } else {
            mPlayImage.setBackgroundResource(R.drawable.play_play);
            mPlayImage.setVisibility(VISIBLE);
        }
    }

    private void delayHidePlayImgvi() {
        if (!mCtrlBarContext.isPlayerPaused()) {
            handler.removeMessages(CHANGE_PLAY_VISIBILITY);
            handler.sendEmptyMessageDelayed(CHANGE_PLAY_VISIBILITY, 100);
        }
    }

    // 视频小于10分钟
    private void seekPosition(KeyEvent event) {
        if (mDuration <= 1000 * 60 * 10) {
            if (KeyEvent.KEYCODE_DPAD_LEFT == event.getKeyCode()) {
                mTmpSecondProgress = (mTmpSecondProgress - DEFAULT_STEP_SIZE) <= 0 ? 0 : (mTmpSecondProgress - DEFAULT_STEP_SIZE);
                mProgressBar.setSecondProgress(mTmpSecondProgress);
            } else if (KeyEvent.KEYCODE_DPAD_RIGHT == event.getKeyCode()) {
                mTmpSecondProgress = (mTmpSecondProgress + DEFAULT_STEP_SIZE) >= mDuration ? mDuration : (mTmpSecondProgress + DEFAULT_STEP_SIZE);
                mProgressBar.setSecondProgress(mTmpSecondProgress);
            }
        } else {
            obtainSeekPosition(event);
        }
    }

    // 视频时间大于10分钟
    private void obtainSeekPosition(KeyEvent event) {
        int repeatCount = event.getRepeatCount();
        if (repeatCount == 0) {
            mStepSize = DEFAULT_STEP_SIZE + 5000;
            reachMaxG = false;
        }
        double ss1 = Math.sin(repeatCount * 5 * Math.PI / 180);
        if (!reachMaxG) {
            if (ss1 < 0) {
                reachMaxG = true;
            }
            mStepSize += ss1 * 3000;
        }
        if (KeyEvent.KEYCODE_DPAD_LEFT == event.getKeyCode()) {
            mTmpSecondProgress -= mStepSize;
            if (mTmpSecondProgress <= 0) {
                mTmpSecondProgress = 0;
            }
        } else if (KeyEvent.KEYCODE_DPAD_RIGHT == event.getKeyCode()) {
            mTmpSecondProgress += mStepSize;
            if (mTmpSecondProgress >= mDuration) {
                mTmpSecondProgress = mDuration;
            }
        }
        mProgressBar.setSecondProgress(mTmpSecondProgress);
    }

    protected String time2String(int timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String continueTime = formatter.format(timeInMillis);
        return continueTime;
    }

    /**
     * 发送消息,开始执行sub字幕(不含idx)相关的内容
     */
    public void subSendMsg() {
        handler.sendEmptyMessageDelayed(PlayerController.CHANG_SRT, 1000);
    }

}
