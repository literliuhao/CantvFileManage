package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.cantv.media.R;
import com.cantv.media.center.activity.GridViewActivity;
import com.cantv.media.center.adapter.MediaListAdapter;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.ui.DisclaimerDialog.OnClickableListener;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.StringUtil;
import com.cantv.media.center.utils.ToastUtils;
import com.cantv.media.center.utils.cybergarage.FileServer;
import com.cantv.media.center.utils.cybergarage.FileServer.OnInitlizedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@SuppressLint("ResourceAsColor")
public class MediaGridView extends CustomGridView {
    private static final String TAG = "MediaGridView";
    private MediaLoaderTask mTask;
    public MediaListAdapter mListAdapter;
    public Stack<Integer> mPosStack = new Stack<>();
    public Stack<List<Media>> mMediaStack = new Stack<>();// 记录上一级目录结构
    private String devicePath;
    private SourceType msSourceType;
    private boolean misShowProcess = false;
    private int mfirst = 0;
    private Context mContext;
    private GridViewActivity mActivity;
    public int mSelectItemPosition;
    public List<Media> mCurrMediaList = new ArrayList<>(); // 记录当前的数据集合
    public FileServer fileServer;
    private boolean autoLoadData = true;
    private DisclaimerDialog mDisclaimerDialog;
    private LoadingDialog mLoadingDialog;
    private String currentType;
    //不能安装应用标记
    public static Boolean flag = false;
    private ApkDialog apkDialog = null;

