package com.cantv.media.center.ui;

import java.io.File;

import com.app.core.sys.MainThread;
import com.cantv.media.center.utils.ImageUtils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

@SuppressLint("ResourceAsColor")
public class ImageFrameView extends FrameLayout {
	private Bitmap mBitmap = null;
	private final ImageView mImageView;
	private int mImgOrginWidth;
	private int mImgOrginHeight;
	private final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private final long SHOWPROCESS_FILE_SIZE = 50 * 1024 * 1024;
	private ProgressDialog mProgressDialog;
	private MediaImageViewLoaderTask mTask;
	private NotifyParentUpdate mNotifyParentUpdate;
	private int mfirst = 0;

	public ImageFrameView(Context context) {
		super(context);
		mProgressDialog = new ProgressDialog(context);
		WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
		mProgressDialog.getWindow().setGravity(Gravity.CENTER);
		params.x = 250;
		mProgressDialog.getWindow().setAttributes(params);
		mImageView = new ImageView(context);
		addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
	}

	public void playImage(final String imageUri, final Runnable onfinish) {
		mTask = new MediaImageViewLoaderTask(imageUri, onfinish);
		asyncLoadData();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = mImgOrginWidth;
		int height = mImgOrginHeight;
		Log.i("liujun4", "width====" + width + "---height====" + height);
		if (width == 0 || height == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			for (int i = 0; i < getChildCount(); i++) {
				measureChild(getChildAt(i), MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
			}
			setMeasuredDimension(width, height);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	private void showProgressBar(String message) {
		if (mProgressDialog.isShowing()) {
			return;
		}
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage(message == null ? "閸ュ墽澧栭崝鐘烘祰娑擄拷..." : message);
		mProgressDialog.show();
	}

	private void dismissProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	private void asyncLoadData() {
		if (mTask != null) {
			mTask.execute();
			mTask = null;
		}
	}

	private class MediaImageViewLoaderTask extends AsyncTask<Void, Void, Bitmap> {
		private final String mimageUri;
		private final Runnable monfinish;
		private int mImgWidth;
		private int mImgHeight;

		MediaImageViewLoaderTask(final String imageUri, final Runnable onfinish) {
			mimageUri = imageUri;
			monfinish = onfinish;
		}

		protected void onPreExecute() {
			 if (new File(mimageUri).length() > SHOWPROCESS_FILE_SIZE) {
			 showProgressBar(null);
			 }
			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap = null;
			}
		};

		protected void onPostExecute(Bitmap result) {
			 dismissProgressBar();
			mImgOrginWidth = mImgWidth;
			mImgOrginHeight = mImgHeight;
			mImageView.setImageBitmap(mBitmap);
			if (mNotifyParentUpdate != null) {
				mNotifyParentUpdate.update();
			}
			requestLayout();
		};

		@Override
		protected Bitmap doInBackground(Void... params) {
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;
				mBitmap = BitmapFactory.decodeFile(mimageUri, opt);
				mImgWidth = opt.outWidth;
				mImgHeight = opt.outHeight;
				opt.inJustDecodeBounds = false;
				if (new File(mimageUri).length() >= MAX_FILE_SIZE) {
					opt.inPurgeable = true;//设置为True时，表示系统内存不足时可以被回 收，设置为False时，表示不能被回收
					opt.inInputShareable = true;
					opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
				}
				// mBitmap = ImageUtils.createImageThumbnail(mimageUri);
				// mBitmap = BitmapFactory.decodeFile(mimageUri, opt);
				mBitmap = ImageUtils.createBitmap(mimageUri);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (monfinish != null) {
					monfinish.run();
				}
			}
			// mBitmap = ImageUtils.createBitmap(mimageUri);
			return mBitmap;
		}
	}

	public void setNotifyParentUpdateListner(NotifyParentUpdate listen) {
		mNotifyParentUpdate = listen;
	}

	public interface NotifyParentUpdate {
		void update();
	}

}
