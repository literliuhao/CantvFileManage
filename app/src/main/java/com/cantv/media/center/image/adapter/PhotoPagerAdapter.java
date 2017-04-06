package com.cantv.media.center.image.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cantv.media.R;
import com.cantv.media.center.image.OnLoadingImageListener;
import com.cantv.media.center.image.ImageActivity;
import com.cantv.media.center.data.ImageBean;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.image.ImageBrowser;
import com.cantv.media.center.image.ImageFrameLayoutView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenpx on 2017/2/24 0024.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private static final String TAG = "PhotoPagerAdapter";
    private ImageActivity mContext;
    private List<Media> mPagerList;
    private OnLoadingImageListener mOnLoadingImageListener;
    private View mCurrentPagerView;
    private int mCurrentPosition;
    public List<ImageBean> mImageList;
    private ImageBean mImageBean;

    public PhotoPagerAdapter(Context context, List<Media> mDataList) {
        super();
        mContext = (ImageActivity) context;
        mPagerList = mDataList;
        mImageList = new ArrayList<>(10);
    }

    public void setOnLoadingImageListener(OnLoadingImageListener listener) {
        this.mOnLoadingImageListener = listener;
    }

    @Override
    public int getCount() {
        return mPagerList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mCurrentPagerView = (View) object;
        mCurrentPosition = position;
        Log.i(TAG, "setPrimaryItem: " + position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        Log.i(TAG, "instantiateItem: " + position + "***" + position % mPagerList.size());
        View mView = View.inflate(mContext, R.layout.item_imagepager, null);
        ImageBrowser imageBrowser = (ImageBrowser) mView.findViewById(R.id.iv_image);
        final TextView loadingFail = (TextView) mView.findViewById(R.id.tv_loadingfail);
        loadingFail.setVisibility(View.INVISIBLE);
        ImageFrameLayoutView imageFrameLayoutView = new ImageFrameLayoutView(mContext);
        imageBrowser.setContentImageView(imageFrameLayoutView);
        imageBrowser.setFocusable(false);
        imageBrowser.layoutOriginal();
        String url = mPagerList.get(position).isSharing ? mPagerList.get(position).sharePath : mPagerList.get(position).mUri;
        boolean isSharing = mPagerList.get(position).isSharing;
        String imageName = mPagerList.get(position).mName;
        imageFrameLayoutView.playImage(url, isSharing, imageName, position, new OnLoadingImageListener() {
            @Override
            public void loadingImageSuccess(boolean loadSuccess, int position) {
                if (null != mOnLoadingImageListener) {
                    mOnLoadingImageListener.loadingImageSuccess(loadSuccess, position);
                }
                if (!loadSuccess) {
                    mImageBean = new ImageBean(0, 0, position, false, true);
                    mImageList.add(mImageBean);
                    loadingFail.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void loadingImageFail(boolean loadFail, int position) {
                if (null != mOnLoadingImageListener) {
                    mOnLoadingImageListener.loadingImageFail(loadFail, position);
                }
            }

            @Override
            public void isScaled(boolean isScaled, int position) {
                if (null != mOnLoadingImageListener) {
                    mOnLoadingImageListener.isScaled(isScaled, position);
                }
            }

            @Override
            public void loadingImageReady(boolean isLoadReady, int position) {
                if (null != mOnLoadingImageListener) {
                    mOnLoadingImageListener.loadingImageReady(isLoadReady, position);
                }
            }

            @Override
            public void getImageSize(int width, int height, boolean isScaled, int position) {
                mImageBean = new ImageBean(width, height, position, true, isScaled);
                mImageList.add(mImageBean);
                if (null != mOnLoadingImageListener) {
                    mOnLoadingImageListener.getImageSize(width, height, isScaled, position);
                }
            }
        });
        container.addView(mView);

        return mView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        Log.i(TAG, "destroyItem: " + position);
        for (int i = mImageList.size() - 1; i >= 0; i--) {
            ImageBean imageBean = mImageList.get(i);
            if (position == imageBean.getPosition()) {
                mImageList.remove(i);
            }
        }
        container.removeView((View) object);
    }

    //===================================================================================
    public View getCurrentPagerView() {
        return mCurrentPagerView;
    }

    public int getCurrentPosition() {
        return mCurrentPosition % mPagerList.size();
    }

}
