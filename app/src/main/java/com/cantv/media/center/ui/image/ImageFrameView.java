package com.cantv.media.center.ui.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.cantv.media.center.activity.ImagePlayerActivity;
import com.cantv.media.center.app.MyApplication;
import com.cantv.media.center.ui.dialog.LoadingDialog;
import com.cantv.media.center.utils.SharedPreferenceUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SuppressLint("ResourceAsColor")
public class ImageFrameView extends FrameLayout {
    public Bitmap mBitmap = null;
    private final ImageView mImageView;
    private int mImgWidth;
    private int mImgHeight;
    private LoadingDialog mLoadingDialog;
    private NotifyParentUpdate mNotifyParentUpdate;
    private Context mContext;
    private ImagePlayerActivity mActivity;
    private int callbackW;
    private int callbackH;
    private String ShareUrl_FLAG = "http://";
    private int convertW = 0;
    private int convertH = 0;
    private int[] sizeArray = new int[2];
    private int MAX_HEIGHT = 4096;
    private int MAX_WIDTH = 4096;
    private int MAX_LENGTH = 4096;
    private int loadResourceReady = 0;
    private static final String TAG = "imageFrameView";
    private int loadError = 0;
    private boolean mIsShare;

    public ImageFrameView(Context context) {
        super(context);
        mContext = context;
        this.mActivity = (ImagePlayerActivity) context;
        mLoadingDialog = new LoadingDialog(mActivity);
        mImageView = new ImageView(context);
        addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    public interface onLoadingImgListener {
        void loadSuccess(boolean loadSuccess);

        void bitmapSize(int width, int height);

        void isFullScreen(boolean isFullScreen);

        void loadResourceReady(boolean isLoadReady);
    }

    private onLoadingImgListener mLoadingImgListener;

    public void playImage(final String imageUri, final boolean isSharing, String imageName, final Runnable onfinish, onLoadingImgListener loadingImgListener) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        this.mLoadingImgListener = loadingImgListener;
        this.mIsShare = isSharing;
        showProgressBar();
        if (isSharing) {
            mImageView.setVisibility(View.GONE);
        }
        loadResourceReady = 0;
        loadImage(imageUri, isSharing, imageName);
    }

    public int[] convertImage(float imageWidht, float imageHeight) {
        Log.i("convertImage", imageWidht + " " + imageHeight);
        if (imageWidht > MAX_WIDTH || imageHeight > MAX_HEIGHT) {
            float lenghtW = imageWidht - MAX_WIDTH;
            float lenghtH = imageHeight - MAX_HEIGHT;
            float cutNumber;
            if (lenghtW > lenghtH) {
                float calc = lenghtW / imageWidht;
                imageWidht -= lenghtW;
                cutNumber = imageHeight * calc;
                imageHeight -= cutNumber;
            } else if (lenghtH > lenghtW) {
                float calc = lenghtH / imageHeight;
                imageHeight -= lenghtH;
                cutNumber = imageWidht * calc;
                imageWidht -= cutNumber;

            }
        } else if (imageWidht < 0 || imageHeight < 0) {
            imageWidht = (int) mActivity.screenWidth;
            imageHeight = (int) mActivity.screenHeight;
        }
        Log.i("convertImage", imageWidht + " " + imageHeight);
        return new int[]{(int) imageWidht, (int) imageHeight};
    }

