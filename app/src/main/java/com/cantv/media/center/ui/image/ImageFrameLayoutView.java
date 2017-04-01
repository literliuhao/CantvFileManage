package com.cantv.media.center.ui.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.cantv.media.center.Listener.OnLoadingImageListener;
import com.cantv.media.center.activity.ImageActivity;
import com.cantv.media.center.ui.dialog.LoadingDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SuppressLint("ResourceAsColor")
public class ImageFrameLayoutView extends FrameLayout {
    public Bitmap mBitmap = null;
    private ImageView mImageView;
    private int mImgWidth;
    private int mImgHeight;
    private LoadingDialog mLoadingDialog;
    private NotifyParentUpdate mNotifyParentUpdate;
    private Context mContext;
    private ImageActivity mActivity;
    private int callbackW = 1;
    private int callbackH = 1;
    //private String ShareUrl_FLAG = "http://";
    private int convertW = 1;
    private int convertH = 1;
    private int[] sizeArray = new int[2];
    private int MAX_HEIGHT = 4096;
    private int MAX_WIDTH = 4096;
    private int loadResourceReady = 0;
    private static final String TAG = "imageFrameView";
    private int loadError = 0;
    private boolean mIsShare;
    private OnLoadingImageListener mLoadingImgListener;
    private long mDeviceTotalMemory;
    private boolean isBluetooth;

