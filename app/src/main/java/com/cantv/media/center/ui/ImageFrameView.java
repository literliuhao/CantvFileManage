package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cantv.media.center.activity.ImagePlayerActivity;

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
    private final int MAX_LENGHT = 4096;

    public ImageFrameView(Context context) {
        super(context);
        mContext = context;
        this.mActivity = (ImagePlayerActivity) context;
        mLoadingDialog = new LoadingDialog(mActivity);
        mImageView = new ImageView(context);
        addView(mImageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    public interface onLoadingImgListener {
        void loadSuccessed(boolean loadSuccessed);

        void bitmapSize(int width, int height);

        void getSizeSuccessed(int width, int height);
    }

    private onLoadingImgListener mLoadingImgListener;

    public void playImage(final String imageUri, final boolean isSharing, final Runnable onfinish, onLoadingImgListener loadingImgListener) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        this.mLoadingImgListener = loadingImgListener;
        showProgressBar();
        mImageView.setVisibility(View.GONE);
        loadImage(imageUri);
    }

    public int[] convertImage(float imageWidht, float imageHeight) {
        Log.i("convertImage", imageWidht + " " + imageHeight);
        if (imageWidht > MAX_LENGHT || imageHeight > MAX_LENGHT) {
            float lenghtW = imageWidht - MAX_LENGHT;
            float lenghtH = imageHeight - MAX_LENGHT;
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
        }else if (imageWidht < 0 || imageHeight < 0) {
            imageWidht = (int) mActivity.screenWidth;
            imageHeight = (int) mActivity.screenHeight;
        }
        Log.i("convertImage", imageWidht + " " + imageHeight);
        return new int[]{(int) imageWidht, (int) imageHeight};
    }

    public void loadImage(final String imageUri) {
        Log.i("playImage", imageUri);
        //计算本地图片的实际宽高
        if (!imageUri.startsWith(ShareUrl_FLAG)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri, options);
            callbackH = options.outHeight;
            callbackW = options.outWidth;
            if (null != bitmap) {
                bitmap.recycle();
            }
        }

        convertW = callbackW;
        convertH = callbackH;
        sizeArray = convertImage(convertW, convertH);

        mImageView.setVisibility(View.VISIBLE);
        Glide.with(mContext).load(imageUri).asBitmap().thumbnail(0.1f).diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true).override(sizeArray[0], sizeArray[1]).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                dismissProgressBar();
                if (null != mLoadingImgListener) {
                    mLoadingImgListener.loadSuccessed(false);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                dismissProgressBar();
                mImgWidth = bitmap.getWidth();
                mImgHeight = bitmap.getHeight();
                if (null != mLoadingImgListener) {
                    mLoadingImgListener.loadSuccessed(true);
                    mLoadingImgListener.bitmapSize(imageUri.startsWith(ShareUrl_FLAG) ? mImgWidth : sizeArray[0], imageUri.startsWith(ShareUrl_FLAG) ? mImgHeight : sizeArray[1]);
                }
                return false;
            }
        }).into(mImageView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = sizeArray[0];
        int height = sizeArray[1];
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

}