    public void loadImage(final String imageUri, boolean isSharing, String imageName) {
        Log.i("playImage", imageUri);
        //计算本地图片的实际宽高
        if (!isSharing) {
            getLocalImageSize(imageUri);
            convertW = callbackW;
            convertH = callbackH;
            //判断机型运行内存，重新赋值MAX_LENGTH,MAX_WIDTH
            Log.i(TAG, "deviceTotalMemory: " + getDeviceTotalMemory());
            int photoModel = SharedPreferenceUtil.getPhotoModel();
            if (photoModel == 1) {
                MAX_WIDTH = (int) mActivity.screenWidth;
                MAX_HEIGHT = (int) mActivity.screenHeight;
                sizeArray = convertImage(convertW, convertH);
                SharedPreferenceUtil.setPhotoModel(0);
                loadImageUsePicasso(imageUri);
            } else {
                if (getDeviceTotalMemory() > 1500) {
                    MAX_WIDTH = MAX_LENGTH;
                    MAX_HEIGHT = MAX_LENGTH;
                    sizeArray = convertImage(convertW, convertH);
                    //是否带有模糊效果，根据分辨率区分,是否是gif
                    if ((callbackW <= (int) mActivity.screenWidth && convertH <= (int) mActivity.screenHeight) || imageName.endsWith(".gif")) {
                        loadLocalGifNoThumbnail(imageUri);
                    } else {
                        loadLocalGif(imageUri);
                    }
                } else {
                    MAX_WIDTH = (int) mActivity.screenWidth;
                    MAX_HEIGHT = (int) mActivity.screenHeight;
                    sizeArray = convertImage(convertW, convertH);
                    //是否带有模糊效果，根据分辨率区分
                    if (callbackW <= (int) mActivity.screenWidth && convertH <= (int) mActivity.screenHeight) {
                        loadLocalImageNoThumbnail(imageUri);
                    } else {
                        loadLocalImage(imageUri);
                    }
                }
            }
        } else {
            //修复OS-3296进入文件共享，播放4K图片，出现文件管理停止运行，按确定键返回到文件管理，焦点异常，再进入文件共享焦点异常。
            loadNetImage(imageUri);
        }
    }

    /**
     * 使用picasso加载图片
     *
     * @param imageUri
     */
    private void loadImageUsePicasso(String imageUri) {
        Picasso.with(mContext).load(new File(imageUri)).into(mImageView, new Callback() {
            @Override
            public void onSuccess() {
                loadImageSuccessNoThumb(callbackW, callbackH);
            }

            @Override
            public void onError() {
                loadImageFail();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        if (!mIsShare) {
            width = sizeArray[0];
            height = sizeArray[1];
        } else {
            width = mImgWidth;
            height = mImgHeight;
        }
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

    public void setNotifyParentUpdateListner(NotifyParentUpdate listen) {
        mNotifyParentUpdate = listen;
    }

    public interface NotifyParentUpdate {
        void update();
    }

    /**
     * 获取本地图片尺寸信息
     *
     * @param imageUri
     */
    private void getLocalImageSize(String imageUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri, options);
        callbackH = options.outHeight;
        callbackW = options.outWidth;
        Log.i(TAG, "尺寸:" + callbackW + "*" + callbackH);
        if (null != bitmap) {
            bitmap.recycle();
        }
    }

    /**
     * 获取网络图片尺寸
     *
     * @param imageUri
     */
    private void getImageSize(String imageUri) {
        mImageView.setVisibility(View.VISIBLE);
        Glide.with(mActivity)
                .load(imageUri).asBitmap().thumbnail(0.1f)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        Log.i(TAG, "原始尺寸:" + width + "*" + height);
                    }
                });
    }

