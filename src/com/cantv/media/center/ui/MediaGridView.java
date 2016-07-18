package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.cantv.media.center.activity.GridViewActivity;
import com.cantv.media.center.adapter.MediaListAdapter;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.utils.FileComparator;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

@SuppressLint("ResourceAsColor")
public class MediaGridView extends CustomGridView {
    private static final String TAG = "MediaGridView";
    private static final int UPDATE_UI = 0;
    private MediaLoaderTask mTask;
    public MediaListAdapter mListAdapter;
    public Stack<Integer> mPosStack = new Stack<Integer>();
    public Stack<List<Media>> mMediaStack = new Stack<List<Media>>();// 记录上一级目录结构
    private List<Media> mMediaes;
    private String devicePath;
    private SourceType msSourceType;
    private UpdateMediaDataShow mUpdateMediaDataShow;
    public int mindex;
    private ProgressDialog mProgressDialog;
    private boolean misShowProcess = false;
    private int mfirst = 0;
    private Context mContext;
    private GridViewActivity mActivity;
    public int mSelectItemPosition;
    public List<Media> mCurrMediaList = new ArrayList<>(); // 记录当前的数据集合
    public String mDevecesFlag = null; //用来标记是否进入外接设备列表

    public MediaGridView(Context context, SourceType sourceType) {
        super(context);
        mActivity = (GridViewActivity) context;
        mContext = context;
        mProgressDialog = new ProgressDialog(context);
        WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
        mProgressDialog.getWindow().setGravity(Gravity.CENTER);
        mProgressDialog.getWindow().setAttributes(params);
        msSourceType = sourceType;
        mListAdapter = new MediaListAdapter(context, new ArrayList<Media>());
        setGridViewSelector(new ColorDrawable(Color.TRANSPARENT));
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 1,如果是文件夹则继续显示下级列表
                // 2,如果是文件则全屏显示
                Media item = (Media) mListAdapter.getItem(position);

                if (item.isDir) {
                    if (!(msSourceType == SourceType.LOCAL || msSourceType == SourceType.DEVICE)) {
                        mCurrMediaList = FileUtil.getFileList(item.mUri, true, msSourceType);
                    } else {
                        mCurrMediaList = FileUtil.getFileList(item.mUri);
                    }
                    FileUtil.sortList(mCurrMediaList, FileComparator.SORT_TYPE_DEFAULT, true);
                    mPosStack.push(position);
                    mMediaStack.push(mListAdapter.getData());
                    mListAdapter.bindData(mCurrMediaList);
                    if (mCurrMediaList.size() == 0) {
                        showNoDataPage();
                    }
                    MediaGridView.this.setSelection(0);

                } else if ((item.mType == SourceType.MOIVE)
                        || (item.mType == SourceType.MUSIC)
                        || (item.mType == SourceType.PICTURE)) {
                    openMediaActivity(item);
                } else {
                    MediaUtils.openMedia(mActivity, item.mUri);
                }
            }
        });
        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (view != null) {
                    mSelectItemPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setStyle(MediaOrientation orientation) {
        mListAdapter.bindStyle(orientation);
    }

    public void asyncLoadData() {
        if (mTask != null) {
            mTask.execute();
            mTask = null;
        }
    }

    public void setDevicePath(String path){
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
        private long mTime = System.currentTimeMillis();

        private Collator mCollator = Collator.getInstance(Locale.CHINESE);
        private Comparator mMediaSort = new Comparator<Media>() {
            @Override
            public int compare(Media arg0, Media arg1) {
                boolean one = arg0.isCollection();
                boolean two = arg1.isCollection();
                return one == two ? mCollator.compare(arg0.getName(),
                        arg1.getName()) : (one ? 1 : -1);
            }
        };

        MediaLoaderTask(SourceType sourceType) {
            mMediaes = new ArrayList<Media>();
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
            if (mfirst == 0) {
                dismissProgressBar();
            }

            FileUtil.sortList(result, FileComparator.SORT_TYPE_DEFAULT, true);
            mListAdapter.bindData(result);
            setAdapter(mListAdapter);
            if (result.size() == 0) {
                showNoDataPage();
            }
            mfirst = 1;
        }

        @Override
        protected List<Media> doInBackground(Void... params) {
            //下面是进入根目录的几种情况,进入更深层的内容在点击事件那里
            try {
                List<String> usbRootPaths = MediaUtils.getUsbRootPaths();
                if (null != mDevecesFlag) { //进入设备列表
                    for (int i = 0; i < usbRootPaths.size(); i++) {
                        File file = new File(usbRootPaths.get(i));
                        Media fileInfo = FileUtil.getFileInfo(file, null, false);
                        mMediaes.add(fileInfo);
                    }
                } else {
                    if (mSourceType == SourceType.LOCAL) {
                        mMediaes.addAll(FileUtil.getFileList(MediaUtils.getLocalPath()));
                    } else if (mSourceType == SourceType.DEVICE) {
                        mMediaes.addAll(FileUtil.getFileList(devicePath));
                    } else {
                        if (usbRootPaths.size() > 0) { //为了防止通过点击首页弹出框进来,而此时设备已经被移出而发生错误
                            List<Media> fileList = FileUtil.getFileList(usbRootPaths.get(0), true, msSourceType);
                            mMediaes.addAll(fileList);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            return mMediaes;
        }
    }

    public boolean onBack() {
        boolean isback = false;
        if (!mPosStack.isEmpty() && !mMediaStack.isEmpty()) {
            mListAdapter.bindData(mMediaStack.pop());
            MediaGridView.this.setSelection(mPosStack.pop());
            isback = true;
        }
        return isback;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && visibility == View.VISIBLE && mTask != null) {
            asyncLoadData();
        }
    }

    @Override
    protected void animateFoucs(View v) {
        if (v != null && v instanceof MediaItemView) {
            View child = ((MediaItemView) v).getFocusImage();
            animateFoucs(child.getLeft() + v.getLeft(),
                    child.getTop() + v.getTop(), child.getWidth(),
                    child.getHeight());
        } else {
            super.animateFoucs(v);
        }
    }

    public void setUpdateMediaDataShowListener(UpdateMediaDataShow listener) {
        mUpdateMediaDataShow = listener;
    }

    public interface UpdateMediaDataShow {
        void updateData(boolean isHasData);
    }

    protected void showProgressBar(String message) {
        if (mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(message == null ? "数据加载中..." : message);
        mProgressDialog.show();
    }

    protected void dismissProgressBar() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_UI:
                    if (mMediaes != null) {
                        mListAdapter.bindData(mMediaes);
                        setAdapter(mListAdapter);
                    }
                default:
                    break;
            }
        }
    };

    /**
     * 打开指定媒体文件
     *
     * @param media
     */
    private void openMediaActivity(Media media) {
        String substring = media.mUri.substring(0, media.mUri.lastIndexOf("/"));

        ArrayList<String> mediaPathList = FileUtil.getMediaPathList(substring,
                media.mType);
        int indexFromList = FileUtil
                .getIndexFromList(mediaPathList, media.mUri);

        MediaUtils.openMediaActivity(mContext, mediaPathList, indexFromList,
                media.mType);

    }

}
