package com.cantv.media.center.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
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
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.ui.image.ImageBrowser;
import com.cantv.media.center.ui.image.ImageFrameView;
import com.cantv.media.center.ui.image.ImageFrameView.NotifyParentUpdate;
import com.cantv.media.center.ui.image.ImageFrameView.onLoadingImgListener;
import com.cantv.media.center.ui.player.MediaControllerBar;
import com.cantv.media.center.utils.DateUtil;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 浏览图片页面
 */
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
    private long MENU_DURATION = 500;
    private boolean mLoadSuccessed = false;

    private boolean mLoadReady = false;
    private TextView mLoadingFail;
    private boolean mFullScreen;
    private Boolean isIN = false;
    private List<Integer> keyList = null;
    private List<Integer> dynamicList = null;
    private final int VALUE = 23;
    private final String PRIVATE_KEY = "19!20!19!20";

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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.media__image_view);
        EventBus.getDefault().register(this);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        initView();
        showImage(indexOfDefaultPlay(), null);
        initViewClickEvent();
        mCurImageIndex = indexOfDefaultPlay();
        initKey(PRIVATE_KEY);
        autoRunnable();
        toHideRunnable();
        toHideView();
        MyApplication.addActivity(this);
    }

    private void initKey(String PRIVATE_KEY) {
        keyList = new ArrayList<>();
        String[] stringKEYS = PRIVATE_KEY.split("!");
        for (int i = 0; i < stringKEYS.length; i++) {
            keyList.add(Integer.valueOf(stringKEYS[i]));
        }
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
            }
        };
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
        mLoadingFail = (TextView) findViewById(R.id.tv_loading_fail);
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
        mLoadingFail.setVisibility(View.GONE);
        if (mShowing) {
            MENU_DURATION = 0;
            toHideView();
            MENU_DURATION = 500;
        }
        //修改幻灯片播放问题，时间不准
        if (mAutoPlay) {
            stopAutoPlay();
            mAutoPlay = true;
        }
        mCurImageIndex = index;
        final int curIndex = index + 1;
        String url = getData().get(index).isSharing ? getData().get(index).sharePath : getData().get(index).mUri;
        boolean isSharing = getData().get(index).isSharing;
        String imageName = getData().get(mCurImageIndex).mName;
        mFrameView.playImage(url, isSharing, imageName, onfinish, new onLoadingImgListener() {
            @Override
            public void loadSuccess(boolean loadSuccess) {
                mLoadSuccessed = loadSuccess;
                //修复MASERATI-63USB幻灯片播放破损图片出现很抱歉，文件管理已停止运行，添加文字提示
                if (!loadSuccess) {
                    mLoadingFail.setVisibility(View.VISIBLE);
                    //修复幻灯片播放破损图停止问题
                    if (mAutoPlay) {
                        mAutoPlay = false;
                        startAutoPlay();
                    }
                }
                if (isFirstMenu) {
                    isFirstMenu = false;
                    mHeader.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(MENU_SHOW, DELAYED_TIME);
                }
                isRotation = false;
                mSizeType = false;
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
            }

            @Override
            public void bitmapSize(int width, int height) {
                mWidth = width;
                mHeight = height;
                mSizeWidth = width;
                mSizeHeight = height;
            }

            @Override
            public void isFullScreen(boolean isFullScreen) {
                mFullScreen = isFullScreen;
            }

            @Override
            public void loadResourceReady(boolean isLoadReady) {
                mLoadReady = isLoadReady;
                if (isLoadReady) {
                    if (!mAutoPlay) {
                        if (curIndex == getData().size() && curIndex != 1) {
                            mToast = Toast.makeText(getApplicationContext(), getString(R.string.image_last_photo), Toast.LENGTH_LONG);
                            mToast.show();
                        } else if (curIndex == 1 && getData().size() > 1) {
                            mToast = Toast.makeText(getApplicationContext(), getString(R.string.image_start_photo), Toast.LENGTH_LONG);
                            mToast.show();
                        }
                    }
                    //修改幻灯片播放问题，时间不准
                    if (mAutoPlay) {
                        mAutoPlay = false;
                        startAutoPlay();
                    }
                }
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
            mSizeType = true;
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
                if (currentW < screenWidth || currentH < screenHeight) {
                    if ((screenWidth / currentW) > (screenHeight / currentH)) {
                        return screenHeight / currentH;
                    } else {
                        return screenWidth / currentW;
                    }
                } else {
                    //屏幕大于图片宽高时
                    return 1.0f;
                }
            }
        } else {
            //等比例全屏
            //图片宽高大于屏幕时
            mSizeType = false;
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
            } else {
                //屏幕大于图片宽高时
                return 1.0f;
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
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
                markRotation();
                mImageBrowser.changeRotation();
                mSizeType = false;
                changeTvSize();
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
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
            }
        });
        mSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFastClick() || mAutoPlay) {
                    return;
                }
                //修复OS-2838大图浏览本地图片，按遥控器菜单键切换图片比例无效,再次切换才有效
                if (!mSizeType) {
                    resetTvSize();
                } else {
                    changeTvSize();
                }
                float calc = calcByWH(mWidth, mHeight, mSizeType);
                Log.i("ImagePlayerActivity", "calc " + calc);
                mImageBrowser.onZoomScale(calc);
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
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
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
            }
        });
        mAutoRunImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
                int curIndex = mCurImageIndex + 1;
                int size = getData().size();
                if (mAutoPlay) {
                    stopAutoPlay();
                    openVolume();
                    Toast.makeText(ImagePlayerActivity.this, getString(R.string.image_end_play), Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                } else {
                    mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    closeVolume();
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
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
            }
        });
        mInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAutoPlay) {
                    return;
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
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
                        //修复OS-2627 在本地储存中打开一个图片,点击图片信息按左键,在焦点移动时,图片信息会向上跳一下在消失
                        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
                        alphaAnimation.setDuration(500);
                        alphaAnimation.setFillAfter(true);
                        mLayout.startAnimation(alphaAnimation);
                        mLayout.setVisibility(View.GONE);
                        nflag = true;
                    }
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, DELAYED_TIME);
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //为了处理从不同的入口进入文件管理器,出现的类型错乱,如：从视频入口进入，按home键,再从图片进入,显示的还是视频类型
        if (!isPressback && !(MyApplication.mHomeActivityList.size() > 0)) {
            MyApplication.onFinishActivity();
        }
        stopAutoPlay();
        mAutoRunImageView.setImageResource(R.drawable.photo_info3);
    }

    private void startAutoPlay() {
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
            MainThread.cancel(mAutoRunnable);
            //修复OS-2665播放幻灯片时有破损图片文件管理器崩溃问题
            try {
                getScreenLock().release();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        toFlyView(0, 0, 0, 1, true, false, MENU_DURATION);
    }

    private void toShowView() {
        if (mShowing) return;
        if (mSizeType) {
            resetTvSize();
        } else {
            changeTvSize();
        }
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
        MainThread.runLater(mToHideRunnable, DELAYED_TIME);
        mediaimagebar.setVisibility(View.VISIBLE);
        toFlyView(0, 0, 1, 0, true, true, MENU_DURATION);
    }

    //改变提示状态
    private void resetTvSize() {
        if (mFullScreen) {
            mTvSize.setText(getString(R.string.image_full_screen));
        } else {
            mTvSize.setText(getString(R.string.image_real_size));
        }
    }

    private void toFlyView(float fromXValue, float toXValue, float fromYValue, float toYValue, boolean fillAfter, final Boolean status, long duration) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue, Animation.RELATIVE_TO_SELF, toYValue);
        animation.setDuration(duration);
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
        Log.i("onKeyDown",keyCode + "............");
        if (keyCode == VALUE) {
            isIN = true;
            dynamicList = new ArrayList<>();
            dynamicList.addAll(keyList);
        } else if (isIN) {
            String status = verify(keyCode);
            if (status.equals("break")) {
                isIN = false;
            } else if (status.equals("true")) {
                isIN = false;
                int photoModel = SharedPreferenceUtil.getPhotoModel();
                if (photoModel == 1) {
                    SharedPreferenceUtil.setPhotoModel(0);
                    Toast.makeText(this,"Close",Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferenceUtil.setPhotoModel(1);
                    Toast.makeText(this,"Open",Toast.LENGTH_LONG).show();
                }
            }
        }
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
            if (mLoadSuccessed) {
                if (mLoadReady) {
                    toShowView();
                }
            }
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
                if (mLoadReady) {
                    markRotation();
                    mImageBrowser.changeRotation();
                    mSizeType = false;
                    changeTvSize();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isFastClick()) {
                    return true;
                }
                if (mLoadReady) {
                    markRotation();
                    mImageBrowser.changeUpRotation();
                    mSizeType = false;
                    changeTvSize();
                }
                return true;
            }
        }
        if (mAutoPlay) {
            if (!mShowing) {
                if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    stopAutoPlay();
                    openVolume();
                    Toast.makeText(ImagePlayerActivity.this, getString(R.string.image_end_play), Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //初始提示状态
    private void changeTvSize() {
        if (mFullScreen) {
            mTvSize.setText(getString(R.string.image_real_size));
        } else {
            mTvSize.setText(getString(R.string.image_full_screen));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAutoPlay) {
            stopAutoPlay();
            openVolume();
            mMusic.setVisibility(View.GONE);
            mAnimationDrawable.stop();
        }
        if (mHandler != null) {
            mHandler.removeMessages(ARROW_SHOW);
            mHandler.removeMessages(MENU_SHOW);
        }
        if (null != mFrameView.mBitmap) {
            mFrameView.mBitmap = null;
        }
        EventBus.getDefault().unregister(this);
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

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public boolean isPressback;

    @Override
    public void onBackPressed() {
        isPressback = true;
        super.onBackPressed();
    }

    /**
     * 修复OS-795本地播放幻灯片，没有背景音乐，切换图片时会响应按键音。
     * 关闭按键音
     */
    public void closeVolume() {
        mImageBrowser.setSoundEffectsEnabled(false);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
    }

    /**
     * 打开按键音
     */
    public void openVolume() {
        mImageBrowser.setSoundEffectsEnabled(true);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mCurrentVolume, 0);
    }

    private String verify(int keyCode) {
        if (dynamicList.get(0) == keyCode) {
            dynamicList.remove(0);
            if (dynamicList.size() > 0) {
                return "continue";
            } else {
                return "true";
            }
        } else {
            return "break";
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbMounted(UsbMounted usbMounted) {
        if (usbMounted.mIsRemoved) {

            final List<Media> mediaList = new ArrayList<>();
            List<String> currPathList = MediaUtils.getCurrPathList();
            for (String path : currPathList) {
                File file = new File(path);
                Media fileInfo = FileUtil.getFileInfo(file, null, false);
                mediaList.add(fileInfo);
            }

            if (getData().size() > 0) {
                if (!getData().get(0).isSharing) {
                    boolean isClose = true; //是否关闭当前页面
                    for (int i = 0; i < mediaList.size(); i++) {
                        if (getData().get(0).mUri.contains(mediaList.get(i).mUri)) {
                            isClose = false;
                            break;
                        }
                    }
                    if (isClose) {
                        finish();
                    }
                }
            }
        }
    }
}