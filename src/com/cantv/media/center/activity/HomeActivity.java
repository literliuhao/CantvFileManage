package com.cantv.media.center.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.constants.FileCategory;
import com.cantv.media.center.service.BootDialogService;
import com.cantv.media.center.utils.MediaUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends Activity implements OnFocusChangeListener {
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
    private FocusScaleUtils mFocusScaleUtils;
    private List<String> mUsbRootPaths = new ArrayList<>();
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mContext = this;
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        mVideoIV = (ImageView) findViewById(R.id.imageview_video);
        mImageIV = (ImageView) findViewById(R.id.imageview_image);
        mAudioIV = (ImageView) findViewById(R.id.imageview_audio);
        mAppIV = (ImageView) findViewById(R.id.imageview_app);
        mExternalIV = (ImageView) findViewById(R.id.imageview_external);
        mExternalIV1 = (ImageView) findViewById(R.id.imageview_external_1);
        mExternalIV2 = (ImageView) findViewById(R.id.imageview_external_2);
        mLocalIV = (ImageView) findViewById(R.id.imageview_local);
        mShareIV = (ImageView) findViewById(R.id.imageview_share);
        mVideoTV = (TextView) findViewById(R.id.textview_video);
        mImageTV = (TextView) findViewById(R.id.textview_image);
        mAudioTV = (TextView) findViewById(R.id.textview_audio);
        mAppTV = (TextView) findViewById(R.id.textview_app);
        mLocalFreeTV = (TextView) findViewById(R.id.textview_localdiskfree);
        mLocalTotalTV = (TextView) findViewById(R.id.textview_localdisktotal);
        mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.focus);
        mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.05f, null, null);
        initUSB();
        mVideoIV.setOnFocusChangeListener(this);
        mImageIV.setOnFocusChangeListener(this);
        mAudioIV.setOnFocusChangeListener(this);
        mAppIV.setOnFocusChangeListener(this);
        mExternalIV.setOnFocusChangeListener(this);
        mExternalIV1.setOnFocusChangeListener(this);
        mExternalIV2.setOnFocusChangeListener(this);
        mLocalIV.setOnFocusChangeListener(this);
        // mExternalFL.setOnFocusChangeListener(this);
        // mExternalFL1.setOnFocusChangeListener(this);
        // mExternalFL2.setOnFocusChangeListener(this);
        mShareIV.setOnFocusChangeListener(this);
        mVideoIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "video");
                if (mUsbRootPaths.size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                startActivity(intent);
            }
        });
        mImageIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "image");
                if (mUsbRootPaths.size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                startActivity(intent);
            }
        });
        mAudioIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "audio");
                if (mUsbRootPaths.size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                startActivity(intent);
            }
        });
        mAppIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "app");
                if (mUsbRootPaths.size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                startActivity(intent);
            }
        });
        mLocalIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "local");
                startActivity(intent);
            }
        });
        mExternalIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                if (mUsbRootPaths.size() > 1) {
                    intent.putExtra("toListFlag", "ListFlag");
                }
                intent.putExtra("type", "device1");
                startActivity(intent);
            }
        });
        mExternalIV1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "device1");
                startActivity(intent);
            }
        });
        mExternalIV2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "device2");
                intent.putExtra("filePath", mUsbRootPaths.get(1));
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
        alertDialog = new AlertDialog.Builder(mContext).create();

        Intent intentStart = new Intent(this, BootDialogService.class);
        intentStart.setAction("com.cantv.service.RECEIVER_START");
        this.startService(intentStart);
        // showMountedDialog();
    }

    private void initUSB() {
        mExternalFL = (FrameLayout) findViewById(R.id.layout_external_all);
        mExternalFL1 = (FrameLayout) findViewById(R.id.layout_external_1);
        mExternalFL2 = (FrameLayout) findViewById(R.id.layout_external_2);

        mExternalUIV = (ImageView) findViewById(R.id.imageview_external_u);
        mExternalFreeTV = (TextView) findViewById(R.id.textview_external_free);
        mExternalTotalTV = (TextView) findViewById(R.id.textview_external_total);

        mExternalUIV1 = (ImageView) findViewById(R.id.imageview_external_1_u);

        mExternalUIV2 = (ImageView) findViewById(R.id.imageview_external_2_u);
        mExternalFreeTV1 = (TextView) findViewById(R.id.textview_external_1_free);
        mExternalTotalTV1 = (TextView) findViewById(R.id.textview_external_1_total);
        mExternalFreeTV2 = (TextView) findViewById(R.id.textview_external_2_free);
        mExternalTotalTV2 = (TextView) findViewById(R.id.textview_external_2_total);
    }

    private void refreshUI(boolean isMounted, int num) {
        if (isMounted) {
            mExternalTotalTV.setVisibility(View.VISIBLE);
            if (num == DOUBLE_DEVICE) {
                mExternalFL.setVisibility(View.GONE);
                mExternalFL1.setVisibility(View.VISIBLE);
                mExternalFL2.setVisibility(View.VISIBLE);
                mExternalUIV1.setBackgroundResource(R.drawable.icon_u);
                mExternalUIV2.setBackgroundResource(R.drawable.icon_u);
                mExternalFreeTV1.setText(getString(R.string.str_localdiskfree) + MediaUtils.getFree(mUsbRootPaths.get(0)));
                mExternalTotalTV1.setText(getString(R.string.str_localdisktotal) + MediaUtils.getTotal(mUsbRootPaths.get(0)));
                mExternalFreeTV2.setText(getString(R.string.str_localdiskfree) + MediaUtils.getFree(mUsbRootPaths.get(1)));
                mExternalTotalTV2.setText(getString(R.string.str_localdisktotal) + MediaUtils.getTotal(mUsbRootPaths.get(1)));
            } else {
                mExternalFL.setVisibility(View.VISIBLE);
                mExternalFL1.setVisibility(View.GONE);
                mExternalFL2.setVisibility(View.GONE);
                mExternalUIV.setBackgroundResource(R.drawable.icon_u);
                if (num == 1) {
                    mExternalFreeTV.setText(getString(R.string.str_localdiskfree) + MediaUtils.getFree(mUsbRootPaths.get(0)));
                    mExternalTotalTV.setText(getString(R.string.str_localdisktotal) + MediaUtils.getTotal(mUsbRootPaths.get(0)));
                } else {
                    mExternalFreeTV.setText(getString(R.string.str_total) + num + getString(R.string.str_devicenum));
                    mExternalTotalTV.setVisibility(View.GONE);
                }
                mExternalIV.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            mFocusScaleUtils.scaleToLargeWH(v, 1.01F, 1.04f);
                            mFocusUtils.startMoveFocus(v, null, true, 0.92f, 1.1f, 0.90F);
                        } else {
                            mFocusScaleUtils.scaleToNormal(v);
                        }
                    }
                });
            }
        } else {
            mExternalFL.setVisibility(View.VISIBLE);
            mExternalFL1.setVisibility(View.GONE);
            mExternalFL2.setVisibility(View.GONE);
            mExternalTotalTV.setVisibility(View.GONE);
            mExternalFreeTV.setText(R.string.str_nodev);
            mExternalUIV.setBackgroundResource(R.drawable.icon_nou);
            mExternalIV.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mFocusScaleUtils.scaleToLargeWH(v, 1.01F, 1.04f);
                        mFocusUtils.startMoveFocus(v, null, true, 0.92f, 1.1f, 0.90F);
                    } else {
                        mFocusScaleUtils.scaleToNormal(v);
                    }
                }
            });
