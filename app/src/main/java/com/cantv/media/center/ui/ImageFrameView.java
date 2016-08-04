package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory.CacheDirectoryGetter;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cantv.media.R;
import com.cantv.media.center.activity.ImagePlayerActivity;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.utils.ImageUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("ResourceAsColor")
public class ImageFrameView extends FrameLayout {
	public Bitmap mBitmap = null;
	private final ImageView mImageView;
	private int mImgOrginWidth;
	private int mImgOrginHeight;
	private final long MAX_FILE_SIZE = 10 * 1024 * 1024;
	private final long SHOWPROCESS_FILE_SIZE = 50 * 1024 * 1024;
	private LoadingDialog mLoadingDialog;
	private MediaImageViewLoaderTask mTask;
	private NotifyParentUpdate mNotifyParentUpdate;
	private Context mContext;
	private ImagePlayerActivity mActivity;

	public ImageFrameView(Context context) {
		super(context);
		mContext = context;
		this.mActivity = (ImagePlayerActivity) context;
		mLoadingDialog = new LoadingDialog(mContext);
		mImageView = new ImageView(context);
		addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
	}

	public interface onLoadingImgListener {
		void loadSuccessed();

		void bitmapSize(int width, int height);

		void getSizeSuccessed(int width, int height);
	}

	private onLoadingImgListener mLoadingImgListener;

	public void playImage(final String imageUri, final Runnable onfinish, onLoadingImgListener loadingImgListener) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		this.mLoadingImgListener = loadingImgListener;
		showProgressBar();
		mImageView.setVisibility(View.GONE);

		Glide.with(mContext).load(imageUri).asBitmap().override((int) mActivity.screenWidth + 50, (int) mActivity.screenHeight + 50).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(new SimpleTarget<Bitmap>() {
			@Override
			public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
				mImgOrginWidth = resource.getWidth();
				mImgOrginHeight = resource.getHeight();

				mBitmap = resource;
				mImageView.setImageBitmap(mBitmap);
				dismissProgressBar();
				mImageView.setVisibility(View.VISIBLE);

				if (null != mLoadingImgListener) {
					mLoadingImgListener.loadSuccessed();
					mLoadingImgListener.bitmapSize(mImgOrginWidth, mImgOrginHeight);
				}

				if (mBitmap != null && !mBitmap.isRecycled()) {
					mBitmap = null;
					resource = null;
				}

				Glide.with(mContext).load(imageUri).asBitmap().into(new SimpleTarget<Bitmap>() {

					@Override
					public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {

						// int myWidth = arg0.getWidth();
						// int myHeiht = arg0.getHeight();
						if (null != mLoadingImgListener) {
							// mLoadingImgListener.loadSuccessed();
							mLoadingImgListener.getSizeSuccessed(arg0.getWidth(), arg0.getHeight());
						}
					}
				});
			}
		});
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

	private void showProgressBar() {
		if (mLoadingDialog.isShowing()) {
			return;
		}
		mLoadingDialog.show();
	}

	private void dismissProgressBar() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
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
				showProgressBar();
			}
			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap = null;
			}
		}

		;

		protected void onPostExecute(Bitmap result) {
			dismissProgressBar();
			mImgOrginWidth = mImgWidth;
			mImgOrginHeight = mImgHeight;
			mImageView.setImageBitmap(mBitmap);
			if (mNotifyParentUpdate != null) {
				mNotifyParentUpdate.update();
			}

			if (null != mLoadingImgListener) {
				mLoadingImgListener.loadSuccessed();
			}

			requestLayout();
		}

		;

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
					opt.inPurgeable = true;// 设置为True时，表示系统内存不足时可以被回
											// 收，设置为False时，表示不能被回收
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
