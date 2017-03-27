package com.cantv.media.center.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.core.sys.MainThread;
import com.app.core.utils.UiUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.Listener.OnLoadingImageListener;
import com.cantv.media.center.adapter.PhotoPagerAdapter;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.ImageBean;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.ui.dialog.LoadingDialog;
import com.cantv.media.center.ui.image.ImageBrowser;
import com.cantv.media.center.ui.image.ImageViewPager;
import com.cantv.media.center.ui.player.MediaControllerBar;
import com.cantv.media.center.ui.player.YFocusTextView;
import com.cantv.media.center.utils.DateUtil;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.StatisticsUtil;
import com.cantv.media.center.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 图片浏览界面
 * Created by Shenpx on 2017/2/24 0015.
 */

public class ImageActivity extends MediaPlayerActivity implements ViewPager.OnPageChangeListener, View.OnFocusChangeListener, OnLoadingImageListener, View.OnClickListener {

    private static final String TAG = "ImageActivity";
    private static final int DELAYED_TIME = 5 * 1000;
    private static final int ARROW_SHOW = 1;
    private static final int MENU_SHOW = 2;
    @Bind(R.id.image_viewPager)
    ImageViewPager mImageViewPager;
    @Bind(R.id.iv_rotation)
    ImageView mRotation;
    @Bind(R.id.tv_rotation)
    TextView tvRotation;
    @Bind(R.id.ll_rotation)
    LinearLayout llRotation;
    @Bind(R.id.iv_size)
    ImageView mSize;
    @Bind(R.id.tv_size)
    TextView tvSize;
    @Bind(R.id.ll_size)
    LinearLayout llSize;
    @Bind(R.id.iv_auto)
    ImageView mAuto;
    @Bind(R.id.tv_auto)
    TextView tvAuto;
    @Bind(R.id.ll_auto)
    LinearLayout llAuto;
    @Bind(R.id.iv_info)
    ImageView mInfo;
    @Bind(R.id.tv_info)
    TextView tvInfo;
    @Bind(R.id.ll_info)
    LinearLayout mLlInfo;
    @Bind(R.id.mediaimagebar)
    LinearLayout mMediaImageBar;//菜单栏
    @Bind(R.id.iv_leftarrow)
    ImageView mLeftArrow;
    @Bind(R.id.iv_rightarrow)
    ImageView mRightArrow;
    @Bind(R.id.tv_total)
    TextView mTotal;
    @Bind(R.id.tv_current)
    TextView mCurrent;
    @Bind(R.id.info_name)
    YFocusTextView mInfoName;
    @Bind(R.id.info_size)
    TextView mInfoSize;
    @Bind(R.id.info_url)
    TextView mInfoUrl;
    @Bind(R.id.info_time)
    TextView mInfoTime;
    @Bind(R.id.ll_imageinfo)
    LinearLayout mImageInfo;//图片信息
    @Bind(R.id.title_image)
    ImageView titleImage;
    @Bind(R.id.tv_menutitle)
    TextView mMenuTitle;
    @Bind(R.id.image_header)
    RelativeLayout imageHeader;//左上角提示
    @Bind(R.id.tv_loadingfail)
    TextView mLoadingFail;
    private PhotoPagerAdapter mPhotoPagerAdapter;
    public float screenWidth;
    public float screenHeight;
    private LoadingDialog mLoadingDialog;
    private Button mFd;
    private Button mSx;
    private Button mXz;
    private ImageBrowser mImageBrowser;
    private boolean mShowing;
    private int MENU_DURATION = 500;
    private long moveTime;
    private FocusUtils mFocusUtils;
    private int POSITION = 5;
    private boolean isFirstFocus = true;
    private boolean isRotation = false;
    private boolean isFirstMenu = true;
    private boolean mFullScreen;
    private static long lastClickTime;
    private boolean mAutoPlay;
    private boolean mSizeType;//是否已缩放
    private int mImageWidth;
    private int mImageHeight;
    private boolean nFlag = true;
    private View mCurrentPagerView;
    public int mCurrentPosition;
    private Runnable mToHideRunnable;
    private Runnable mAutoRunnable;
    private int mAutoCurrentPosition;
    private int mKeyTone;
    private boolean isAutoPlay;//是否在开启幻灯片
    private boolean mLoadedSucceed;//加载图片成功
    private boolean mLoadedFailed;//图片加载失败
    private boolean mLoadedThumbnailSucceed;//加载当前缩略图成功
    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            int flag = msg.what;
            if (flag == ARROW_SHOW) {
                if (mLeftArrow.getVisibility() == View.GONE && mRightArrow.getVisibility() == View.GONE) {
                    return;
                }
                mHandler.removeMessages(ARROW_SHOW);
                mLeftArrow.setVisibility(View.GONE);
                mRightArrow.setVisibility(View.GONE);
            } else if (flag == MENU_SHOW) {
                if (imageHeader.getVisibility() == View.GONE) {
                    return;
                }
                mHandler.removeMessages(MENU_SHOW);
                imageHeader.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__image);
        ButterKnife.bind(this);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        EventBus.getDefault().register(this);
        mLoadingDialog = new LoadingDialog(this);
        showProgressBar();
        initView();
        initData();
        initListener();
        autoRunnable();
        toHideRunnable();
        getKeyToneSetting();
        StatisticsUtil.customEvent(ImageActivity.this, "picture_player");
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticsUtil.registerResume(this);
        if (isAutoPlay) {
            startAutoPlayImage();
        }