    public MediaGridView(Context context, SourceType sourceType) {
        super(context);
        mContext = context;
        mActivity = (GridViewActivity) context;
        mLoadingDialog = new LoadingDialog(mActivity);
        msSourceType = sourceType;
        switch (msSourceType) {
            case MOIVE:
                currentType = "暂无视频";
                break;
            case PICTURE:
                currentType = "暂无图片";
                break;
            case MUSIC:
                currentType = "暂无音频";
                break;
            case APP:
                currentType = "暂无应用";
                break;
            default:
                currentType = "暂无数据";
                break;
        }
        syncType(currentType);
        mListAdapter = new MediaListAdapter(context, mCurrMediaList);
        setGridViewSelector(new ColorDrawable(Color.TRANSPARENT));
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 1,如果是文件夹则继续显示下级列表
                // 2,如果是文件则全屏显示
                final Media item = mListAdapter.getItem(position);
                if (item.isDir) {
                    if (msSourceType == SourceType.SHARE) {
                        try {
                            String proxyPathPrefix = fileServer.getProxyPathPrefix();
                            mCurrMediaList = FileUtil.getSmbFileList(item.mUri, proxyPathPrefix);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showMessage(mContext, getResources().getString(R.string.data_exception));
                        }
                    } else if (!(msSourceType == SourceType.LOCAL || msSourceType == SourceType.DEVICE)) {
                        mCurrMediaList = FileUtil.getFileList(item.mUri, true, msSourceType);
                    } else {
                        mCurrMediaList = FileUtil.getFileList(item.mUri);
                    }
                    FileUtil.sortList(mCurrMediaList, FileComparator.SORT_TYPE_DEFAULT, true);
                    mPosStack.push(position);
                    mMediaStack.push(mListAdapter.getData());
                    mListAdapter.bindData(mCurrMediaList);
                    if (mCurrMediaList.size() == 0) {
                        showNoDataPage(currentType);
                        mActivity.mRTCountView.setVisibility(View.GONE);
                    } else {
                        setTextRTview(1 + "", " / " + mCurrMediaList.size());
                    }
                    MediaGridView.this.setSelection(0);
                } else if ((item.mType == SourceType.MOIVE) || (item.mType == SourceType.MUSIC) || (item.mType == SourceType.PICTURE)) {
                    openMediaActivity(item);
                } else {
                    if (item.mType == SourceType.APP) {
                        if (flag) {
                            int disclaimer = SharedPreferenceUtil.getDisclaimer();
                            if (disclaimer == 1) {
                                MediaUtils.openMedia(mActivity, item.isSharing ? item.sharePath : item.mUri);
                            } else {
                                if (mDisclaimerDialog == null) {
                                    mDisclaimerDialog = new DisclaimerDialog(mActivity);
                                    mDisclaimerDialog.setOnClickableListener(new OnClickableListener() {
                                        @Override
                                        public void onConfirmClickable() {
                                            MediaUtils.openMedia(mActivity, item.isSharing ? item.sharePath : item.mUri);
                                        }

                                        @Override
                                        public void onCancelClickable() {
                                            return;
                                        }
                                    });
                                }
                                mDisclaimerDialog.show();
                            }
                        } else {
                            apkDialog = new ApkDialog(mActivity);
                            apkDialog.setOnClickableListener(new ApkDialog.OnClickableListener() {
                                @Override
                                public void onConfirmClickable() {
                                    apkDialog.dismiss();
                                }
                            });
                            apkDialog.show();
                        }
                    } else {
                        MediaUtils.openMedia(mActivity, item.isSharing ? item.sharePath : item.mUri);
                    }
                }
            }
        });
        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    mSelectItemPosition = position;
                    setTextRTview(position + 1 + "", " / " + mCurrMediaList.size());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (msSourceType == SourceType.SHARE) {
            autoLoadData = false;
            fileServer = new FileServer();
            fileServer.setOnInitlizedListener(new OnInitlizedListener() {
                @Override
                public void onInitlized() {
                    asyncLoadData();
                    autoLoadData = true;
                }
            });
            fileServer.start();
        }
    }

    public void setStyle(MediaOrientation orientation) {
        mListAdapter.bindStyle(orientation);
    }

    public void asyncLoadData() {
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.RUNNING) {
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mTask = null;
        }
    }

    public void setDevicePath(String path) {
        devicePath = path;
    }

    public void show() {
        mPosStack.clear();
        misShowProcess = true;
        mTask = new MediaLoaderTask(msSourceType);
        asyncLoadData();
        MediaGridView.this.setSelection(0);
    }

    private class MediaLoaderTask extends AsyncTask<Void, Void, List<Media>> {
        private List<Media> mMediaes;
        private SourceType mSourceType;

        MediaLoaderTask(SourceType sourceType) {
            mMediaes = new ArrayList<>();
            mSourceType = sourceType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mfirst == 0) {
                if (misShowProcess) {
                    showProgressBar(null);
                }
                misShowProcess = false;
            }
        }

        @Override
        protected void onPostExecute(List<Media> result) {
            super.onPostExecute(result);
            FileUtil.sortList(result, FileComparator.SORT_TYPE_DEFAULT, true);
            mCurrMediaList = result;
            mListAdapter.bindData(result);
            setAdapter(mListAdapter);
            if (result.size() == 0) {
                showNoDataPage(currentType);
            } else {
                mActivity.mRTCountView.setVisibility(View.VISIBLE);
                setTextRTview("1", " / " + mCurrMediaList.size());
            }
            if (mfirst == 0) {
                dismissProgressBar();
            }
            mfirst = 1;
        }

        @Override
        protected List<Media> doInBackground(Void... params) {
            // 下面是进入根目录的几种情况,进入更深层的内容在点击事件那里
            try {
                if (mSourceType == SourceType.SHARE) {
                    List<Media> smbFileList = FileUtil.getSmbFileList(devicePath, fileServer.getProxyPathPrefix());
                    mMediaes.addAll(smbFileList);
                } else {
                    List<String> usbRootPaths = MediaUtils.getCurrPathList();
                    // 外接设备选择
                    if (MediaUtils.getUSBNum() > 1 && devicePath == null && mSourceType != SourceType.LOCAL) {
                        for (int i = 0; i < usbRootPaths.size(); i++) {
                            File file = new File(usbRootPaths.get(i));
                            Media fileInfo = FileUtil.getFileInfo(file, null, false);
                            mMediaes.add(fileInfo);
                        }
                    } else {
                        if (mSourceType == SourceType.LOCAL) {
                            mMediaes.addAll(FileUtil.getFileList(MediaUtils.getLocalPath()));
                        } else if (mSourceType == SourceType.DEVICE || devicePath != null) {
                            mMediaes.addAll(FileUtil.getFileList(devicePath));
                        } else {
                            if (usbRootPaths.size() > 0) { // 为了防止通过点击首页弹出框进来,而此时设备已经被移出而发生错误
                                List<Media> fileList = FileUtil.getFileList(usbRootPaths.get(0), true, msSourceType);
                                mMediaes.addAll(fileList);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mMediaes;
        }
    }

    public boolean onBack() {
        boolean isback = false;
        if (!mPosStack.isEmpty() && !mMediaStack.isEmpty()) {
            List<Media> pop = mMediaStack.pop();
            mListAdapter.bindData(pop);
            MediaGridView.this.setSelection(mPosStack.pop());
            mCurrMediaList = pop;
            if (mActivity.mRTCountView.getVisibility() == View.GONE) {
                mActivity.mRTCountView.setVisibility(View.VISIBLE);
            }
            setTextRTview("1", " / " + mCurrMediaList.size());
            isback = true;
        }
        return isback;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (autoLoadData && changedView == this && visibility == View.VISIBLE && mTask != null) {
            asyncLoadData();
        }
    }

    @Override
    protected void animateFoucs(View v) {
        if (v != null && v instanceof MediaItemView) {
            View child = ((MediaItemView) v).getFocusImage();
            animateFoucs(child.getLeft() + v.getLeft(), child.getTop() + v.getTop(), child.getWidth(), child.getHeight());
        } else {
            super.animateFoucs(v);
        }
    }

    protected void showProgressBar(String message) {

        mLoadingDialog.show();
    }

    protected void dismissProgressBar() {

        mLoadingDialog.dismiss();
    }

    /**
     * 打开指定媒体文件
     *
     * @param media
     */
    private void openMediaActivity(Media media) {
        ArrayList mediaPathList = FileUtil.getListFromList(mCurrMediaList, media.mType);
        int indexFromList = 1;
        mediaPathList = FileUtil.getMediaListFromList(mCurrMediaList, media.mType);
        if (media.isSharing) {
            indexFromList = FileUtil.getMediaIndexFromList(mediaPathList, media.sharePath);
        } else {
            indexFromList = FileUtil.getMediaIndexFromList(mediaPathList, media.mUri);
        }
        MediaUtils.openMediaActivity(mContext, mediaPathList, indexFromList, media.mType);
    }

    /**
     * 设置右上角索引展示
     *
     * @param st1 当前
     * @param st2 总数
     */
    public void setTextRTview(String st1, String st2) {
        StringUtil.getMergeString(mContext, mActivity.mRTCountView, R.style.rtTextStyle, st1, st2);
    }
}