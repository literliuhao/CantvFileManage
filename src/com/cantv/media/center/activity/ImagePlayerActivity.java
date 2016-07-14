package com.cantv.media.center.activity;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.core.sys.MainThread;
import com.app.core.utils.UiUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.ui.ImageBrowser;
import com.cantv.media.center.ui.ImageFrameView;
import com.cantv.media.center.ui.ImageFrameView.NotifyParentUpdate;
import com.cantv.media.center.ui.MediaControllerBar;
import com.cantv.media.center.utils.MediaUtils;

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
    private TextView mtxtname;
    private TextView mtxtsize;
    private TextView mtxtresolution;
    private boolean nflag = true;
    private int screenWitdh;
    private int screenHeight;
    private Context mContext;
    private BroadcastReceiver mimageReceiver;
    private LinearLayout mediaimagebar;
    private Runnable mToHideRunnable;
    private boolean mShowing = false;
    private int ROTATION = 0;
    private int PREV = 1;
    private int NEXT = 2;
    private int AUTO = 3;
    private int INFO = 4;
    private int POSTION = 0;
    private FocusUtils mFocusUtils;
	private TextView mTvRotation;
	private TextView mTvSize;
	private TextView mTvAuto;
	private TextView mTvInfo;
	private ImageView mArrowLeft;
	private ImageView mArrowRight;
	private TextView mPosition;
	private TextView mTotal;
	private static final int DELAYED_TIME = 5*1000;
	private long mNextTime;
	private long mDurationTime;
	private boolean isFirst = true ;
	private boolean mSizeType = true;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int flag =msg.what;
			if(flag == 1){
				if(mArrowLeft.getVisibility() == View.GONE && mArrowRight.getVisibility() == View.GONE){
					return ;
				}
				mHandler.removeCallbacksAndMessages(null);
				mArrowLeft.setVisibility(View.GONE);
				mArrowRight.setVisibility(View.GONE);
			}
		};
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.media__image_view);
        mediaimagebar = (LinearLayout) findViewById(R.id.mediaimagebar);
        mLayout = (LinearLayout) findViewById(R.id.ly_imageinfo);
        mtxtname = (TextView) findViewById(R.id.txt_name);
        mtxtsize = (TextView) findViewById(R.id.txt_size);
        mArrowLeft = (ImageView) findViewById(R.id.media__image_view__left);
        mArrowRight = (ImageView) findViewById(R.id.media__image_view__right);
        mPosition = (TextView) findViewById(R.id.media__image_tv__position);
        mTotal = (TextView) findViewById(R.id.media__image_tv__total);
        mtxtresolution = (TextView) findViewById(R.id.txt_solution);
        mFrameView = new ImageFrameView(this);
        mFrameView.setNotifyParentUpdateListner(this);
        mImageBrowser = (ImageBrowser) findViewById(R.id.media__image_view__image);
        mImageBrowser.setContentImageView(mFrameView);
        mImageBrowser.setBackgroundColor(Color.BLACK);
        mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus);
        showImage(indexOfDefaultPlay(), null);
        initViewClickEvent();
        mCurImageIndex = indexOfDefaultPlay();
        mAutoRunnable = new Runnable() {
            public void run() {
                int offset = mCurImageIndex + 1;
                offset = (offset >= getData().size()) ? 0 : offset;
                showImage(offset, null);
                startAutoPlay();
            }
        };
        mToHideRunnable = new Runnable() {
            @Override
            public void run() {
                toHideView();
            }
        };
		//toShowView();
        mimageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("liujun22", "onReceive");
                if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    if (getData() == null || getData().size() == 0) {
                        return;
                    }
                    String sourcepath = getData().get(0);
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

    @Override
    protected MediaControllerBar getMediaControllerBar() {
        return null;
    }

    private void showImage(int index, Runnable onfinish) {
        List<String> data = getData();
        if (index < 0 || index >= data.size()) {
            return;
        }
        mCurImageIndex = index;
        int curIndex = index + 1;
        mPosition.setText(String.valueOf(curIndex));
        mTotal.setText(" / "+data.size());
        mFrameView.playImage(data.get(index), onfinish);
        UiUtils.runAfterLayout(mImageBrowser, new Runnable() {
            @Override
            public void run() {
                mImageBrowser.reset();
                UiUtils.fadeView(mImageBrowser, 0, 1, UiUtils.ANIM_DURATION_LONG_LONG * 0, false, null);
            }
        });
        String curFileUri = getData().get(mCurImageIndex);
        mtxtname.setText(new File(curFileUri).getName());
        mtxtsize.setText("文件大小：" + MediaUtils.fileLength(new File(curFileUri).length()));
        
        if (!mAutoPlay){
        if(index == 0 && data.size() > 1){
        	//显示右面
        	mArrowRight.setVisibility(View.VISIBLE);
        	mArrowLeft.setVisibility(View.GONE);
        }else if(index == data.size() - 1 && data.size() > 1){
        	//显示左面
        	mArrowLeft.setVisibility(View.VISIBLE);
        	mArrowRight.setVisibility(View.GONE);
        }else if(index > 0 && data.size() > 1 && index < data.size() - 1){
        	mArrowLeft.setVisibility(View.VISIBLE);
        	mArrowRight.setVisibility(View.VISIBLE);
        }else{
        	mArrowLeft.setVisibility(View.GONE);
        	mArrowRight.setVisibility(View.GONE);
        	}
        new Thread(new Runnable() {
			
			@Override
			public void run() {
					mHandler.removeCallbacksAndMessages(null);
					mHandler.sendEmptyMessageDelayed(1, DELAYED_TIME);
			}
		}).start();
        }
    }

    private void initViewClickEvent() {
    	mTvRotation = (TextView) findViewById(R.id.media__image_tv__rotation);
    	mTvSize = (TextView) findViewById(R.id.media__image_tv__size);
    	mTvAuto = (TextView) findViewById(R.id.media__image_tv__auto);
    	mTvInfo = (TextView) findViewById(R.id.media__image_tv__info);
    	mRotation = (ImageView) findViewById(R.id.media__image_view__rotation);
    	mSize = (ImageView) findViewById(R.id.media__image_view__size);
    	mAutoRunImageView = (ImageView) findViewById(R.id.media__image_view__auto);
    	mInfo = (ImageView) findViewById(R.id.media__image_view__info);
    	
    	mRotation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
                mImageBrowser.changeRotation();
            }
        });
    	mRotation.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    POSTION = 0;
                    mFocusUtils.startMoveFocus(v, true, (float) 0.9);
                    translateDown(mTvRotation);
                }else{
                	translateUp(mTvRotation);
                }
                
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }

        });

    	mSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "缩放", Toast.LENGTH_SHORT).show();
                if(mSizeType){
                	mSizeType = false;
                	//mImageBrowser.sets
                	
                	ImageView imageView = new ImageView(getApplicationContext());
                	imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                	 
                }
            }
        });
    	mSize.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if (hasFocus) {
                    POSTION = 1;
                    mFocusUtils.startMoveFocus(v, true, (float) 0.9);
                    translateDown(mTvSize);
                }else{
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
                if (mAutoPlay) {
                    stopAutoPlay();
                    Toast.makeText(ImagePlayerActivity.this, "结束幻灯片播放", Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                } else {
                    startAutoPlay();
                    Toast.makeText(ImagePlayerActivity.this, "开始幻灯片播放", Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.play_stop);
                }
            }
        });
        mAutoRunImageView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if (hasFocus) {
                    POSTION = 2;
                    mFocusUtils.startMoveFocus(v, true, (float) 0.9);
                    translateDown(mTvAuto);
                }else{
                	translateUp(mTvAuto);
                }
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
            }
        });

        mInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainThread.cancel(mToHideRunnable);
                MainThread.runLater(mToHideRunnable, 5 * 1000);
                if (nflag) {
                    mLayout.setVisibility(View.VISIBLE);
                    // 初始化
                    Animation translateAnimation = new TranslateAnimation(0.1f, 0.1f, 100.0f, 0.1f);
                    // 设置动画时间
                    translateAnimation.setDuration(300);
                    mLayout.startAnimation(translateAnimation);
                    nflag = false;
                } else {
                    Animation translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, 100.0f);
                    // 设置动画时间
                    translateAnimation.setDuration(300);
                    mLayout.startAnimation(translateAnimation);
                    mLayout.setVisibility(View.GONE);
                    nflag = true;
                }
                String curFileUri = getData().get(mCurImageIndex);
                mtxtname.setText(new File(curFileUri).getName());
                mtxtsize.setText("文件大小：" + MediaUtils.fileLength(new File(curFileUri).length()));

            }
        });
        mInfo.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if (hasFocus) {
                    POSTION = 3;
                    mFocusUtils.startMoveFocus(v, true, (float) 0.9);
                    translateDown(mTvInfo);
                }else{
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
        mRotation.setFocusable(true);
        mRotation.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mRotation.requestFocus();
			}
		}, 345);
        
    }

    private PowerManager.WakeLock getScreenLock() {
        if (mScreenLock == null) {
            mScreenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");
        }
        return mScreenLock;
    }

    private void startAutoPlay() {
    	int curIndex = mCurImageIndex + 1;
    	int size = getData().size();
    	
    	if(curIndex == size){
    		stopAutoPlay();
    		Toast.makeText(getApplicationContext(), "已经是最后一张", Toast.LENGTH_LONG).show();
    		return ;
    	}
        if (mAutoPlay == false) {
            mAutoPlay = true;
            getScreenLock().acquire();
            Log.i("", "Hua...getScreenLock().acquire();");
        }
        MainThread.runLater(mAutoRunnable, 5000);
    }

    private void stopAutoPlay() {
        if (mAutoPlay) {
            mAutoPlay = false;
            MainThread.cancel(mAutoRunnable);
            getScreenLock().release();
            Log.i("", "Hua...getScreenLock().release();");
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
        mFocusUtils.hideFocus();
        mediaimagebar.setVisibility(View.GONE);
        MainThread.cancel(mToHideRunnable);
        toFlyView(0, 0, 0, 1, true);
    }

    private void toShowView() {
        if (mShowing)
            return;
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
        mFocusUtils.showFocus();
        mediaimagebar.setVisibility(View.VISIBLE);
        toFlyView(0, 0, 1, 0, true);

    }

    private void toFlyView(float fromXValue, float toXValue, float fromYValue, float toYValue, boolean fillAfter) {
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF, toXValue, Animation.RELATIVE_TO_SELF, fromYValue,
                Animation.RELATIVE_TO_SELF, toYValue);
        animation.setDuration(UiUtils.ANIM_DURATION_LONG);
        animation.setFillAfter(fillAfter);
        mediaimagebar.clearAnimation();
        mediaimagebar.startAnimation(animation);
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
                int offset = mCurImageIndex - 1;
                offset = (offset < 0) ? getData().size() - 1 : offset;
                showImage(offset, null);
                return true;

            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                int offset = mCurImageIndex + 1;
                offset = (offset >= getData().size()) ? 0 : offset;
                showImage(offset, null);
                return true;

            }
        }
        
        if(mAutoPlay){
        	if(!mShowing){
        	 if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        		 stopAutoPlay();
        		 Toast.makeText(ImagePlayerActivity.this, "结束幻灯片播放", Toast.LENGTH_SHORT).show();
                 mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                 return true;
        	 	}
        	 }
        }
        
        if (keyCode == event.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!mShowing) {
                if (mAutoPlay) {
                    stopAutoPlay();
                    Toast.makeText(ImagePlayerActivity.this, "结束幻灯片播放", Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.photo_info3);
                } else {
                    startAutoPlay();
                    Toast.makeText(ImagePlayerActivity.this, "开始幻灯片播放", Toast.LENGTH_SHORT).show();
                    mAutoRunImageView.setImageResource(R.drawable.play_stop);
                }

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAutoPlay) {
            stopAutoPlay();
        }
        unregisterReceiver(mimageReceiver);
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
		Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.7f);
		translateAnimation.setDuration(200);
		translateAnimation.setFillAfter(true);
		view.clearAnimation();
		view.startAnimation(translateAnimation);
	}
	
	private void translateUp(View view) {
		Animation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0);
		translateAnimation.setDuration(200);
		translateAnimation.setFillAfter(true);
		view.clearAnimation();
		view.startAnimation(translateAnimation);
	}
}