//			mExternalIV.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					closeTimer();
//				}
//			});
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                // openTimer();
                MediaUtils.addUsbRootPaths(intent.getData().getPath());
                mUsbRootPaths = MediaUtils.getUsbRootPaths();
                sendUSBRefreshMsg(true, mUsbRootPaths.size());
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                closeTimer();
                MediaUtils.removeUsbRootPaths(intent.getData().getPath());
                mUsbRootPaths = MediaUtils.getUsbRootPaths();
                if (null != mUsbRootPaths && mUsbRootPaths.size() > 0) {
                    sendUSBRefreshMsg(true, mUsbRootPaths.size());
                } else {
                    sendUSBRefreshMsg(false, 0);
                }
            }
        }
    };

    private void openTimer() {
        if (mTimer == null) {
            mTimer = new Timer(true);
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
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
        // 视频文件
        Uri uri = MediaStore.Video.Media.getContentUri(EXTERNAL);
        columns = new String[]{MediaStore.Video.Media.TITLE};
        int video = refreshMediaCategory(FileCategory.Video, columns, uri);
        // 图片文件
        uri = MediaStore.Images.Media.getContentUri(EXTERNAL);
        columns = new String[]{MediaStore.Images.Media.TITLE};
        int image = refreshMediaCategory(FileCategory.Picture, columns, uri);
        // 音频文件
        uri = MediaStore.Audio.Media.getContentUri(EXTERNAL);
        columns = new String[]{MediaStore.Audio.Media.TITLE};
        int audio = refreshMediaCategory(FileCategory.Music, columns, uri);
        // 应用文件
        uri = MediaStore.Files.getContentUri(EXTERNAL);
        columns = new String[]{MediaStore.Files.FileColumns.TITLE};
        int app = refreshMediaCategory(FileCategory.Apk, columns, uri);
        sendFileRefreshMsg(video, image, audio, app);
    }

    private int refreshMediaCategory(FileCategory fc, String[] columns, Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri, columns, MediaUtils.buildSelectionByCategory(fc), null, null);
        if (cursor == null) {
            Log.i(TAG, "fail to query uri:" + uri);
            return 0;
        }
        // cursor.moveToFirst();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // openTimer();
        if (MediaUtils.isExistUSB()) {
            mUsbRootPaths = MediaUtils.getUsbRootPaths();
            sendUSBRefreshMsg(true, mUsbRootPaths.size());
        } else {
            sendUSBRefreshMsg(false, 0);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mFocusScaleUtils.scaleToLarge(v);
            mFocusUtils.startMoveFocus(v, true, 0.92F);
        } else {
            mFocusScaleUtils.scaleToNormal(v);
        }
    }
}