        if (mDataList.size() < 1) {
            String mediaPath = SharedPreferenceUtil.getMediaPath();
            mediaPath = mediaPath.subSequence(0, mediaPath.lastIndexOf("/")).toString();
            FileUtil.getFileList(mediaPath, false, new FileUtil.OnFileListListener() {
                @Override
                public void findFileListFinish(List<Media> list) {
                    List<Media> fileList = list;

                    FileUtil.sortList(fileList, FileComparator.SORT_TYPE_DEFAULT, true);
                    if (fileList.size() > 0) {
                        mDataList.clear();
                        mDataList.addAll(fileList);
                    }
                    for (int i = 0; i < fileList.size(); i++) {
                        String path = fileList.get(i).isSharing ? fileList.get(i).sharePath : fileList.get(i).mUri;
                        if (SharedPreferenceUtil.getMediaPath().equals(path)) {
                            mDefaultPlayIndex = i;
                            break;
                        }
                    }
                    if (mDataList.size() > 0) {
                        mImageViewPager.setCurrentItem(mDefaultPlayIndex);
                        mPhotoPagerAdapter.notifyDataSetChanged();
                    }
                }
            }, SourceType.PICTURE);
        }
    }

    private void initView() {
        mPhotoPagerAdapter = new PhotoPagerAdapter(this, mDataList);
        mPhotoPagerAdapter.setOnLoadingImageListener(this);
        mImageViewPager.setAdapter(mPhotoPagerAdapter);
        mImageViewPager.setCurrentItem(mDefaultPlayIndex);
        mImageViewPager.setOnPageChangeListener(this);
        mImageViewPager.setOnFocusChangeListener(this);
        mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus);
    }

    private void initData() {
        mCurrentPosition = mDefaultPlayIndex;
    }

    private void initListener() {
        mRotation.setOnClickListener(this);
        mSize.setOnClickListener(this);
        mAuto.setOnClickListener(this);
        mInfo.setOnClickListener(this);

        mRotation.setOnFocusChangeListener(this);
        mSize.setOnFocusChangeListener(this);
        mAuto.setOnFocusChangeListener(this);
        mInfo.setOnFocusChangeListener(this);
    }

    private void autoRunnable() {
        mAutoRunnable = new Runnable() {
            public void run() {
                mAutoCurrentPosition = mCurrentPosition + 1;
                hideView();
                Log.i(TAG, "run: " + mAutoCurrentPosition);
                if (mAutoCurrentPosition == mDataList.size()) {
                    mAutoCurrentPosition = 0;
                    mImageViewPager.setCurrentItem(mAutoCurrentPosition, false);
                } else {
                    mImageViewPager.setCurrentItem(mAutoCurrentPosition);
                }
                startAutoPlay();
            }
        };
    }

    private void hideView() {
        imageHeader.setVisibility(View.INVISIBLE);
        mLeftArrow.setVisibility(View.GONE);
        mRightArrow.setVisibility(View.GONE);
        if (mDataList.size() != 1 && mDataList.size() > 0) {
            mCurrent.setText("");
            mTotal.setText("");
        }
        if (mShowing) {
            MENU_DURATION = 0;
            toHideView();
            MENU_DURATION = 500;
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

    //==================================================================================
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_rotation:
                rotationImage();
                break;
            case R.id.iv_size:
                String mName = getData().get(mCurrentPosition).mName;
                getImageSize();
                if (mName.endsWith(".gif")) {
                    scaleImage();
                } else if (mImageWidth > screenWidth || mImageHeight > screenHeight) {
                    showProgressBar();
                    openLargeImageActivity();
                } else {
                    scaleImage();
                }
                break;
            case R.id.iv_auto:
                autoPlayImage();
                break;
            case R.id.iv_info:
                if (mAutoPlay) {
                    return;
                }
                AutoHideMenu();
                showImageInfoAnimation();
                showImageInfo();
                break;
            default:
                break;
        }
    }

    private void openLargeImageActivity() {
        Intent intent = new Intent();
        intent.setAction("com.cantv.action.LARGE_ACTIVITY");
        intent.putExtra("path", mDataList.get(mCurrentPosition).mUri);
        ImageActivity.this.startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dismissProgressBar();
        toHideView();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.iv_rotation:
                moveFocus(v, hasFocus, tvRotation, 0);
                AutoHideMenu();
                break;
            case R.id.iv_size:
                moveFocus(v, hasFocus, tvSize, 1);
                AutoHideMenu();
                break;
            case R.id.iv_auto:
                moveFocus(v, hasFocus, tvAuto, 2);
                AutoHideMenu();
                break;
            case R.id.iv_info:
                if (hasFocus) {
                    POSITION = 3;
                    mFocusUtils.startMoveFocus(v, true, (float) 1.2);
                    translateDown(tvInfo);
                } else {
                    translateUp(tvInfo);
                    if (mImageInfo.getVisibility() == View.VISIBLE) {
                        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
                        alphaAnimation.setDuration(500);
                        alphaAnimation.setFillAfter(true);
                        mImageInfo.startAnimation(alphaAnimation);
                        mImageInfo.setVisibility(View.GONE);
                        nFlag = true;
                    }
                }
                AutoHideMenu();
                break;
            default:
                break;
        }
    }

    /**
     * 焦点移动
     *
     * @param v
     * @param hasFocus
     * @param moveFocus
     * @param position
     */
    private void moveFocus(View v, boolean hasFocus, View moveFocus, int position) {
        if (hasFocus) {
            POSITION = position;
            mFocusUtils.startMoveFocus(v, true, (float) 1.2);
            translateDown(moveFocus);
        } else {
            translateUp(moveFocus);
        }
    }

    //setOnPageChangeListener
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.i(TAG, "onPageScrolled: " + position);
    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected: " + position);
        if (mAutoPlay) {
            stopAutoPlay();
            startAutoPlay();
        }
        isRotation = false;
        mSizeType = false;
        dismissProgressBar();
        hideView();
        boolean lastPage = mCurrentPosition == 0 && position == mDataList.size() - 1;
        boolean firstPage = position == 0 && mCurrentPosition == mDataList.size() - 1;
        if (!firstPage && !lastPage) {
            setImageLayoutReset();
        }
        mLoadedSucceed = false;
        mLoadedFailed = false;
        mLoadedThumbnailSucceed = false;
        List<ImageBean> mImageList = mPhotoPagerAdapter.mImageList;
        for (int i = 0; i < mImageList.size(); i++) {
            if (position == mImageList.get(i).getPosition()) {
                if (!mImageList.get(i).getLoadingSucceed()) {
                    mLoadedFailed = true;
                }
                mFullScreen = mImageList.get(i).getFullScreen();
                mLoadedSucceed = true;
                break;
            } else {
                mLoadedSucceed = false;
            }
        }
        if (!mLoadedSucceed) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mLoadedThumbnailSucceed) {
                        showProgressBar();
                    }
                }
            }, 100);
        } else {
            getViewpagerView();
            showArrow(position);
            showPagerHint(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("onKeyDown", keyCode + "............");
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
            if (mLoadedSucceed) {
                toShowView();
            }
            return true;
        }
        if (!mAutoPlay) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (isFastClick()) {
                    return true;
                }
                if (!mLoadedFailed && mLoadedSucceed) {
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
                if (!mLoadedFailed && mLoadedSucceed) {
                    markRotation();
                    mImageBrowser.changeUpRotation();
                    mSizeType = false;
                    changeTvSize();
                }
                return true;
            }
        }
        if (!mShowing) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (mCurrentPosition == 0) {
                    mImageViewPager.setCurrentItem(mDataList.size() - 1, false);
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (mCurrentPosition == mDataList.size() - 1) {
                    mImageViewPager.setCurrentItem(0, false);
                }
                return true;
            }
        }
        if (mAutoPlay) {
            if (!mShowing) {
                if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    stopAutoPlayImage();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //==================================================================================
    //setOnLoadingImageListener
    @Override
    public void loadingImageSuccess(boolean loadSuccess, int position) {
        Log.i(TAG, "loadingImageSuccess: " + position);
        mCurrentPosition = mPhotoPagerAdapter.getCurrentPosition();
        if (position == mCurrentPosition) {
            mLoadedThumbnailSucceed = true;
            dismissProgressBar();
            if (!loadSuccess) {
                if (isFirstMenu) {
                    isFirstMenu = false;
                    imageHeader.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(MENU_SHOW, DELAYED_TIME);
                }
                getViewpagerView();
                showArrow(mCurrentPosition);
                showPagerHint(position);
                mLoadedSucceed = true;
            }
        }
    }

    @Override
    public void loadingImageFail(boolean loadFail, int position) {
        Log.i(TAG, "loadingImageFail: " + position);
    }

    @Override
    public void isScaled(boolean isScaled, int position) {
        Log.i(TAG, "isScaled: ");
    }

    @Override
    public void loadingImageReady(boolean isLoadReady, int position) {
        Log.i(TAG, "loadingImageReady: " + position);
        if (isLoadReady) {
            if (mCurrentPosition == position) {
                if (isFirstMenu) {
                    isFirstMenu = false;
                    imageHeader.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(MENU_SHOW, DELAYED_TIME);
                }
                getViewpagerView();
                showArrow(mCurrentPosition);
                showPagerHint(position);
                mLoadedSucceed = true;
            }
        }
    }

    @Override
    public void getImageSize(int width, int height, boolean isScaled, int position) {
        Log.i(TAG, "getImageSize: " + width + " * " + height);
        if (mCurrentPosition == position) {
            mFullScreen = isScaled;
        }
    }

    @Override
    protected MediaControllerBar getMediaControllerBar() {
        return null;

    }

    private void getKeyToneSetting() {
        mKeyTone = Settings.System.getInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
        Log.i(TAG, "getKeyToneSetting: " + mKeyTone);
    }

    //==================================================================================
    //显示菜单
    private void toShowView() {
        if (mShowing) {
            return;
        }
        if (mSizeType) {
            resetTvSize();
        } else {
            changeTvSize();
        }
        switch (POSITION) {
            case 0:
                mRotation.requestFocus();
                break;
            case 1:
                mSize.requestFocus();
                break;
            case 2:
                mAuto.requestFocus();
                break;
            case 3:
                mInfo.requestFocus();
                break;
            default:
                break;
        }
        mShowing = true;
        mImageViewPager.setFocusable(false);
        MainThread.runLater(mToHideRunnable, DELAYED_TIME);
        mMediaImageBar.setVisibility(View.VISIBLE);
        toFlyView(0, 0, 1, 0, true, true, MENU_DURATION);
    }

    private void toFlyView(float fromXValue, float toXValue, float fromYValue, float toYValue, boolean fillAfter, final Boolean status, long duration) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue, Animation.RELATIVE_TO_SELF, toYValue);
        animation.setDuration(duration);
        animation.setFillAfter(fillAfter);
        mMediaImageBar.clearAnimation();
        mMediaImageBar.startAnimation(animation);
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

    //循环播放幻灯片
    private void startAutoPlay() {
        if (mAutoPlay == false) {
            mAutoPlay = true;
        }
        MainThread.runLater(mAutoRunnable, DELAYED_TIME);
    }

    private void stopAutoPlay() {
        if (mAutoPlay) {
            mAutoPlay = false;
            MainThread.cancel(mAutoRunnable);
        }
    }

    //隐藏菜单
    private void AutoHideMenu() {
        MainThread.cancel(mToHideRunnable);
        MainThread.runLater(mToHideRunnable, DELAYED_TIME);
    }

    //获取viewpager控件
    private void getViewpagerView() {
        mCurrentPagerView = mPhotoPagerAdapter.getCurrentPagerView();
        mImageBrowser = (ImageBrowser) mCurrentPagerView.findViewById(R.id.iv_image);
        mCurrentPosition = mPhotoPagerAdapter.getCurrentPosition();
    }

    //显示箭头数字
    private void showArrow(int position) {
        mCurrent.setText(String.valueOf(position % mDataList.size() + 1));
        mTotal.setText(" / " + mDataList.size());
        arrowShow();
    }

    //显示第一张，最后一张提示
    private void showPagerHint(int position) {
        if (!mAutoPlay) {
            ToastUtils.cancelCurrentToast();
            if (mDataList.size() == 1) {
                ToastUtils.showMessage(ImageActivity.this, getString(R.string.image_start_photo));
                return;
            }
            if (position == mDataList.size() - 1 && position != 1) {
                ToastUtils.showMessage(ImageActivity.this, getString(R.string.image_last_photo));
            } else if (position == 0 && mDataList.size() > 1) {
                ToastUtils.showMessage(ImageActivity.this, getString(R.string.image_start_photo));
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
        mImageViewPager.setFocusable(true);
        mMediaImageBar.setVisibility(View.GONE);
        MainThread.cancel(mToHideRunnable);
        mFocusUtils.hideFocus();
        toFlyView(0, 0, 0, 1, true, false, MENU_DURATION);
    }

    private void arrowShow() {
        if (!mAutoPlay) {
            if (mCurrentPosition == 0 && mDataList.size() > 1) {
                mRightArrow.setVisibility(View.VISIBLE);
                mLeftArrow.setVisibility(View.GONE);
            } else if (mCurrentPosition == mDataList.size() - 1 && mDataList.size() > 1) {
                mLeftArrow.setVisibility(View.VISIBLE);
                mRightArrow.setVisibility(View.GONE);
            } else if (mCurrentPosition > 0 && mDataList.size() > 1 && mCurrentPosition < mDataList.size() - 1) {
                mLeftArrow.setVisibility(View.VISIBLE);
                mRightArrow.setVisibility(View.VISIBLE);
            } else {
                mLeftArrow.setVisibility(View.GONE);
                mRightArrow.setVisibility(View.GONE);
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

    //初始提示状态
    private void changeTvSize() {
        if (mFullScreen) {
            tvSize.setText(getString(R.string.image_real_size));
        } else {
            tvSize.setText(getString(R.string.image_full_screen));
        }
    }

    //改变提示状态
    private void resetTvSize() {
        if (mFullScreen) {
            tvSize.setText(getString(R.string.image_full_screen));
        } else {
            tvSize.setText(getString(R.string.image_real_size));
        }
    }

    //旋转图片
    private void rotationImage() {
        if (isFastClick() || mAutoPlay) {
            return;
        }
        AutoHideMenu();
        markRotation();
        mImageBrowser.changeRotation();
        mSizeType = false;
        changeTvSize();
    }

    //旋转相关
    private void markRotation() {
        isRotation = !isRotation;
    }

    //点击缩放
    private void scaleImage() {
        if (isFastClick() || mAutoPlay) {
            return;
        }
        AutoHideMenu();
        if (!mSizeType) {
            resetTvSize();
        } else {
            changeTvSize();
        }
        float calc = calcByWH(mImageWidth, mImageHeight, mSizeType);
        Log.i("ImagePlayerActivity", "calc " + calc);
        mImageBrowser.onZoomScale(calc);
    }

    //获取图片实际宽高
    private void getImageSize() {
        List<ImageBean> mImageList = mPhotoPagerAdapter.mImageList;
        for (int i = 0; i < mImageList.size(); i++) {
            ImageBean imageBean = mImageList.get(i);
            int position = imageBean.getPosition();
            if (position == mCurrentPosition) {
                mImageWidth = imageBean.getWidth();
                mImageHeight = imageBean.getHeight();
                break;
            }
        }
    }

    //播放幻灯片
    private void autoPlayImage() {
        AutoHideMenu();
        Log.i(TAG, "onClick: " + mCurrentPosition);
        if (mAutoPlay) {
            stopAutoPlayImage();
        } else {
            startAutoPlayImage();
            StatisticsUtil.customEvent(ImageActivity.this, "slide_player");
        }
    }

    private void startAutoPlayImage() {
        closeVolume();
        startAutoPlay();
        ToastUtils.showMessage(ImageActivity.this, getString(R.string.image_start_play));
        mAuto.setImageResource(R.drawable.photo_info33);
        changeMenuImageColor(true);
    }

    //修改菜单按钮颜色，属性
    private void changeMenuImageColor(boolean isAutoPlay) {
        mRotation.setAlpha(isAutoPlay ? 0.5f : 1.0f);
        mSize.setAlpha(isAutoPlay ? 0.5f : 1.0f);
        mInfo.setAlpha(isAutoPlay ? 0.5f : 1.0f);
        tvRotation.setAlpha(isAutoPlay ? 0.5f : 1.0f);
        tvSize.setAlpha(isAutoPlay ? 0.5f : 1.0f);
        tvInfo.setAlpha(isAutoPlay ? 0.5f : 1.0f);
        mRotation.setClickable(isAutoPlay ? false : true);
        mSize.setClickable(isAutoPlay ? false : true);
        mInfo.setClickable(isAutoPlay ? false : true);
    }

    private void stopAutoPlayImage() {
        stopAutoPlay();
        openVolume();
        ToastUtils.showMessage(ImageActivity.this, getString(R.string.image_end_play));
        mAuto.setImageResource(R.drawable.photo_info3);
        changeMenuImageColor(false);
    }

    //图片信息显示
    private void showImageInfoAnimation() {
        if (nFlag) {
            mImageInfo.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1.0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setFillAfter(true);
            mImageInfo.startAnimation(alphaAnimation);
            nFlag = false;
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
            alphaAnimation.setDuration(500);
            alphaAnimation.setFillAfter(true);
            mImageInfo.startAnimation(alphaAnimation);
            mImageInfo.setVisibility(View.GONE);
            nFlag = true;
        }
    }

    private void showImageInfo() {
        getImageSize();
        mInfoName.setText(getString(R.string.image_name) + "：" + getData().get(mCurrentPosition).mName);
        mInfoSize.setText(getString(R.string.image_volume) + "：" + FileUtil.convertStorage(getData().get(mCurrentPosition).fileSize));
        mInfoUrl.setText(getString(R.string.image_size) + "：" + mImageWidth + "*" + mImageHeight);
        mInfoTime.setText(getString(R.string.image_time) + "：" + DateUtil.onDate2String(new Date(getData().get(mCurrentPosition).modifiedDate), "yyyy-MM-dd HH:mm"));
    }

    //防止快速点击
    private synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
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
            if(width == 0 || height == 0){
                return 1.0f;
            }
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
            if(width == 0 || height == 0){
                return 1.0f;
            }
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
                } else {
                    return screenHeight / currentH;
                }
            } else {
                //屏幕大于图片宽高时
                return 1.0f;
            }
        }
    }

    //菜单选项移动动画
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

    private void showProgressBar() {
        if (mLoadingDialog.isShowing()) {
            return;
        }
        mLoadingDialog.show();
    }

    private void dismissProgressBar() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    //重置布局
    private void setImageLayoutReset() {
        UiUtils.runAfterLayout(mImageBrowser, new Runnable() {
            @Override
            public void run() {
                mImageBrowser.reset();
                mImageBrowser.changeReset();
                UiUtils.fadeView(mImageBrowser, 0, 1, UiUtils.ANIM_DURATION_LONG_LONG * 0, false, null);
            }
        });
    }

    /**
     * 关闭按键音
     * 0关闭，1开启
     */
    private void closeVolume() {
        boolean isClose = Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
        int keyTone = Settings.System.getInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
        Log.i(TAG, isClose ? "closeVolume: " + keyTone : "");
    }

    //打开按键音
    private void openVolume() {
        if (mKeyTone == 1) {
            boolean isOpen = Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
            int keyTone = Settings.System.getInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
            Log.i(TAG, isOpen ? "openVolume: " + keyTone : "");
        }
    }

    //==================================================================================
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

    @Override
    protected void onPause() {
        StatisticsUtil.registerPause(this);
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        isAutoPlay = mAutoPlay;
        if(mAutoPlay){
            stopAutoPlayImage();
        }
        //保存当前播放的路径
        String path = mDataList.get(mCurrentPosition).isSharing ? mDataList.get(mCurrentPosition).sharePath : mDataList.get(mCurrentPosition).mUri;
        SharedPreferenceUtil.saveMediaPath(path);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAutoPlay) {
            stopAutoPlay();
            openVolume();
        }
        dismissProgressBar();
        EventBus.getDefault().unregister(this);
    }
}
