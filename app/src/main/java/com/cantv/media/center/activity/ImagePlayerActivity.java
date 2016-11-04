package com.cantv.media.center.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.core.sys.MainThread;
import com.app.core.utils.UiUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.ImageBrowser;
import com.cantv.media.center.ui.ImageFrameView;
import com.cantv.media.center.ui.ImageFrameView.NotifyParentUpdate;
import com.cantv.media.center.ui.ImageFrameView.onLoadingImgListener;
import com.cantv.media.center.ui.MediaControllerBar;
import com.cantv.media.center.utils.DateUtil;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;

import java.util.Date;
import java.util.List;

public class ImagePlayerActivity extends MediaPlayerActivity implements NotifyParentUpdate {
    private int mCurImageIndex;
    private ImageFrameView mFrameView;
    private ImageBrowser mImageBrowser;
    private Runnable mAutoRunnable;
    private ImageView mAutoRunImageView;
    private ImageView mRotation;
    private ImageView mSize;
    private ImageView mInfo;
    private PowerManager.WakeLock mScreenLock;
    private boolean mAutoPlay = false;
    private LinearLayout mLayout;
    private boolean nflag = true;
    public float screenWidth;
    public float screenHeight;
    private Context mContext;
    private BroadcastReceiver mimageReceiver;
    private LinearLayout mediaimagebar;
    private Runnable mToHideRunnable;
    private boolean mShowing = true;
    private int POSTION = 5;
    private FocusUtils mFocusUtils;
    private TextView mTvRotation;
    private TextView mTvSize;
    private TextView mTvAuto;
    private TextView mTvInfo;
    private ImageView mArrowLeft;
    private ImageView mArrowRight;
    private TextView mPosition;
    private TextView mTotal;
    private TextView mInfoName;
    private TextView mInfoSize;
    private TextView mInfoTime;
    private TextView mInfoUrl;
    private RelativeLayout mHeader;
    private ImageView mMusic;
    private AnimationDrawable mAnimationDrawable;
    private static final int DELAYED_TIME = 5 * 1000;
    private final int ARROW_SHOW = 1;
    private final int MENU_SHOW = 2;
    private boolean isFirstFocus = true;
    private boolean isFirstPlayMusic = true;
    private boolean mSizeType = false;
    private boolean isFirstMenu = true;
    private MediaPlayer mMediaPlayer;
    private int PLAYING_STATUS;
    private int STOP = 0;
    private int PLAYING = 1;
    private int PAUSE = 2;
    private static long lastClickTime;
    private int mWidth;
    private int mHeight;
    private int mSizeWidth;
    private int mSizeHeight;
    private Boolean isRotation = false;
    private AudioManager mAudioManager;
    private int mCurrentVolume;
    private Toast mToast = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int flag = msg.what;
            if (flag == 1) {
                if (mArrowLeft.getVisibility() == View.GONE && mArrowRight.getVisibility() == View.GONE) {
                    return;
                }
                mHandler.removeMessages(ARROW_SHOW);
                mArrowLeft.setVisibility(View.GONE);
                mArrowRight.setVisibility(View.GONE);
            } else if (flag == 2) {
                if (mHeader.getVisibility() == View.GONE) {
                    return;
                }
                mHandler.removeMessages(MENU_SHOW);
                mHeader.setVisibility(View.GONE);
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.media__image_view);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        initView();
        showImage(indexOfDefaultPlay(), null);
        initViewClickEvent();
        mCurImageIndex = indexOfDefaultPlay();
        autoRunnable();
        toHideRunnable();
        registerReceiver();
        toHideView();
        MyApplication.addActivity(this);
    }

    private void toHideRunnable() {
        mToHideRunnable = new Runnable() {
            @Override
            public void run() {
                toHideView();
            }
        };
    }

    private void autoRunnable() {
        mAutoRunnable = new Runnable() {
            public void run() {
                int offset = mCurImageIndex + 1;
                offset = (offset >= getData().size()) ? 0 : offset;
                showImage(offset, null);
                startAutoPlay();
            }
        };
    }

    private void registerReceiver() {
        mimageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    if (getData() == null || getData().size() == 0) {
                        return;
                    }
                    String sourcepath = getData().get(0).isSharing ? getData().get(0).sharePath : getData().get(0).mUri;
                    String targetpath = intent.getDataString();
                    boolean isequal = MediaUtils.isEqualDevices(sourcepath, targetpath);
                    if (isequal) {
                        ImagePlayerActivity.this.finish();
                    }
                }
            }
        };
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.setPriority(1000);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addDataScheme("file");
        mContext.registerReceiver(mimageReceiver, usbFilter);
    }

    private void initView() {
        mediaimagebar = (LinearLayout) findViewById(R.id.mediaimagebar);
        mLayout = (LinearLayout) findViewById(R.id.media__image_info_total);
        mInfoName = (TextView) findViewById(R.id.media__image_info_name);
        mInfoSize = (TextView) findViewById(R.id.media__image_info_size);
        mInfoTime = (TextView) findViewById(R.id.media__image_info_time);
        mInfoUrl = (TextView) findViewById(R.id.media__image_info_url);
        mArrowLeft = (ImageView) findViewById(R.id.media__image_view__left);
        mArrowRight = (ImageView) findViewById(R.id.media__image_view__right);
        mPosition = (TextView) findViewById(R.id.media__image_tv__position);
        mTotal = (TextView) findViewById(R.id.media__image_tv__total);
        mTvRotation = (TextView) findViewById(R.id.media__image_tv__rotation);
        mTvSize = (TextView) findViewById(R.id.media__image_tv__size);
        mTvAuto = (TextView) findViewById(R.id.media__image_tv__auto);
        mTvInfo = (TextView) findViewById(R.id.media__image_tv__info);
        mRotation = (ImageView) findViewById(R.id.media__image_view__rotation);
        mSize = (ImageView) findViewById(R.id.media__image_view__size);
        mAutoRunImageView = (ImageView) findViewById(R.id.media__image_view__auto);
        mInfo = (ImageView) findViewById(R.id.media__image_view__info);
        mHeader = (RelativeLayout) findViewById(R.id.media_image_header);
        mFrameView = new ImageFrameView(this);
        mFrameView.setNotifyParentUpdateListner(this);
        mImageBrowser = (ImageBrowser) findViewById(R.id.media__image_view__image);
        mImageBrowser.setContentImageView(mFrameView);
        mImageBrowser.setBackgroundColor(Color.BLACK);
        mImageBrowser.layoutOriginal();
        mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus);
        mMusic = (ImageView) findViewById(R.id.media_view_music);
        mMusic.setBackgroundResource(R.drawable.media_music);
        mAnimationDrawable = (AnimationDrawable) mMusic.getBackground();
    }

    @Override
    protected MediaControllerBar getMediaControllerBar() {
        return null;
    }

    private void showImage(int index, Runnable onfinish) {
        //final List<String> data = getData();
        if (index < 0 || index >= getData().size()) {
            return;
        }

        if (mToast != null) {
            mToast.cancel();
        }
        //修复OS-1578加载图片翻页过程屏幕上多处会显示“白斑”
        mHeader.setVisibility(View.INVISIBLE);
        mArrowLeft.setVisibility(View.GONE);
        mArrowRight.setVisibility(View.GONE);
        mPosition.setText("");
        mTotal.setText("");
        mCurImageIndex = index;
        final int curIndex = index + 1;
        String url = getData().get(index).isSharing ? getData().get(index).sharePath : getData().get(index).mUri;
        boolean isSharing = getData().get(index).isSharing;
        mFrameView.playImage(url, isSharing, onfinish, new onLoadingImgListener() {
            @Override
            public void loadSuccessed() {
                if (isFirstMenu) {
                    isFirstMenu = false;
                    mHeader.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(MENU_SHOW, DELAYED_TIME);
                }
                isRotation = false;
                mSizeType = false;
                mTvSize.setText(getString(R.string.image_full_screen));
                mPosition.setText(String.valueOf(mCurImageIndex + 1));
                mTotal.setText(" / " + getData().size());
                arrowShow(getData());

                UiUtils.runAfterLayout(mImageBrowser, new Runnable() {
                    @Override
                    public void run() {
                        mImageBrowser.reset();
                        mImageBrowser.changeReset();
                        UiUtils.fadeView(mImageBrowser, 0, 1, UiUtils.ANIM_DURATION_LONG_LONG * 0, false, null);
                    }
                });
                if (!mAutoPlay) {
                    if (curIndex == getData().size() && curIndex != 1) {
                        mToast = Toast.makeText(getApplicationContext(), getString(R.string.image_last_photo), Toast.LENGTH_LONG);
                        mToast.show();
                    } else if (curIndex == 1 && getData().size() > 1) {
                        mToast = Toast.makeText(getApplicationContext(), getString(R.string.image_start_photo), Toast.LENGTH_LONG);
                        mToast.show();
                    }
                }
            }

            @Override
            public void bitmapSize(int width, int height) {
                mWidth = width;
                mHeight = height;
                mSizeWidth = width;
                mSizeHeight = height;
            }

            @Override
            public void getSizeSuccessed(int width, int height) {
            }
        });
    }

    /**
     * 计算等比例缩放和实际大小百分比
     *
     * @param width
     * @param height
     * @return
     */
    private float calcByWH(float width, float height, Boolean isFullSize) {
        float currentW;
        float currentH;
        if (!isRotation) {
            currentW = width;
            currentH = height;
        } else {
            currentW = height;
            currentH = width;
        }
        //实际大小
        if (!isFullSize) {
            if (currentW > screenWidth || currentH > screenHeight) {
                if (currentW > screenWidth && currentH > screenHeight) {
                    //取最大的进行缩放
                    return currentW / screenWidth > currentH / screenHeight ? currentW / screenWidth : currentH / screenHeight;
                } else if (currentW > screenWidth) {
                    return currentW / screenWidth;
                } else {
                    return currentH / screenHeight;
                }
            } else {
                //屏幕大于图片宽高时
                return 1.0f;
            }
        } else {
            //等比例全屏
            //图片宽高大于屏幕时
            if (currentW > screenWidth || currentH > screenHeight) {
                //图片实际宽高都大于屏幕宽高
                if (currentW > screenWidth && currentH > screenHeight) {
                    if (!isRotation) {
                        if ((currentW / screenWidth) < (currentH / screenHeight)) {
                            Log.w("scrWidth / currentW ", screenWidth / currentW + "");
                            return screenHeight / currentH;
                        } else {
                            Log.w("scrHeight / currentH ", screenHeight / currentH + "");
                            return screenWidth / currentW;
                        }
                    } else {
                        if ((currentW / screenWidth) > (currentH / screenHeight)) {
                            return screenWidth / currentW;
                        } else {
                            return screenHeight / currentH;
                        }
                    }
                } else if (currentW > screenWidth) {
                    return screenWidth / currentW;
                }
                //  (currentH > screenHeight)
                else {
                    return screenHeight / currentH;
                }

//                else {
//                    if (!isRotation) {
//                        if ((currentW - screenWidth) < (currentH - screenHeight)) {
//                            return screenWidth / currentW;
//                        } else {
//                            return screenHeight / currentH;
//                        }
//                    } else {
//                        if ((currentW - screenWidth) > (currentH - screenHeight)) {
//                            return screenWidth / currentW;
//                        } else {
//                            return screenHeight / currentH;
//                        }
//                    }
//                }
            } else {
                //屏幕大于图片宽高时
                if ((screenWidth / currentW) > (screenHeight / currentH)) {
                    return screenHeight / currentH;
                } else {
                    return screenWidth / currentW;
                }
            }
        }

    }

    private void arrowShow(final List<Media> data) {
        if (!mAutoPlay) {
            if (mCurImageIndex == 0 && data.size() > 1) {
                mArrowRight.setVisibility(View.VISIBLE);
                mArrowLeft.setVisibility(View.GONE);
            } else if (mCurImageIndex == data.size() - 1 && data.size() > 1) {
                mArrowLeft.setVisibility(View.VISIBLE);
                mArrowRight.setVisibility(View.GONE);
            } else if (mCurImageIndex > 0 && data.size() > 1 && mCurImageIndex < data.size() - 1) {
                mArrowLeft.setVisibility(View.VISIBLE);
                mArrowRight.setVisibility(View.VISIBLE);
            } else {
                mArrowLeft.setVisibility(View.GONE);
                mArrowRight.setVisibility(View.GONE);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeMessages(ARROW_SHOW);
                    mHandler.sendEmptyMessageDelayed(ARROW_SHOW, DELAYED_TIME);
                }
            }).start();
        }
    }

    private void initViewClickEvent() {
        mRotation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFastClick() || mAutoPlay) {
                    return;
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
                markRotation();
                mImageBrowser.changeRotation();
                mSizeType = false;
                mTvSize.setText(getString(R.string.image_full_screen));
            }
        });
        mRotation.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    POSTION = 0;
                    mFocusUtils.startMoveFocus(v, true, (float) 1.2);
                    translateDown(mTvRotation);
                } else {
                    translateUp(mTvRotation);
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }
        });
        mSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFastClick() || mAutoPlay) {
                    return;
                }
                if (!mSizeType) {
                    mSizeType = true;
                    mTvSize.setText(getString(R.string.image_real_size));
                } else {
                    mSizeType = false;
                    mTvSize.setText(getString(R.string.image_full_screen));
                }
                mImageBrowser.onZoomScale(calcByWH(mWidth, mHeight, mSizeType));
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }
        });
        mSize.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    POSTION = 1;
                    mFocusUtils.startMoveFocus(v, true, (float) 1.2);
                    translateDown(mTvSize);
                } else {
                    translateUp(mTvSize);
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }
        });
        mAutoRunImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
                int curIndex = mCurImageIndex + 1;
                int size = getData().size();
                if (mAutoPlay) {
                    stopAutoPlay();
                    Toast.makeText(ImagePlayerActivity.this, getString(R.string.image_end_play), Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                } else {
                    mImageBrowser.setSoundEffectsEnabled(false);
                    mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
                    startAutoPlay();
                    if (isFirstPlayMusic) {
                        isFirstPlayMusic = false;
                    } else {
                    }
                    Toast.makeText(ImagePlayerActivity.this, getString(R.string.image_start_play), Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info33);
                }
            }
        });
        mAutoRunImageView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    POSTION = 2;
                    mFocusUtils.startMoveFocus(v, true, (float) 1.2);
                    translateDown(mTvAuto);
                } else {
                    translateUp(mTvAuto);
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }
        });
        mInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutoPlay) {
                    return;
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
                if (nflag) {
                    mLayout.setVisibility(View.VISIBLE);
                    // 初始化
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1.0f);
                    // 设置动画时间
                    alphaAnimation.setDuration(500);
                    alphaAnimation.setFillAfter(true);
                    mLayout.startAnimation(alphaAnimation);
                    nflag = false;
                } else {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
                    // 设置动画时间
                    alphaAnimation.setDuration(500);
                    alphaAnimation.setFillAfter(true);
                    mLayout.startAnimation(alphaAnimation);
                    mLayout.setVisibility(View.GONE);
                    nflag = true;
                }
                String curFileUri = getData().get(mCurImageIndex).isSharing ? getData().get(mCurImageIndex).sharePath : getData().get(mCurImageIndex).mUri;
                mInfoName.setText(getString(R.string.image_name) + "：" + getData().get(mCurImageIndex).mName);
                mInfoSize.setText(getString(R.string.image_volume) + "：" + FileUtil.convertStorage(getData().get(mCurImageIndex).fileSize));
                mInfoUrl.setText(getString(R.string.image_size) + "：" + mSizeWidth + "*" + mSizeHeight);
                mInfoTime.setText(getString(R.string.image_time) + "：" + DateUtil.onDate2String(new Date(getData().get(mCurImageIndex).modifiedDate), "yyyy-MM-dd HH:mm"));
            }
        });
        mInfo.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    POSTION = 3;
                    mFocusUtils.startMoveFocus(v, true, (float) 1.2);
                    translateDown(mTvInfo);
                } else {
                    translateUp(mTvInfo);
                    if (mLayout.getVisibility() == View.VISIBLE) {
                        Animation translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, 100.0f);
                        // 设置动画时间
                        translateAnimation.setDuration(300);
                        mLayout.startAnimation(translateAnimation);
                        mLayout.setVisibility(View.GONE);
                        nflag = true;
                    }
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }
        });
    }

    private PowerManager.WakeLock getScreenLock() {
        if (mScreenLock == null) {
            mScreenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");
        }
        return mScreenLock;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAutoPlay();
        mAutoRunImageView.setImageResource(R.drawable.photo_info3);
    }

    private void startAutoPlay() {
        int curIndex = mCurImageIndex + 1;
        int size = getData().size();
        /*if (curIndex == size) {
            stopAutoPlay();
            //stopMusic();
            mAutoRunImageView.setImageResource(R.drawable.photo_info3);
            return;
        }*/
        if (mAutoPlay == false) {
            mAutoPlay = true;
            getScreenLock().acquire();
        }
        //startMusicAnimation();
        MainThread.runLater(mAutoRunnable, 5000);
    }

    private void stopAutoPlay() {
        if (mAutoPlay) {
            mAutoPlay = false;
            //endMusicAnimation();
            mImageBrowser.setSoundEffectsEnabled(true);
            mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mCurrentVolume, 0);
            MainThread.cancel(mAutoRunnable);
            getScreenLock().release();
        }
    }

    private void toHideView() {
        if (mShowing == true) {
            mShowing = false;
            forceHideView();
        }
    }

    private void forceHideView() {
        mShowing = false;
        mediaimagebar.setVisibility(View.GONE);
        MainThread.cancel(mToHideRunnable);
        mFocusUtils.hideFocus();
        toFlyView(0, 0, 0, 1, true, false);
    }

    private void toShowView() {
        if (mShowing) return;
        switch (POSTION) {
            case 0:
                mRotation.requestFocus();
                break;
            case 1:
                mSize.requestFocus();
                break;
            case 2:
                mAutoRunImageView.requestFocus();
                break;
            case 3:
                mInfo.requestFocus();
                break;
            default:
                break;
        }
        mShowing = true;
        MainThread.runLater(mToHideRunnable, 5 * 1000);
        mediaimagebar.setVisibility(View.VISIBLE);
        toFlyView(0, 0, 1, 0, true, true);
    }

    private void toFlyView(float fromXValue, float toXValue, float fromYValue, float toYValue, boolean fillAfter, final Boolean status) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue, Animation.RELATIVE_TO_SELF, toYValue);
        animation.setDuration(UiUtils.ANIM_DURATION_LONG);
        animation.setFillAfter(fillAfter);
        mediaimagebar.clearAnimation();
        mediaimagebar.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (status) {
                    if (isFirstFocus) {
                        isFirstFocus = false;
                        mRotation.requestFocus();
                        mFocusUtils.setFocusLayout(mRotation, true, (float) 1.2);
                    }
                    mFocusUtils.showFocus();
                } else {
                    mFocusUtils.hideFocus();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void markRotation() {
        if (isRotation) {
            isRotation = false;
        } else {
            isRotation = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mShowing) {
            if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                toHideView();
            }
            if (keyCode == event.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
                toHideView();
            }
            return false;
        }
        if (keyCode == event.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
            toShowView();
            return true;
        }
        if (!mAutoPlay) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isFastClick()) {
                    return true;
                }
                int offset = mCurImageIndex - 1;
                offset = (offset < 0) ? getData().size() - 1 : offset;
                showImage(offset, null);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isFastClick()) {
                    return true;
                }
                int offset = mCurImageIndex + 1;
                offset = (offset >= getData().size()) ? 0 : offset;
                showImage(offset, null);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isFastClick()) {
                    return true;
                }
                markRotation();
                mImageBrowser.changeRotation();
                mSizeType = false;
                mTvSize.setText(getString(R.string.image_full_screen));
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isFastClick()) {
                    return true;
                }
                markRotation();
                mImageBrowser.changeUpRotation();
                mSizeType = false;
                mTvSize.setText(getString(R.string.image_full_screen));
                return true;
            }
        }
        if (mAutoPlay) {
            if (!mShowing) {
                if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    stopAutoPlay();
                    //stopMusic();
                    Toast.makeText(ImagePlayerActivity.this, getString(R.string.image_end_play), Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                    return true;
                }
                /*
                 * if (keyCode == event.KEYCODE_DPAD_CENTER && event.getAction()
                 * == KeyEvent.ACTION_DOWN) { stopAutoPlay();
                 * Toast.makeText(ImagePlayerActivity.this, "结束幻灯片播放",
                 * Toast.LENGTH_SHORT).show();
                 * mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                 * return true; }
                 */
            }
        }
        /*
         * if (keyCode == event.KEYCODE_DPAD_CENTER && event.getAction() ==
         * KeyEvent.ACTION_DOWN) { if (!mShowing) { if (mAutoPlay) {
         * //stopAutoPlay(); //Toast.makeText(ImagePlayerActivity.this,
         * "结束幻灯片播放", Toast.LENGTH_SHORT).show();
         * //mAutoRunImageView.setImageResource(R.drawable.photo_info3); } else
         * { startAutoPlay(); Toast.makeText(ImagePlayerActivity.this,
         * "开始幻灯片播放", Toast.LENGTH_SHORT).show();
         * mAutoRunImageView.setImageResource(R.drawable.photo_info33); }
         * 
         * } return true; }
         */
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAutoPlay) {
            stopAutoPlay();
            //stopMusic();
            mMusic.setVisibility(View.GONE);
            mAnimationDrawable.stop();
        }
        if (mHandler != null) {
            mHandler.removeMessages(ARROW_SHOW);
            mHandler.removeMessages(MENU_SHOW);
        }
        unregisterReceiver(mimageReceiver);
        if (null != mFrameView.mBitmap) {
            mFrameView.mBitmap = null;
        }
        MyApplication.removeActivity(this);
    }

    @SuppressLint("NewApi")
    private long getBitmapsize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    @Override
    public void update() {
        mImageBrowser.reset();
    }

    private void translateDown(View view) {
        Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.2f);
        translateAnimation.setDuration(200);
        translateAnimation.setFillAfter(true);
        view.clearAnimation();
        view.startAnimation(translateAnimation);
    }

    private void translateUp(View view) {
        Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.2f, Animation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(200);
        translateAnimation.setFillAfter(true);
        view.clearAnimation();
        view.startAnimation(translateAnimation);
    }

    /*private void stopMusic() {
        stop();
        isFirstPlayMusic = true;
    }

    private void startMusic() {
        play();
    }

    private void pauseMusic() {
        pause();
    }

    private void resumeMusic() {
        resume();
    }

    private void play() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.reset();
            AssetManager assetManager = mContext.getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("mm.mp3");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mMediaPlayer.start();
                    mMediaPlayer.setLooping(true);
                }
            });
            PLAYING_STATUS = PLAYING;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            PLAYING_STATUS = PAUSE;
        }
    }

    private void resume() {
        if (mMediaPlayer != null && PLAYING_STATUS == PAUSE) {
            mMediaPlayer.start();
            PLAYING_STATUS = PLAYING;
        }
    }

    private void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            PLAYING_STATUS = STOP;
        }
    }*/

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

   /* private void startMusicAnimation() {
        if (!mAnimationDrawable.isRunning()) {
            mMusic.setVisibility(View.VISIBLE);
            mAnimationDrawable.start();
        }
    }

    private void endMusicAnimation() {
        if (mAnimationDrawable.isRunning()) {
            mMusic.setVisibility(View.GONE);
            mAnimationDrawable.stop();
        }
    }*/
}