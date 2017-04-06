package com.cantv.media.center.directory.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.cantv.media.R;
import com.cantv.media.center.directory.adapter.MediaListAdapter;
import com.cantv.media.center.constants.MediaOrientation;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.directory.dialog.ApkForbidDialog;
import com.cantv.media.center.directory.dialog.CommonDialog;
import com.cantv.media.center.dialog.LoadingDialog;
import com.cantv.media.center.utils.CopyPasUtils;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.cantv.media.center.utils.StringUtil;
import com.cantv.media.center.utils.ToastUtils;
import com.cantv.media.center.share.cybergarage.FileServer;
import com.cantv.media.center.share.cybergarage.FileServer.OnInitlizedListener;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import jcifs.smb.SmbFile;

@SuppressLint("ResourceAsColor")
public class MediaGridView extends CustomGridView {
    private static final String TAG = "MediaGridView";
    private MediaLoaderTask mTask;
    public MediaListAdapter mListAdapter;
    public Stack<Integer> mPosStack = new Stack<>();
    public Stack<List<Media>> mMediaStack = new Stack<>();// 记录上一级目录结构
    private String devicePath;
    public SourceType msSourceType;
    private boolean misShowProcess = false;
    private Boolean mFirst = false;
    private Context mContext;
    private GridViewActivity mActivity;
    public int mSelectItemPosition;
    public List<Media> mCurrMediaList = new ArrayList<>(); // 记录当前的数据集合
    public FileServer fileServer;
    private boolean autoLoadData = true;
    private LoadingDialog mLoadingDialog;
    private String currentType;
    //不能安装应用标记
    public static Boolean flag = false;
    private ApkForbidDialog apkForbidDialog = null;
    private String install_app = "0";
    private CommonDialog mDisclaimerDialog;
    private CommonDialog apkDialog = null;
    private String mCurrFolderPath;

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
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Media item = mListAdapter.getItem(position);
                // 1,如果是文件夹则继续显示下级列表
                // 2,如果是文件则全屏显示
                if (item.isDir) {
                    if (msSourceType == SourceType.SHARE) {
                        try {
                            String proxyPathPrefix = fileServer.getProxyPathPrefix();
                            FileUtil.getSmbFileList(item.mUri, proxyPathPrefix, new FileUtil.OnSmbFileListListener() {
                                @Override
                                public void findSmbFileListFinish(List<Media> list) {
                                    mCurrMediaList = list;
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            clickSetData(position);
                                        }
                                    });
                                }

                                @Override
                                public void findSmbFileListFiled() {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtils.showMessage(mContext, "共享已断开,请重新连接!");
                                        }
                                    });
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showMessage(mContext, getResources().getString(R.string.data_exception));
                        }
                    } else if (!(msSourceType == SourceType.LOCAL || msSourceType == SourceType.DEVICE)) {
                        mCurrFolderPath = item.mUri;
                        FileUtil.getFileList(item.mUri, true, new FileUtil.OnFileListListener() {
                            @Override
                            public void findFileListFinish(List<Media> list) {
                                mCurrMediaList = list;
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        clickSetData(position);
                                    }
                                });
                            }

                        }, msSourceType);
                    } else {
                        mCurrFolderPath = item.mUri;
                        FileUtil.getFileList(item.mUri, new FileUtil.OnFileListListener() {
                            @Override
                            public void findFileListFinish(List<Media> list) {
                                mCurrMediaList = list;
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        clickSetData(position);
                                    }
                                });
                            }
                        });
                    }

                } else if ((item.mType == SourceType.MOIVE) || (item.mType == SourceType.MUSIC) || (item.mType == SourceType.PICTURE)) {
                    if (item.mUri.contains(":")) {
                        new Thread(new Runnable() { //防止共享无响应
                            @Override
                            public void run() {
                                try {

                                    SmbFile file = new SmbFile(item.mUri);
                                    Log.w("文件大小", file.getContentLength() + "");
                                    if (file.getContentLength() > 0) {
                                        mActivity.isStartAc = true;
                                        openMediaActivity(item);
                                    } else {
                                        mActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtils.showMessage(mContext, "共享已断开,请重新连接!");
                                            }
                                        });
                                    }
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else {
                        mActivity.isStartAc = true;
                        openMediaActivity(item);
                    }
                } else {
                    if (item.mType == SourceType.APP) {
                        //添加APP安装设置弹框(OS1.2)
//                        if (SystemCateUtil.isContainsCurrModel()) {
//                            install_app = Settings.System.getString(mActivity.getContentResolver(), "install_app");
//                            if (null == install_app) {
//                                getApkForbidDialog();
//                            } else if (install_app.equals("1")) {
//                                getDisclaimerDialog(item);
//                            } else if (install_app.equals("0")) {
//                                getSettingDialog();
//                            }
//                        } else {
                        //添加APP弹框(OS1.1)
                        if (flag) {
                            getDisclaimerDialog(item);
                        } else {
                            getApkForbidDialog();
                        }
//                        }
                    } else {
                        mActivity.isStartAc = true;
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

    /**
     * 点击设置数据
     *
     * @param position
     */
    private void clickSetData(int position) {
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
            if (!mFirst) {
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
            if (!mFirst) {
                dismissProgressBar();
            }
            mFirst = true;
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
                    // 外接设备选择(多个外接设备)
                    if (MediaUtils.getUSBNum() > 1 && devicePath == null && mSourceType != SourceType.LOCAL) {
                        for (int i = 0; i < usbRootPaths.size(); i++) {
                            File file = new File(usbRootPaths.get(i));
                            Media fileInfo = FileUtil.getFileInfo(file, null, false);
                            mMediaes.add(fileInfo);
                        }
                    } else {
                        //本机
                        if (mSourceType == SourceType.LOCAL) {
                            mMediaes.addAll(FileUtil.getFileList(MediaUtils.getLocalPath()));
                        } else if (mSourceType == SourceType.DEVICE || devicePath != null) {
                            if (null == devicePath && MediaUtils.getCurrPathList().size() > 0) {
                                devicePath = MediaUtils.getCurrPathList().get(0);
                            }
                            //外接设备
                            mMediaes.addAll(FileUtil.getFileList(devicePath));
                        } else {
                            if (usbRootPaths.size() > 0) { // 为了防止通过点击首页弹出框进来,而此时设备已经被移出而发生错误
                                mMediaes.addAll(FileUtil.getFileList(usbRootPaths.get(0), true, msSourceType));
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
        if (!mPosStack.isEmpty() && !mMediaStack.isEmpty()) {
            List<Media> pop = mMediaStack.pop();
            mListAdapter.bindData(pop);
            MediaGridView.this.setSelection(mPosStack.pop());
            mCurrMediaList = pop;
            if (mActivity.mRTCountView.getVisibility() == View.GONE) {
                mActivity.mRTCountView.setVisibility(View.VISIBLE);
            }
            setTextRTview("1", " / " + mCurrMediaList.size());
//            onFocus();
            return true;
        }
        return false;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (autoLoadData && changedView == this && visibility == View.VISIBLE && mTask != null) {
            asyncLoadData();
        }
    }

    @Override
    protected void animateFocus(View v) {
        if (v != null && v instanceof MediaItemView) {
            View child = ((MediaItemView) v).getFocusImage();
            animateFocus(child.getLeft() + v.getLeft(), child.getTop() + v.getTop(), child.getWidth(), child.getHeight());
        } else {
            super.animateFocus(v);
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

    /**
     * 禁止安装Apk
     */
    private void getApkForbidDialog() {
        apkForbidDialog = new ApkForbidDialog(mActivity);
        apkForbidDialog.setOnClickableListener(new ApkForbidDialog.OnClickableListener() {
            @Override
            public void onConfirmClickable() {
                apkForbidDialog.dismiss();
            }
        });
        apkForbidDialog.show();
    }

    /**
     * 免责声明
     *
     * @param item
     */
    private void getDisclaimerDialog(final Media item) {
        int disclaimer = SharedPreferenceUtil.getDisclaimer();
        if (disclaimer == 1) {
            MediaUtils.openMedia(mActivity, item.isSharing ? item.sharePath : item.mUri);
        } else {
            if (mDisclaimerDialog == null) {
                mDisclaimerDialog = new CommonDialog(mActivity);
                mDisclaimerDialog.setTitle(mActivity.getString(R.string.disclaimer_title))
                        .setContent(mActivity.getString(R.string.disclaimer_text), 0)
                        .setButtonContent(mActivity.getString(R.string.str_disclaimerconfirm), mActivity.getString(R.string.str_cancel));
                mDisclaimerDialog.setOnClickableListener(new CommonDialog.OnClickableListener() {
                    @Override
                    public void onConfirmClickable() {
                        SharedPreferenceUtil.setDisclaimer(1);
                        MediaUtils.openMedia(mActivity, item.isSharing ? item.sharePath : item.mUri);
                    }

                    @Override
                    public void onCancelClickable() {
                        SharedPreferenceUtil.setDisclaimer(0);
                        return;
                    }
                });
            }
            mDisclaimerDialog.show();
        }
    }

    /**
     * 设置弹出
     */
    private void getSettingDialog() {
        if (apkDialog == null) {
            apkDialog = new CommonDialog(mActivity);
            apkDialog.setTitle(mActivity.getString(R.string.disclaimer_install))
                    .setContentSize((int) mActivity.getResources().getDimension(R.dimen.px26))
                    .setContent(mActivity.getString(R.string.disclaimer_apkinfo), 0)
                    .setButtonContent(mActivity.getString(R.string.disclaimer_setting), mActivity.getString(R.string.disclaimer_cancel));
        }
        apkDialog.setOnClickableListener(new CommonDialog.OnClickableListener() {
            @Override
            public void onConfirmClickable() {
                try {
                    mActivity.startActivity(new Intent("com.os.setting.GENERAL_SETTINGS"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelClickable() {

            }
        });
        apkDialog.show();
    }

    /**
     * 重置资源类型
     *
     * @param sourceType
     */
    public void resetSourceType(SourceType sourceType) {
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
    }

    /**
     * 粘贴监听
     */
    public interface YPasteListener {
        void onStartPaste();

        void onPasteFailed();

        void onPasteSucceed();

        void onRefreshList(String currPath);   //拷贝到当前根目录,需要刷新当前数据
    }

    /**
     * @param isCopy 是否是拷贝文件,true 拷贝,false 粘贴
     */
    public void copyPasteFile(final boolean isCopy, final YPasteListener yPasteListener) {
        String path1 = "";
        Media item = null;
        if (mCurrMediaList.size() > 0) {
            item = mListAdapter.getItem(mSelectItemPosition);
            path1 = item.mUri;
        } else {
            path1 = mCurrFolderPath;
            item = new Media(SourceType.FOLDER, "");
            item.isDir = true;
        }

        final String path = path1;

        if (item.isDir) {
            if (isCopy) {
                ToastUtils.showMessage(mContext, "暂不支持复制文件夹");
                return;
            }

            final String copyFilePath = SharedPreferenceUtil.getCopyFilePath();
            if (!TextUtils.isEmpty(copyFilePath)) {
//                        File.separator
                if (null != yPasteListener) {
                    yPasteListener.onStartPaste();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String newPath = path + copyFilePath.substring(copyFilePath.lastIndexOf("/"));
                        File file = new File(newPath);
                        if (file.exists()) {
                            yPasteListener.onPasteFailed();
                            ToastUtils.showMessage(mContext, "文件已经存在,无法粘贴");
                            return;
                        }
                        boolean isFuccessed = CopyPasUtils.nioTransferCopy(copyFilePath, newPath);
//                boolean isFuccessed = CopyPasUtils.moveFile(copyFilePath, item.mUri);
                        if (isFuccessed) {
                            ToastUtils.showMessage(mContext, "粘贴成功");
                            SharedPreferenceUtil.saveCopyFilePath("");  //相当于清空粘贴板
                            if (null != yPasteListener) {
                                yPasteListener.onPasteSucceed();
                                if (!(mCurrMediaList.size() > 0)) {
                                    yPasteListener.onRefreshList(path);
                                }
                            }
                        } else {
                            ToastUtils.showMessage(mContext, "粘贴失败");
                            if (null != yPasteListener) {
                                yPasteListener.onPasteFailed();
                            }
                        }
                    }
                }).start();
            } else {
                ToastUtils.showMessage(mContext, "请先复制文件");
            }
        } else {
            if (isCopy) {
                if (SharedPreferenceUtil.saveCopyFilePath(path)) {
                    ToastUtils.showMessage(mContext, "复制成功");
                } else {
                    ToastUtils.showMessage(mContext, "复制失败");
                }
            } else { //粘贴在当前文件同级目录
                final String copyFilePath = SharedPreferenceUtil.getCopyFilePath();
                if (TextUtils.isEmpty(copyFilePath)) {
                    ToastUtils.showMessage(mContext, "请先复制文件");
                    return;
                }

                if (null != yPasteListener) {
                    yPasteListener.onStartPaste();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String newPath = path.substring(0, path.lastIndexOf("/")) + copyFilePath.substring(copyFilePath.lastIndexOf("/"));
                        File file = new File(newPath);
                        if (file.exists()) {
                            yPasteListener.onPasteFailed();
                            ToastUtils.showMessage(mContext, "文件已经存在,无法粘贴");
                            return;
                        }
                        boolean isFuccessed = CopyPasUtils.nioTransferCopy(copyFilePath, newPath);
                        if (isFuccessed) {
                            ToastUtils.showMessage(mContext, "粘贴成功");
                            SharedPreferenceUtil.saveCopyFilePath("");  //相当于清空粘贴板
                            if (null != yPasteListener) {
                                yPasteListener.onPasteSucceed();
                                yPasteListener.onRefreshList(newPath.substring(0, newPath.lastIndexOf("/") + 1));
                            }
                        } else {
                            ToastUtils.showMessage(mContext, "粘贴失败");
                            if (null != yPasteListener) {
                                yPasteListener.onPasteFailed();
                            }
                        }
                    }
                }).start();
            }
        }
    }

}