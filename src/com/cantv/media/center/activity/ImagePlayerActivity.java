package com.cantv.media.center.activity;

import java.io.File;
import java.util.List;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.media__image_view);
		mediaimagebar = (LinearLayout) findViewById(R.id.mediaimagebar);
		mLayout = (LinearLayout) findViewById(R.id.ly_imageinfo);
		mtxtname = (TextView) findViewById(R.id.txt_name);
		mtxtsize = (TextView) findViewById(R.id.txt_size);
		mtxtresolution = (TextView) findViewById(R.id.txt_solution);
		mFrameView = new ImageFrameView(this);
		mFrameView.setNotifyParentUpdateListner(this);
		mImageBrowser = (ImageBrowser) findViewById(R.id.media__image_view__image);
		mImageBrowser.setContentImageView(mFrameView);
		mImageBrowser.setBackgroundColor(Color.BLACK);
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
		toShowView();
		mimageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("liujun22", "onReceive");
				if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
					// Intent temintent = new Intent();
					// temintent.setClass(ImagePlayerActivity.this,
					// IndexActivity.class);
					// ImagePlayerActivity.this.startActivity(temintent);
					if (getData() == null || getData().size() == 0) {
						return;
					}
					String sourcepath = getData().get(0);
					String targetpath = intent.getDataString();
					boolean isequal = MediaUtils.isEqualDevices(sourcepath, targetpath);
					// Intent temintent = new Intent();
					// temintent.setClass(VideoPlayerActivity.this,
					// IndexActivity.class);
					// VideoPlayerActivity.this.startActivity(temintent);
					if (isequal) {
						ImagePlayerActivity.this.finish();
					}
				}
			}
		};
		IntentFilter usbFilter = new IntentFilter();
		usbFilter.setPriority(1000);
		// usbFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		// usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
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
	}

	private void initViewClickEvent() {
		findViewById(R.id.media__image_view__rotation).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
				mImageBrowser.changeRotation();
			}
		});
		findViewById(R.id.media__image_view__rotation).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					POSTION = 0;
				}
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
			}
		});
		findViewById(R.id.media__image_view__prev).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
				int offset = mCurImageIndex - 1;
				offset = (offset < 0) ? getData().size() - 1 : offset;
				showImage(offset, null);
			}
		});
		findViewById(R.id.media__image_view__prev).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					POSTION = 1;
				}
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
			}
		});
		findViewById(R.id.media__image_view__next).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("liujun001", "onClick--------");
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
				int offset = mCurImageIndex + 1;
				offset = (offset >= getData().size()) ? 0 : offset;
				showImage(offset, null);
			}
		});
		findViewById(R.id.media__image_view__next).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					POSTION = 2;
				}
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
			}
		});
		findViewById(R.id.media__image_view__info).setOnClickListener(new OnClickListener() {
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
		findViewById(R.id.media__image_view__info).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					POSTION = 4;
				}
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
				if (!hasFocus) {
					if (mLayout.getVisibility() == View.VISIBLE) {
						Animation translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, 100.0f);
						// 设置动画时间
						translateAnimation.setDuration(300);
						mLayout.startAnimation(translateAnimation);
						mLayout.setVisibility(View.GONE);
						nflag = true;
					}
				}

			}
		});
		mAutoRunImageView = (ImageView) findViewById(R.id.media__image_view__auto);
		mAutoRunImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
				if (mAutoPlay) {
					stopAutoPlay();
					Toast.makeText(ImagePlayerActivity.this, "结束幻灯片播放", Toast.LENGTH_SHORT).show();
					mAutoRunImageView.setImageResource(R.drawable.general__share__auto);
					findViewById(R.id.media__image_view__prev).setFocusable(true);
					findViewById(R.id.media__image_view__next).setFocusable(true);
				} else {
					startAutoPlay();
					Toast.makeText(ImagePlayerActivity.this, "开始幻灯片播放", Toast.LENGTH_SHORT).show();
					mAutoRunImageView.setImageResource(R.drawable.media__image__pause);
					findViewById(R.id.media__image_view__prev).setFocusable(false);
					findViewById(R.id.media__image_view__next).setFocusable(false);
				}
			}
		});
		mAutoRunImageView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					POSTION = 3;
				}
				MainThread.cancel(mToHideRunnable);
				MainThread.runLater(mToHideRunnable, 5 * 1000);
			}
		});
		findViewById(R.id.media__image_view__rotation).requestFocus();
	}

	private PowerManager.WakeLock getScreenLock() {
		if (mScreenLock == null) {
			mScreenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");
		}
		return mScreenLock;
	}

	private void startAutoPlay() {
		if (mAutoPlay == false) {
			mAutoPlay = true;
			getScreenLock().acquire();
			Log.i("", "Hua...getScreenLock().acquire();");
		}
		MainThread.runLater(mAutoRunnable, 4000);
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
		mediaimagebar.setVisibility(View.GONE);
		MainThread.cancel(mToHideRunnable);
		toFlyView(0, 0, 0, 1, true);
	}

	private void toShowView() {
		if (mShowing)
			return;
		switch (POSTION) {
		case 0:
			findViewById(R.id.media__image_view__rotation).requestFocus();
			break;
		case 1:
			findViewById(R.id.media__image_view__prev).requestFocus();
			break;
		case 2:
			findViewById(R.id.media__image_view__next).requestFocus();
			break;
		case 3:
			findViewById(R.id.media__image_view__auto).requestFocus();
			break;
		case 4:
			findViewById(R.id.media__image_view__info).requestFocus();
			break;

		default:
			break;
		}
		mShowing = true;
		MainThread.runLater(mToHideRunnable, 5 * 1000);
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
		if (keyCode == event.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (!mShowing) {
				if (mAutoPlay) {
					stopAutoPlay();
					Toast.makeText(ImagePlayerActivity.this, "结束幻灯片播放", Toast.LENGTH_SHORT).show();
					mAutoRunImageView.setImageResource(R.drawable.general__share__auto);
					findViewById(R.id.media__image_view__prev).setFocusable(true);
					findViewById(R.id.media__image_view__next).setFocusable(true);
				} else {
					startAutoPlay();
					Toast.makeText(ImagePlayerActivity.this, "开始幻灯片播放", Toast.LENGTH_SHORT).show();
					mAutoRunImageView.setImageResource(R.drawable.media__image__pause);
					findViewById(R.id.media__image_view__prev).setFocusable(false);
					findViewById(R.id.media__image_view__next).setFocusable(false);
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

}
