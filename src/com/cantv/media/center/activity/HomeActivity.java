package com.cantv.media.center.activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import com.cantv.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.constants.FileCategory;
import com.cantv.media.center.utils.MediaUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";
	private static final String EXTERNAL = "external";
	private static final int SINGLE_DEVICE = 1;
	private static final int DOUBLE_DEVICE = 2;
	private static final int MUTI_DEVICE = 3;

	private Context mContext;
	private Timer mTimer;
	private TimerTask mTimerTask;

	private FrameLayout mExternalFL;
	private FrameLayout mExternalFL1;
	private FrameLayout mExternalFL2;

	private ImageView mVideoIV;
	private ImageView mImageIV;
	private ImageView mAudioIV;
	private ImageView mAppIV;
	private ImageView mLocalIV;
	private ImageView mShareIV;

	private ImageView mExternalIV;
	private ImageView mExternalUIV;

	private ImageView mExternalIV1;
	private ImageView mExternalUIV1;
	private ImageView mExternalIV2;
	private ImageView mExternalUIV2;

	private TextView mVideoTV;
	private TextView mImageTV;
	private TextView mAudioTV;
	private TextView mAppTV;
	private TextView mShareTV;
	
	private TextView mLocalFreeTV;
	private TextView mLocalTotalTV;

	private TextView mExternalFreeTV;
	private TextView mExternalTotalTV;

	private TextView mExternalFreeTV1;
	private TextView mExternalTotalTV1;
	private TextView mExternalFreeTV2;
	private TextView mExternalTotalTV2;

	private FocusUtils mFocusUtils;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		mContext = this;
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_home);

		mVideoIV = (ImageView)findViewById(R.id.imageview_video);
		mImageIV = (ImageView)findViewById(R.id.imageview_image);
		mAudioIV = (ImageView)findViewById(R.id.imageview_audio);
		mAppIV = (ImageView)findViewById(R.id.imageview_app);
		mLocalIV = (ImageView)findViewById(R.id.imageview_local);
		mShareIV = (ImageView)findViewById(R.id.imageview_share);

		mVideoTV = (TextView)findViewById(R.id.textview_video);
		mImageTV = (TextView)findViewById(R.id.textview_image);
		mAudioTV = (TextView)findViewById(R.id.textview_audio);
		mAppTV = (TextView)findViewById(R.id.textview_app);

		mLocalFreeTV = (TextView)findViewById(R.id.textview_localdiskfree);
		mLocalTotalTV = (TextView)findViewById(R.id.textview_localdisktotal);

		mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus);

		initUSB();

		String str_null = getString(R.string.str_null);
		String str_movie = getString(R.string.str_movie);
		String str_photo = getString(R.string.str_photo);
		String str_music = getString(R.string.str_music);
		String str_app = getString(R.string.str_app);

		mVideoTV.setText(str_null + str_movie);
		mImageTV.setText(str_null + str_photo);
		mAudioTV.setText(str_null + str_music);
		mAppTV.setText(str_null + str_app);

		mVideoIV.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					mFocusUtils.startMoveFocus(v, true, (float)0.9);										
                }
			}
		});
		mImageIV.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if(hasFocus) {
					mFocusUtils.startMoveFocus(v, true, (float)0.9);
                }
			}
		});
		mAudioIV.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if(hasFocus) {
					mFocusUtils.startMoveFocus(v, true, (float)0.9);
                }
			}
		});
		mAppIV.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if(hasFocus) {
					mFocusUtils.startMoveFocus(v, true, (float)0.9);
                }
			}
		});

		mLocalIV.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					mFocusUtils.startMoveFocus(v, true, (float)0.9);
                }
			}
		});
		mShareIV.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					mFocusUtils.startMoveFocus(v, true, (float)0.9);										
                }
			}
		});

		mVideoIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "video");
                //Uri uri = Uri.parse("data://video");
				//Intent intent = new Intent("android.intent.action.VIEW", uri);
		        startActivity(intent);
			}
		});

		mImageIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "image");
                //Uri uri = Uri.parse("data://image");
				//Intent intent = new Intent("android.intent.action.VIEW", uri);
		        startActivity(intent);
			}
		});
		
		mAudioIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "audio");
                //Uri uri = Uri.parse("data://audio");
				//Intent intent = new Intent("android.intent.action.VIEW", uri);
		        startActivity(intent);
			}
		});

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);

		mLocalFreeTV.setText(getString(R.string.str_localdiskfree) + MediaUtils.getInternalFree());
		mLocalTotalTV.setText(getString(R.string.str_localdisktotal) + MediaUtils.getInternalTotal());

		if (MediaUtils.isExistUSB()) {
			openTimer();
			sendUSBRefreshMsg(true, MediaUtils.getUSBNum());
		} else {
			sendUSBRefreshMsg(false, 0);
		}
	}

	private void initUSB() {
		mExternalFL = (FrameLayout)findViewById(R.id.layout_external_all);
		mExternalFL1 = (FrameLayout)findViewById(R.id.layout_external_1);
		mExternalFL2 = (FrameLayout)findViewById(R.id.layout_external_2);
		
		mExternalIV = (ImageView)findViewById(R.id.imageview_external);
		mExternalUIV = (ImageView)findViewById(R.id.imageview_external_u);
		mExternalFreeTV = (TextView)findViewById(R.id.textview_external_free);
		mExternalTotalTV = (TextView)findViewById(R.id.textview_external_total);
		
		mExternalIV1 = (ImageView)findViewById(R.id.imageview_external_1);
		mExternalUIV1 = (ImageView)findViewById(R.id.imageview_external_1_u);
		mExternalIV2 = (ImageView)findViewById(R.id.imageview_external_2);
		mExternalUIV2 = (ImageView)findViewById(R.id.imageview_external_2_u);

		mExternalFreeTV1 = (TextView)findViewById(R.id.textview_external_1_free);
		mExternalTotalTV1 = (TextView)findViewById(R.id.textview_external_1_total);
		mExternalFreeTV2 = (TextView)findViewById(R.id.textview_external_2_free);
		mExternalTotalTV2 = (TextView)findViewById(R.id.textview_external_2_total);
	}

	private void refreshUI(boolean isMounted, int num) {
		if (isMounted) {
			if (num == DOUBLE_DEVICE) {
				mExternalFL.setVisibility(View.GONE);
				mExternalFL1.setVisibility(View.VISIBLE);
				mExternalFL2.setVisibility(View.VISIBLE);

				mExternalUIV1.setBackgroundResource(R.drawable.icon_u);
				mExternalUIV2.setBackgroundResource(R.drawable.icon_u);

				mExternalFreeTV1.setText(getString(R.string.str_localdiskfree) + MediaUtils.getFree(1));
				mExternalTotalTV1.setText(getString(R.string.str_localdisktotal) + MediaUtils.getTotal(1));
				mExternalFreeTV2.setText(getString(R.string.str_localdiskfree) + MediaUtils.getFree(2));
				mExternalTotalTV2.setText(getString(R.string.str_localdisktotal) + MediaUtils.getTotal(2));

				mExternalIV1.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus) {
							mFocusUtils.startMoveFocus(v, true, (float)0.9);										
						}
					}
				});
				mExternalIV2.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus) {
							mFocusUtils.startMoveFocus(v, true, (float)0.9);										
						}
					}
				});

				mExternalIV1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						closeTimer();
					}
				});

				mExternalIV2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						closeTimer();
					}
				});
			} else {
				mExternalFL.setVisibility(View.VISIBLE);
				mExternalFL1.setVisibility(View.GONE);
				mExternalFL2.setVisibility(View.GONE);
				mExternalUIV.setBackgroundResource(R.drawable.icon_u);
				if (num == 1) {
					mExternalFreeTV.setText(getString(R.string.str_localdiskfree) + MediaUtils.getFree(0));
					mExternalTotalTV.setText(getString(R.string.str_localdisktotal) + MediaUtils.getTotal(0));
				} else {
					mExternalFreeTV.setText(getString(R.string.str_total) + num + getString(R.string.str_devicenum));
				}
				mExternalIV.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus) {
							mFocusUtils.startMoveFocus(v, true, (float)0.9);										
						}
					}
				});

				mExternalIV.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						closeTimer();
					}
				});
			}
		} else {
			mExternalFL.setVisibility(View.VISIBLE);
			mExternalFL1.setVisibility(View.GONE);
			mExternalFL2.setVisibility(View.GONE);
			mExternalFreeTV.setText(R.string.str_nodev);
			mExternalUIV.setBackgroundResource(R.drawable.icon_nou);
			mExternalIV.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus) {
						mFocusUtils.startMoveFocus(v, true, (float)0.9);										
					}
				}
			});

			mExternalIV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					closeTimer();
				}
			});
		}
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
				openTimer();
				sendUSBRefreshMsg(true, MediaUtils.getUSBNum());				
    			showMountedDialog();				
    		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)
    				|| intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
   				closeTimer();
   				sendUSBRefreshMsg(false, 0);
				sendFileRefreshMsg(0, 0, 0, 0);
    		}		
        }
    };

    private void showMountedDialog() {
    	AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();  
		alertDialog.show();  
		Window window = alertDialog.getWindow();  
		window.setContentView(R.layout.dialog_mounted);
		final FocusUtils focusUtils = new FocusUtils(this, window.getDecorView(), R.drawable.focus);
		ImageView dialogImage = (ImageView)window.findViewById(R.id.dialog_image);
		dialogImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "image");
				//Uri uri = Uri.parse("data://image");
				//Intent intent = new Intent("android.intent.action.VIEW", uri);
		        startActivity(intent);
			}
		});
		
		ImageView dialogVideo = (ImageView)window.findViewById(R.id.dialog_video);
		dialogVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "video");
				//Uri uri = Uri.parse("data://video");
				//Intent intent = new Intent("android.intent.action.VIEW", uri);
		        startActivity(intent);
			}
		});
		
		ImageView dialogAudio = (ImageView)window.findViewById(R.id.dialog_audio);
		dialogAudio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "audio");
				//Uri uri = Uri.parse("data://audio");
				//Intent intent = new Intent("android.intent.action.VIEW", uri);
		        startActivity(intent);
			}
		});
		
		ImageView dialogFile = (ImageView)window.findViewById(R.id.dialog_file);
		dialogFile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTimer();
				Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "file");
		        startActivity(intent);
			}
		});

		dialogVideo.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					focusUtils.startMoveFocus(v, true, (float)0.9);										
				}
			}
		});
		dialogAudio.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					focusUtils.startMoveFocus(v, true, (float)0.9);										
				}
			}
		});
		dialogImage.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					focusUtils.startMoveFocus(v, true, (float)0.9);										
                }
			}
		});		
		dialogFile.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					focusUtils.startMoveFocus(v, true, (float)0.9);										
                }
			}
		});
    }

	private void openTimer() {
		if (mTimer == null) {
			mTimer = new Timer(true);
		}
		if (mTimerTask == null) {
			mTimerTask = new TimerTask(){  
				public void run() {
					refreshMediaCategory();
				}  
			};
		}
		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, 0, 5000);
		}
	}

	private void closeTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask = null;
		}
	}

	private void sendUSBRefreshMsg(boolean isMounted, int num) {
		Message msg = mHandler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putBoolean("file", false);
		bundle.putBoolean("mount", isMounted);
		bundle.putInt("num", num);
		msg.setData(bundle);
		msg.sendToTarget();
	}

	private void sendFileRefreshMsg(int video, int image, int audio, int app) {
		Message msg = mHandler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putBoolean("file", true);
		bundle.putInt("video", video);
		bundle.putInt("image", image);
		bundle.putInt("audio", audio);
		bundle.putInt("app", app);
		msg.setData(bundle);
		msg.sendToTarget();
	}

	private void refreshMediaCategory() {
        String[] columns;
        
        //瑙嗛鏂囦欢
	    Uri uri = MediaStore.Video.Media.getContentUri(EXTERNAL);
	    columns = new String[] {MediaStore.Video.Media.TITLE};
	    int video = refreshMediaCategory(FileCategory.Video, columns, uri);

	    //鍥剧墖鏂囦欢
	    uri = MediaStore.Images.Media.getContentUri(EXTERNAL);
	    columns = new String[] {MediaStore.Images.Media.TITLE};
	    int image = refreshMediaCategory(FileCategory.Picture, columns, uri);

	    //闊抽鏂囦欢
		uri = MediaStore.Audio.Media.getContentUri(EXTERNAL);
		columns = new String[] {MediaStore.Audio.Media.TITLE};
		int audio = refreshMediaCategory(FileCategory.Music, columns, uri);

		//搴旂敤鏂囦欢
		uri = MediaStore.Files.getContentUri(EXTERNAL);
		columns = new String[] {MediaStore.Files.FileColumns.TITLE};
		int app = refreshMediaCategory(FileCategory.Apk, columns, uri);
		
		sendFileRefreshMsg(video, image, audio, app);
	}

	private int refreshMediaCategory(FileCategory fc, String[] columns, Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, columns, MediaUtils.buildSelectionByCategory(fc), null, null);
        if (cursor == null) {
            Log.i(TAG, "fail to query uri:" + uri);
            return 0;
        }

        //cursor.moveToFirst();
        return cursor.getCount();
    }
    
    private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String str_total = getString(R.string.str_total);
			String str_null = getString(R.string.str_null);
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			if (bundle == null) {
				return;
			}

			if (bundle.getBoolean("file")) {
				int video = bundle.getInt("video");
				if (video > 0) {
					mVideoTV.setText(str_total + video + getString(R.string.str_movienum));
				} else {
					mVideoTV.setText(str_null + getString(R.string.str_movie));
				}

				int image = bundle.getInt("image");
				if (image > 0) {
					mImageTV.setText(str_total + image + getString(R.string.str_photonum));
				} else {
					mImageTV.setText(str_null + getString(R.string.str_photo));
				}
				
				int audio = bundle.getInt("audio");
				if (audio > 0) {
					mAudioTV.setText(str_total + audio + getString(R.string.str_musicnum));
				} else {
					mAudioTV.setText(str_null + getString(R.string.str_music));
				}
				
				int app = bundle.getInt("app");
				if (app > 0) {
					mAppTV.setText(str_total + app + getString(R.string.str_appnum));
				} else {
					mAppTV.setText(str_null + getString(R.string.str_app));
				}
			} else {
				boolean mount = bundle.getBoolean("mount");
				int num = bundle.getInt("num");
				refreshUI(mount, num);
			}
		}
	};

    @Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		ActivityManager am = (ActivityManager) getSystemService(this.ACTIVITY_SERVICE);
		am.killBackgroundProcesses(getPackageName()); // 鐎瑰苯鍙忛柅鈧崙鍝勭安閻�?
		//SecretCollector.get().unregeistToCollectorCore(this);

	}
}
