package com.cantv.media.center.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cantv.liteplayer.core.focus.FocusScaleUtils;
import com.cantv.liteplayer.core.focus.FocusUtils;
import com.cantv.media.R;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.constants.FileCategory;
import com.cantv.media.center.data.UsbMounted;
import com.cantv.media.center.ui.MediaGridView;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends Activity implements OnFocusChangeListener {
    private static final String TAG = "HomeActivity";
    private static final String EXTERNAL = "external";
    private static final int SINGLE_DEVICE = 1;
    private static final int DOUBLE_DEVICE = 2;
    private static final int MUTI_DEVICE = 3;
    private Context mContext;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private FrameLayout mVideoIV;
    private FrameLayout mImageIV;
    private FrameLayout mAudioIV;
    private FrameLayout mAppIV;
    private FrameLayout mLocalIV;
    private FrameLayout mShareIV;
    private FrameLayout mExternalIV;
    private ImageView mExternalUIV;
    private FrameLayout mExternalIV1;
    private ImageView mExternalUIV1;
    private FrameLayout mExternalIV2;
    private ImageView mExternalUIV2;
    private TextView mVideoTV;
    private TextView mImageTV;
    private TextView mAudioTV;
    private TextView mAppTV;
    private TextView mLocalFreeTV;
    private TextView mLocalTotalTV;
    private TextView mVersion;
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
    private int mNum;
    private boolean isMounted;
    private Boolean isIN = false;
    private List<Integer> keyList = null;
    private List<Integer> dynamicList = null;
    private final int VALUE = 82;
    private final String PRIVATEKEY = "19!20!19!20";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_home);
        MyApplication.onFinishActivity();
        MyApplication.addHomeActivity(this);
        mVideoIV = (FrameLayout) findViewById(R.id.imageview_video_layout);
        mImageIV = (FrameLayout) findViewById(R.id.imageview_image_layout);
        mAudioIV = (FrameLayout) findViewById(R.id.imageview_audio_layout);
        mAppIV = (FrameLayout) findViewById(R.id.imageview_app_layout);
        mExternalIV = (FrameLayout) findViewById(R.id.layout_external_all);
        mExternalIV1 = (FrameLayout) findViewById(R.id.layout_external_1);
        mExternalIV2 = (FrameLayout) findViewById(R.id.layout_external_2);
        mLocalIV = (FrameLayout) findViewById(R.id.imageview_local_layout);
        mShareIV = (FrameLayout) findViewById(R.id.imageview_share_layout);
        mVideoTV = (TextView) findViewById(R.id.textview_video);
        mImageTV = (TextView) findViewById(R.id.textview_image);
        mAudioTV = (TextView) findViewById(R.id.textview_audio);
        mAppTV = (TextView) findViewById(R.id.textview_app);
        mLocalFreeTV = (TextView) findViewById(R.id.textview_localdiskfree);
        mLocalTotalTV = (TextView) findViewById(R.id.textview_localdisktotal);
        mVersion = (TextView) findViewById(R.id.tv_version);
        mFocusUtils = new FocusUtils(this, getWindow().getDecorView(), R.drawable.image_focus);
        mFocusScaleUtils = new FocusScaleUtils(300, 300, 1.05f, null, null);
        initUSB();
        initKey(PRIVATEKEY);
        mVideoIV.setOnFocusChangeListener(this);
        mImageIV.setOnFocusChangeListener(this);
        mAudioIV.setOnFocusChangeListener(this);
        mAppIV.setOnFocusChangeListener(this);
        mExternalIV.setOnFocusChangeListener(this);
        mExternalIV1.setOnFocusChangeListener(this);
        mExternalIV2.setOnFocusChangeListener(this);
        mLocalIV.setOnFocusChangeListener(this);
        mShareIV.setOnFocusChangeListener(this);
        mVideoIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, GridViewActivity.class);
                intent.putExtra("type", "video");
                if (mUsbRootPaths.size() > SINGLE_DEVICE) {
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
                if (mUsbRootPaths.size() > SINGLE_DEVICE) {
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
                if (mUsbRootPaths.size() > SINGLE_DEVICE) {
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
                if (mUsbRootPaths.size() > SINGLE_DEVICE) {
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
                if (mUsbRootPaths.size() > SINGLE_DEVICE) {
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
                intent.putExtra("filePath", mUsbRootPaths.get(SINGLE_DEVICE));
                startActivity(intent);
            }
        });
        mShareIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTimer();
                Intent intent = new Intent(mContext, DeviceShareActivity.class);
                startActivity(intent);
            }
        });
        mLocalFreeTV.setText(getString(R.string.str_localdiskfree) + MediaUtils.getInternalFree());
        mLocalTotalTV.setText(getString(R.string.str_localdisktotal) + MediaUtils.getInternalTotal());
        mVersion.setText(FileUtil.getVersionName(this));
        alertDialog = new AlertDialog.Builder(mContext).create();
    }

    private void initKey(String PRIVATEKEY) {
        keyList = new ArrayList<>();
        String[] stringKEYS = PRIVATEKEY.split("!");
        for (int i = 0; i < stringKEYS.length; i++) {
            keyList.add(Integer.valueOf(stringKEYS[i]));
        }
    }

    private void initUSB() {
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

    private void refreshUI() {
        mUsbRootPaths = MediaUtils.getCurrPathList();
        mNum = mUsbRootPaths.size();
        if (mNum > 0) {
            isMounted = true;
        } else {
            isMounted = false;
        }
        Log.w("设备数量~~", mNum + "");
        if (isMounted) {
            mExternalTotalTV.setVisibility(View.VISIBLE);
            if (mNum == DOUBLE_DEVICE) {
                mExternalIV.setVisibility(View.GONE);
                mExternalIV1.setVisibility(View.VISIBLE);
                mExternalIV2.setVisibility(View.VISIBLE);
                mExternalUIV1.setBackgroundResource(R.drawable.icon_u);
                mExternalUIV2.setBackgroundResource(R.drawable.icon_u);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        try { //外接存储设备移出过快可能出现集合越界异常
                            mExternalFreeTV1.setText(getString(R.string.str_localdiskfree) + MediaUtils.getRealFreeSize(mUsbRootPaths.get(0)));
                            mExternalTotalTV1.setText(getString(R.string.str_localdisktotal) + MediaUtils.getRealTotalSize(mUsbRootPaths.get(0)));
                            mExternalFreeTV2.setText(getString(R.string.str_localdiskfree) + MediaUtils.getRealFreeSize(mUsbRootPaths.get(SINGLE_DEVICE)));
                            mExternalTotalTV2.setText(getString(R.string.str_localdisktotal) + MediaUtils.getRealTotalSize(mUsbRootPaths.get(SINGLE_DEVICE)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                mExternalIV.setVisibility(View.VISIBLE);
                mExternalIV1.setVisibility(View.GONE);
                mExternalIV2.setVisibility(View.GONE);
                mExternalUIV.setBackgroundResource(R.drawable.icon_u);
                if (mNum == SINGLE_DEVICE) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mExternalFreeTV.setText(getString(R.string.str_localdiskfree) + MediaUtils.getRealFreeSize(mUsbRootPaths.get(0)));
                                mExternalTotalTV.setText(getString(R.string.str_localdisktotal) + MediaUtils.getRealTotalSize(mUsbRootPaths.get(0)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    mExternalFreeTV.setText(getString(R.string.str_total) + mNum + getString(R.string.str_devicenum));
                    mExternalTotalTV.setVisibility(View.GONE);
                }
            }
        } else {
            mExternalIV.setVisibility(View.VISIBLE);
            mExternalIV1.setVisibility(View.GONE);
            mExternalIV2.setVisibility(View.GONE);
            mExternalTotalTV.setVisibility(View.GONE);
            mExternalFreeTV.setText(R.string.str_nodev);
            mExternalUIV.setBackgroundResource(R.drawable.icon_nou);
        }
    }

//    BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
//                // openTimer();
//                sendUSBRefreshMsg();
//            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
//                closeTimer();
//                sendUSBRefreshMsg();
//            }
//        }
//    };

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

    private void sendUSBRefreshMsg() {
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean("file", false);
        // bundle.putBoolean("mount", isMounted);
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
                refreshUI();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.removeHomeActivity();
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendUSBRefreshMsg();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == mExternalIV.getId()) {
                mFocusScaleUtils.scaleToLargeWH(v, 1.03F, 1.06f);
                mFocusUtils.startMoveFocus(v, null, true, 0.95f, 0.84f, 0f, 0f);
            } else if (v.getId() == mExternalIV1.getId() || v.getId() == mExternalIV2.getId() || v.getId() == mLocalIV.getId() || v.getId() == mShareIV.getId()) {
                mFocusScaleUtils.scaleToLargeWH(v, 1.05F, 1.06f);
                mFocusUtils.startMoveFocus(v, null, true, 0.88f, 0.84f, 0f, 0f);
            } else {
                mFocusScaleUtils.scaleToLarge(v);
                mFocusUtils.startMoveFocus(v, true, 0.90F);
            }
        } else {
            mFocusScaleUtils.scaleToNormal(v);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
                if (MediaGridView.flag) {
                    MediaGridView.flag = false;
                } else {
                    MediaGridView.flag = true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
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
        Log.i("Mount", "Home ...");
        if (usbMounted.mIsRemoved) {
            closeTimer();
        }
        sendUSBRefreshMsg();
    }


    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}