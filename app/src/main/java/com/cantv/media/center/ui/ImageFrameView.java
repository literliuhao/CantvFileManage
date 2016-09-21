package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cantv.media.center.activity.ImagePlayerActivity;
import com.cantv.media.center.utils.ImageUtils;

import java.io.File;

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
        loadImage(imageUri);
    }

    public void loadImage(final String imageUri) {
        Log.i("playImage", imageUri);
        //加50是为了防止刚好是屏幕的整数倍,出现获取处理后的图片宽高正好和屏幕的宽高相同而出现不能缩放(也有可能碰到是加完后数据的整数倍)
        Glide.with(mContext).load(imageUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).into(new SimpleTarget<Bitmap>((int) mActivity.screenWidth + 50, (int) mActivity.screenHeight + 50) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Bitmap bitmap = getBitmap(resource, (int) mActivity.screenWidth, (int) mActivity.screenHeight);
                mImgOrginWidth = resource.getWidth();
                mImgOrginHeight = resource.getHeight();
                mBitmap = bitmap;
                mImageView.setImageBitmap(mBitmap);
                dismissProgressBar();
                mImageView.setVisibility(View.VISIBLE);

                if (null != mLoadingImgListener) {
                    mLoadingImgListener.loadSuccessed();
//                    //大图处理过的图片经过比率进行缩小后无法获取实际尺寸,所以经过二次获取(有内存溢出风险)
//                    if ((mImgOrginWidth >= (int) mActivity.screenWidth) && (mImgOrginHeight >= (int) mActivity.screenHeight)) {
//                        getRealWH(imageUri, mLoadingImgListener);
//                    } else {
                    mLoadingImgListener.bitmapSize(resource.getWidth(), resource.getHeight());
//                    }
                }

                if (mBitmap != null && !mBitmap.isRecycled()) {
                    mBitmap = null;
                    resource = null;
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                Log.i("playImage", "onLoadFailed......................");
                loadImage(imageUri);
            }
        });
    }

    /**
     * 获取大于屏幕尺寸图片的实际尺寸
     *
     * @param imageUri
     * @param mLoadingImgListener
     */
    private void getRealWH(String imageUri, final onLoadingImgListener mLoadingImgListener) {

        Glide.with(mContext).load(imageUri).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                mLoadingImgListener.bitmapSize(bitmap.getWidth(), bitmap.getHeight());
                Log.w("W / H ", bitmap.getWidth() + "   " + bitmap.getHeight());
                if (null != bitmap) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        });
    }

    public static Bitmap getBitmap(Bitmap bitmap, int screenWidth, int screenHight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) screenWidth / w;
        matrix.postScale(scale, scale);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mImgOrginWidth;
        int height = mImgOrginHeight;
//        Log.i("liujun4", "width====" + width + "---height====" + height);
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
