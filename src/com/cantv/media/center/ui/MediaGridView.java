package com.cantv.media.center.ui;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import com.cantv.media.R;
import com.cantv.media.center.adapter.MediaListAdapter;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Audio;
import com.cantv.media.center.data.Image;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.data.Video;
import com.cantv.media.center.utils.MediaUtils;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("ResourceAsColor")
public class MediaGridView extends CustomGridView {
	private static final String TAG = "MediaGridView";
	private static final int UPDATE_UI = 0; 
	private MediaLoaderTask mTask;
	private MediaUtils mMediaUtils;
	public MediaListAdapter mListAdapter;
	private Stack<Integer> mPosStack = new Stack<Integer>();
	private Stack<List<Media>> mMediaStack = new Stack<List<Media>>();
	private List<Media> mMediaes;
	//private List<String> muries;
	private String mPath;
	private SourceType msSourceType;
	private UpdateMediaDataShow mUpdateMediaDataShow;
	public int mindex;
	private ProgressDialog mProgressDialog;
	private boolean misShowProcess = false;
	private int mfirst=0;
	private Context mContext;
	
	public MediaGridView(Context context, String uri, SourceType sourceType) {
		super(context);
		mContext = context;
		mPath = uri;
		mProgressDialog = new ProgressDialog(context);
		WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
		mProgressDialog.getWindow().setGravity(Gravity.CENTER);
		mProgressDialog.getWindow().setAttributes(params);
		msSourceType = sourceType;
		mMediaUtils = new MediaUtils();
		setHorizontalSpacing((int) getResources().getDimension(R.dimen.px146));
		setVerticalSpacing((int) getResources().getDimension(R.dimen.px86));
		mListAdapter = new MediaListAdapter(context, new ArrayList<Media>());
		setGridViewSelector(new ColorDrawable(Color.TRANSPARENT));
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 1,如果是文件夹则继续显示下级列表
				// 2,如果是文件则全屏显示
				Media item = (Media) mListAdapter.getItem(position);
				if (item.isCollection()) {
					mPosStack.push(position);
					mMediaStack.push(mListAdapter.getData());
					mListAdapter.bindData(item.getSubMedias());
					MediaGridView.this.setSelection(0);
				} else {
					// 全屏展示
					mMediaUtils.showMediaDetail(getContext(), mListAdapter.getData(), position);
				}
			}
		});

	}

	public void asyncLoadData() {
		if (mTask != null) {
			mTask.execute();
			mTask = null;
		}
	}

	public void show() {
		mPosStack.clear();
		misShowProcess = true;
		if (mPath == null) {
			return;
		}

		mTask = new MediaLoaderTask(mPath, msSourceType);
		asyncLoadData();

		MediaGridView.this.setSelection(0);
	}

	private class MediaLoaderTask extends AsyncTask<Void, Void, List<Media>> {
		private List<Media> mMediaes;
		private String mSourceUries;
		private SourceType mSourceType;
		private long mTime = System.currentTimeMillis();

		private Collator mCollator = Collator.getInstance(Locale.CHINESE);
		private Comparator mMediaSort = new Comparator<Media>() {
			@Override
			public int compare(Media arg0, Media arg1) {
				boolean one = arg0.isCollection();
				boolean two = arg1.isCollection();
				return one == two ? mCollator.compare(arg0.getName(), arg1.getName()) : (one ? 1 : -1);
			}
		};

		MediaLoaderTask(String uries, SourceType sourceType) {
			mMediaes = new ArrayList<Media>();
			mSourceUries = uries;
			mSourceType = sourceType;
		}

		private List<Media> fetchMediasBySource(String root, final SourceType source) {
			if (System.currentTimeMillis() - mTime > 5000) {
				mTime = System.currentTimeMillis();
				Message msg = mHandler.obtainMessage();
				msg.what = UPDATE_UI;
				mHandler.sendMessage(msg);
			}
			root = root.replace("\\040", " ");
			List<Media> medias = new ArrayList<Media>();
			if (TextUtils.isEmpty(root))
				return medias;
			File rootFile = new File(root);
			if (rootFile.canRead() == false)
				return medias;
			File[] subFiles = null;
			subFiles = rootFile.listFiles(new FileFilter() {
				@Override
				public boolean accept(File subFile) {
					return subFile.isDirectory() || MediaUtils.checkMediaSource(subFile.getAbsolutePath(), source);
				}
			});
			List<Media> curMedias = new ArrayList<Media>();
			if (subFiles == null) {
				return medias;
			}
			for (File each : subFiles) {
				if (each.isDirectory()) {
					medias.addAll(fetchMediasBySource(each.getAbsolutePath(), source));
				} else {
					curMedias.add(genMediaByUri(each.getAbsolutePath(), source));
				}
			}
			if (curMedias.size() > 1) {
				Media media = new Media(source, "");
				media.setSubMedias(curMedias);
				medias.add(media);
			} else if (curMedias.size() == 1) {
				medias.add(curMedias.get(0));
			}
			Collections.sort(medias, mMediaSort);
			return medias;
		}

		private Media genMediaByUri(String uri, SourceType type) {
			if (MediaUtils.isImage(uri))
				return new Image(type, uri);
			if (MediaUtils.isVideo(uri))
				return new Video(type, uri);
			if (MediaUtils.isAudio(uri))
				return new Audio(type, uri);
			return new Media(type, uri);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mfirst==0) {
				if (misShowProcess) {
					showProgressBar(null);
				}
				misShowProcess = false;
			}
		}

		@Override
		protected void onPostExecute(List<Media> result) {
			super.onPostExecute(result);
			if (mfirst==0) {
				dismissProgressBar();
			}
			mListAdapter.bindData(result);
			setAdapter(mListAdapter);
			mfirst=1;
		}

		@Override
		protected List<Media> doInBackground(Void... params) {
			try {
				mMediaes.addAll(fetchMediasBySource(mSourceUries, mSourceType));
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
			isback=true;
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
			animateFoucs(child.getLeft() + v.getLeft(), child.getTop() + v.getTop(), child.getWidth(), child.getHeight());
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
}