    /**
     * 加载网络图片
     *
     * @param imageUri
     */
    private void loadNetImage(final String imageUri) {
        Glide.with(mContext).load(imageUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).into(new SimpleTarget<Bitmap>((int) mActivity.screenWidth + 50, (int) mActivity.screenHeight + 50) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mImgWidth = resource.getWidth();
                mImgHeight = resource.getHeight();
                Log.i(TAG, "onResourceReady: " + mImgWidth + "*" + mImgHeight);
                Bitmap bitmap = null;
                if (mImgHeight != (int) mActivity.screenWidth && mImgWidth != (int) mActivity.screenHeight) {
                    bitmap = getBitmap(resource, (int) mActivity.screenWidth, (int) mActivity.screenHeight);
                }
                mBitmap = bitmap;
                mImageView.setImageBitmap(mBitmap);
                dismissProgressBar();
                mImageView.setVisibility(View.VISIBLE);
                if (null != mLoadingImgListener) {
                    mLoadingImgListener.loadSuccess(true);
                    mLoadingImgListener.bitmapSize(mImgWidth, mImgHeight);
                    mLoadingImgListener.loadResourceReady(true);
                    if (mImgHeight < (int) mActivity.screenHeight && mImgWidth < (int) mActivity.screenWidth) {
                        mLoadingImgListener.isFullScreen(false);
                    } else {
                        mLoadingImgListener.isFullScreen(true);
                    }
                }
                if (mBitmap != null && !mBitmap.isRecycled()) {
                    mBitmap = null;
                    resource = null;
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                //解决图片加载不出来的情况下，页面一直处于加载中
                //重试3次后弹出异常提示
                if (loadError >= 3) {
                    loadError = 0;
                    loadImageFail();
                } else {
                    loadNetImage(imageUri);
                    loadError++;
                }
            }
        });
    }

    /**
     * 加载本地图片
     *
     * @param imageUri
     */
    private void loadLocalImage(final String imageUri) {
        Glide.with(mContext).load(imageUri).asBitmap().thumbnail(0.1f).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                loadImageFail();
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                loadResourceReady += 1;
                loadImageSuccess(callbackW, callbackH);
                return false;
            }
        }).into(mImageView);
    }

    /**
     * 加载本地图片无模糊
     *
     * @param imageUri
     */
    private void loadLocalImageNoThumbnail(final String imageUri) {
        Glide.with(mContext).load(imageUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                loadImageFail();
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                loadImageSuccessNoThumb(callbackW, callbackH);
                return false;
            }
        }).into(mImageView);
    }

    /**
     * 压缩图片
     *
     * @param bitmap
     * @param screenWidth
     * @param screenHight
     * @return
     */
    public Bitmap getBitmap(Bitmap bitmap, int screenWidth, int screenHight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) screenWidth / w;
        //修复OS-2080 USB幻灯片播”测试图片“文件夹内图片,播放中出现"很抱歉,文件管理已停止运行"(内存溢出)
        float scale2 = (float) screenHight / h;
        scale = scale < scale2 ? scale : scale2;
        matrix.postScale(scale, scale);
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
            if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    /**
     * 获取系统总内存
     *
     * @return 单位：Byte
     */
    public static long getDeviceTotalMemory() {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l / 1024 / 1024;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 加载动态图片
     *
     * @param imageUri
     */
    private void loadLocalGif(final String imageUri) {
        Glide.with(mContext).load(imageUri).asBitmap().thumbnail(0.1f).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                loadImageFail();
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                loadResourceReady += 1;
                loadImageSuccess(sizeArray[0], sizeArray[1]);
                return false;
            }
        }).into(mImageView);
    }

    /**
     * 加载动态图片无模糊
     *
     * @param imageUri
     */
    private void loadLocalGifNoThumbnail(final String imageUri) {
        Glide.with(mContext).load(imageUri).crossFade(0).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                loadImageFail();
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                loadImageSuccessNoThumb(sizeArray[0], sizeArray[1]);
                return false;
            }
        }).into(mImageView);
    }

    /**
     * 本地加载图片失败
     */
    private void loadImageFail() {
        dismissProgressBar();
        if (null != mLoadingImgListener) {
            mLoadingImgListener.loadSuccess(false);
        }
    }

    /**
     * 本地图片加载成功无模糊
     *
     * @param callbackW
     * @param callbackH
     */
    private void loadImageSuccessNoThumb(int callbackW, int callbackH) {
        dismissProgressBar();
        if (null != mLoadingImgListener) {
            mLoadingImgListener.loadSuccess(true);
            mLoadingImgListener.bitmapSize(callbackW, callbackH);
            mLoadingImgListener.loadResourceReady(true);
            if (sizeArray[1] < (int) mActivity.screenHeight && sizeArray[0] < (int) mActivity.screenWidth) {
                mLoadingImgListener.isFullScreen(false);
            } else {
                mLoadingImgListener.isFullScreen(true);
            }
        }
    }

    /**
     * 本地图片加载成功
     *
     * @param width
     * @param height
     */
    private void loadImageSuccess(int width, int height) {
        dismissProgressBar();
        if (null != mLoadingImgListener) {
            mLoadingImgListener.loadSuccess(true);
            mLoadingImgListener.bitmapSize(width, height);
            mLoadingImgListener.loadResourceReady(loadResourceReady == 2 ? true : false);
            if (sizeArray[1] < (int) mActivity.screenHeight && sizeArray[0] < (int) mActivity.screenWidth) {
                mLoadingImgListener.isFullScreen(false);
            } else {
                mLoadingImgListener.isFullScreen(true);
            }
        }
    }
}