    public ImageFrameLayoutView(Context context) {
        super(context);
        mContext = context;
        this.mActivity = (ImageActivity) context;
        mLoadingDialog = new LoadingDialog(mActivity);
        mImageView = new ImageView(context);
        addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    public void playImage(final String imageUri, final boolean isSharing, String imageName, int position, OnLoadingImageListener loadingImgListener) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        this.mLoadingImgListener = loadingImgListener;
        this.mIsShare = isSharing;
        loadResourceReady = 0;
        loadImage(imageUri, isSharing, imageName, position);
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

    private void loadImage(final String imageUri, boolean isSharing, String imageName, final int position) {
        Log.i("playImage", imageUri);
        //修复OS-4134手机通过蓝牙传输图片至电视，未传输完成前在终端打开该图片，仅显示已传输的部分，传输完成并刷新后仍只显示此前传输的部分
        String path = "/storage/emulated/0/bluetooth";
        isBluetooth = imageUri.contains(path);
        if (!isSharing) {
            getImageFile(imageUri);
            getLocalImageSize(imageUri);
            convertW = callbackW;
            convertH = callbackH;
            //判断机型运行内存，重新赋值MAX_LENGTH,MAX_WIDTH
            Log.i(TAG, "deviceTotalMemory: " + getDeviceTotalMemory());
            MAX_WIDTH = (int) mActivity.screenWidth;
            MAX_HEIGHT = (int) mActivity.screenHeight;
            sizeArray = convertImage(convertW, convertH);
            mDeviceTotalMemory = getDeviceTotalMemory();
            //是否加载gif，是否带有模糊效果
            if (imageName.endsWith(".gif") && mDeviceTotalMemory > 1800) {
                loadLocalGifNoThumbnail(imageUri, position);
            } else if (callbackW <= (int) mActivity.screenWidth && convertH <= (int) mActivity.screenHeight) {
                loadLocalImageNoThumbnail(imageUri, position);
            } else {
                loadLocalImage(imageUri, position);
            }
        } else {
            loadNetImage(imageUri, position);
        }
    }

    /**
     * 通过路径判断图片文件是否存在
     *
     * @param imageUri
     */
    private void getImageFile(final String imageUri) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                File imageFile = new File(imageUri);
                if (!imageFile.exists()) {
                    mActivity.finish();
                    return;
                }
            }
        }, 0);
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
        Log.i(TAG, "onMeasure: " + width + "*" + height);
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
        if (null != mLoadingDialog) {
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
     * 加载网络图片
     *
     * @param imageUri
     * @param position
     */
    private void loadNetImage(final String imageUri, final int position) {
        Glide.with(mActivity).load(imageUri).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).into(new SimpleTarget<Bitmap>((int) (mActivity.screenWidth + 50), (int) (mActivity.screenHeight + 50)) {
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
                if (null != mLoadingImgListener) {
                    mLoadingImgListener.loadingImageSuccess(true, position);
                    mLoadingImgListener.loadingImageReady(true, position);
                    if (mImgHeight <= (int) mActivity.screenHeight && mImgWidth <= (int) mActivity.screenWidth) {
                        mLoadingImgListener.getImageSize(mImgWidth, mImgHeight, false, position);
                    } else {
                        mLoadingImgListener.getImageSize(mImgWidth, mImgHeight, true, position);
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
                    loadImageFail(position);
                } else {
                    loadNetImage(imageUri, position);
                    loadError++;
                }
            }

        });
    }

    /**
     * 加载本地图片
     *
     * @param imageUri
     * @param position
     */
    private void loadLocalImage(final String imageUri, final int position) {
        Glide.with(mActivity).load(imageUri).asBitmap().thumbnail(0.1f).diskCacheStrategy(getStrategy()).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                loadImageFail(position);
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                loadResourceReady += 1;
                Log.i(TAG, "onResourceReady: " + bitmap.getWidth() + "*" + bitmap.getHeight());
                loadImageSuccess(callbackW, callbackH, bitmap, position);
                return false;
            }
        }).into(mImageView);
    }

    /**
     * 加载本地图片无模糊
     *
     * @param imageUri
     * @param position
     */
    private void loadLocalImageNoThumbnail(final String imageUri, final int position) {
        Glide.with(mActivity).load(imageUri).asBitmap().diskCacheStrategy(getStrategy()).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                loadImageFail(position);
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                loadImageSuccessNoThumb(callbackW, callbackH, position);
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
                //loadImageFail(position);
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                loadResourceReady += 1;
                Log.i(TAG, "onResourceReady: " + bitmap.getWidth() + "*" + bitmap.getHeight());
                //loadImageSuccess(sizeArray[0], sizeArray[1], bitmap, position);
                return false;
            }
        }).into(mImageView);
    }

    /**
     * 加载动态图片无模糊
     *
     * @param imageUri
     * @param position
     */
    private void loadLocalGifNoThumbnail(final String imageUri, final int position) {
        Glide.with(mActivity).load(imageUri).crossFade(0).diskCacheStrategy(getStrategy()).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                loadImageFail(position);
                Log.i(TAG, "onException: " + s);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                loadImageSuccessNoThumb(callbackW, callbackH, position);
                return false;
            }
        }).into(mImageView);
    }

    @NonNull
    private DiskCacheStrategy getStrategy() {
        return isBluetooth ? DiskCacheStrategy.NONE : (mDeviceTotalMemory > 1800 ? DiskCacheStrategy.ALL : DiskCacheStrategy.SOURCE);
    }

    /**
     * 本地加载图片失败
     *
     * @param position
     */
    private void loadImageFail(int position) {
        if (null != mLoadingImgListener) {
            mLoadingImgListener.loadingImageSuccess(false, position);
        }
    }

    /**
     * 本地图片加载成功无模糊
     *
     * @param callbackW
     * @param callbackH
     * @param position
     */
    private void loadImageSuccessNoThumb(int callbackW, int callbackH, int position) {
        if (null != mLoadingImgListener) {
            mLoadingImgListener.loadingImageSuccess(true, position);
            mLoadingImgListener.loadingImageReady(true, position);
            getImageListener(callbackW, callbackH, position);
        }
    }

    /**
     * 本地图片加载成功
     *
     * @param width
     * @param height
     * @param position
     */
    private void loadImageSuccess(int width, int height, Bitmap bitmap, int position) {
        if (null != mLoadingImgListener) {
            mLoadingImgListener.loadingImageSuccess(true, position);
            if (loadResourceReady == 2) {
                mLoadingImgListener.loadingImageReady(true, position);
            } else {
                if (bitmap.getWidth() >= mActivity.screenWidth - 50 || bitmap.getHeight() >= mActivity.screenHeight - 50) {
                    mLoadingImgListener.loadingImageReady(true, position);
                } else {
                    mLoadingImgListener.loadingImageReady(false, position);
                }
            }
            getImageListener(width, height, position);

        }
    }

    //传递尺寸
    private void getImageListener(int width, int height, int position) {
        if (height < (int) mActivity.screenHeight && width < (int) mActivity.screenWidth) {
            mLoadingImgListener.getImageSize(width, height, false, position);
        } else {
            mLoadingImgListener.getImageSize(width, height, true, position);
        }
    }
}
