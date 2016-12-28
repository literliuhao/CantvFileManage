package com.cantv.media.center.ui.directory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cantv.media.R;
import com.cantv.media.center.constants.PicStretch;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;
import com.cantv.media.center.utils.DateUtil;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;

import java.util.Date;
import java.util.List;

@SuppressLint("NewApi")
public class MediaListItemView extends MediaItemView {
    private MediaPicView mImageView;
    private ImageView mPicImageView;
    private MediaPicView mBgView;
    private ImageView mFocusView;
    private TextView mTvName;
    private TextView mTvSize;
    private TextView mTvDate;
    private NumberDrawable mNumDrawable;
    private Media mMedia;
    private boolean isShow = false;
    private float mAnimateRate = 0;
    private AlphaAnimation mAnimation = null;
    private Transformation mDrawingTransform = new Transformation();
    private Context mContext;
    private List<String> usbRootPaths;

    public MediaListItemView(Context context) {
        this(context, null);
    }

    public MediaListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    @SuppressLint("ResourceAsColor")
    public MediaListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setPadding((int) getResources().getDimension(R.dimen.px40), 0, 0, 0);
        int relativeLayoutWidth = (int) getResources().getDimension(R.dimen.px1764);
        int relativeLayoutHeight = (int) getResources().getDimension(R.dimen.px220);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(relativeLayoutWidth, relativeLayoutHeight);
        relativeLayout.setLayoutParams(relativeLayoutParams);
        mContext = context;
        mNumDrawable = new NumberDrawable(context);
        setWillNotDraw(false);
        setFocusable(false);
        mFocusView = new ImageView(context);
        LayoutParams mFocusParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.px220));
        mFocusParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFocusParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mFocusView.setLayoutParams(mFocusParams);
        mBgView = new MediaPicView(context);
        mBgView.setId(R.id.bg_list_background);
        mBgView.setPicStretch(PicStretch.SCALE_CROP);
        LayoutParams mediaParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px200), (int) getResources().getDimension(R.dimen.px155));
        mediaParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mBgView.setLayoutParams(mediaParams);
        mImageView = new MediaPicView(context);

        mImageView.setPicStretch(PicStretch.SCALE_CROP);
        LayoutParams imageParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px160), (int) getResources().getDimension(R.dimen.px90));
        imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        imageParams.setMargins(FileUtil.dip2px(mContext, 14), 0, 0, 0);
        mImageView.setLayoutParams(imageParams);

        mPicImageView = new ImageView(context);
        LayoutParams picImageParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px160), (int) getResources().getDimension(R.dimen.px90));
        picImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        picImageParams.setMargins(FileUtil.dip2px(mContext, 14), 0, 0, 0);
        mPicImageView.setLayoutParams(picImageParams);

        mTvName = (TextView) LayoutInflater.from(context).inflate(R.layout.marquee_textview, null);
        mTvName.setId(R.id.bg_list_name);
        mTvName.setTextColor(getResources().getColorStateList(R.color.btn_selector));
        mTvName.setSingleLine(true);
        LayoutParams tvParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px1200), LayoutParams.WRAP_CONTENT);
        tvParams.addRule(RelativeLayout.RIGHT_OF, mBgView.getId());
        tvParams.setMargins((int) getResources().getDimension(R.dimen.px30), (int) getResources().getDimension(R.dimen.px60), 0, (int) getResources().getDimension(R.dimen.px10));
        mTvName.setLayoutParams(tvParams);
        mTvSize = new TextView(context);
        mTvSize.setId(R.id.bg_list_size);
        mTvSize.setTextColor(getResources().getColorStateList(R.color.list_item_font_selector));
        LayoutParams tvSizeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvSizeParams.addRule(RelativeLayout.RIGHT_OF, mBgView.getId());
        tvSizeParams.addRule(RelativeLayout.BELOW, mTvName.getId());
        tvSizeParams.setMargins((int) getResources().getDimension(R.dimen.px30), 0, 0, 0);
        mTvSize.setLayoutParams(tvSizeParams);
        mTvDate = new TextView(context);
        mTvDate.setTextSize(getResources().getDimension(R.dimen.px18));
        mTvDate.setTextColor(getResources().getColorStateList(R.color.list_item_font_selector));
        LayoutParams tvDateParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvDateParams.addRule(RelativeLayout.RIGHT_OF, mTvSize.getId());
        tvDateParams.addRule(RelativeLayout.BELOW, mTvName.getId());
        tvDateParams.addRule(RelativeLayout.ALIGN_BASELINE, mTvSize.getId());
        tvDateParams.setMargins((int) getResources().getDimension(R.dimen.px27), 0, 0, 0);
        mTvDate.setLayoutParams(tvDateParams);
        relativeLayout.addView(mFocusView);
        relativeLayout.addView(mBgView);
        relativeLayout.addView(mImageView);
        relativeLayout.addView(mPicImageView);
        relativeLayout.addView(mTvName);
        relativeLayout.addView(mTvSize);
        relativeLayout.addView(mTvDate);
        addView(relativeLayout);
    }

    public ImageView getFocusImage() {
        return mFocusView;
    }

    public void setMediaItem(final Media media, int position) {
        mMedia = media;
        mTvName.setText(media.getName());
        mTvDate.setVisibility(VISIBLE);
        // 当是文件类型,并且不是外接设备的根目录(根目录是默认1970时间,无意义)
        if ((mMedia.mType == SourceType.FOLDER) && !usbRootPaths.contains(mMedia.mUri)) {
            mTvSize.setVisibility(GONE);
        } else {
            mTvSize.setVisibility(VISIBLE);
            // 设置根目录大小
            if (usbRootPaths.contains(mMedia.mUri)) {
                mTvDate.setVisibility(GONE);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        String free = MediaUtils.getRealFreeSize(mMedia.mUri); // 可用大小
                        String total = MediaUtils.getRealTotalSize(mMedia.mUri); // 总大小
                        mTvSize.setText("总大小: " + total + "  可用大小: " + free);
                    }
                });
            } else {
                mTvSize.setText("大小: " + FileUtil.convertStorage(media.fileSize));
            }
        }
        mTvDate.setText("日期: " + DateUtil.onDate2String(new Date(media.modifiedDate), "yyyy.MM.dd HH:mm"));
        mNumDrawable.setNum(media.getSubMediasCount());
        LayoutParams mediaParams;
        switch (media.getMediaFormat()) {
            case IMAGE:

                String path = "";
                if (media.isSharing) {
                    path = media.sharePath;
                } else {
                    path = media.mUri;
                }
                MediaUtils.loadPicImg(mContext, path, mPicImageView);

                mBgView.setBackground(media);
                break;
            case AUDIO:
                mBgView.setBackground(media);
                break;
            case VIDEO:
                //第三方默认为开始视频截图
//                if (!media.isSharing) {
//                    mImageView.setMedia(media);
//                }
                //第三方默认为开始视频截图
                mBgView.setBackground(media);
                break;
            case APP:
                Drawable apkIcon = FileUtil.getApkIcon(mContext, media.mUri);
                mediaParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px165), (int) getResources().getDimension(R.dimen.px160));
                mediaParams.setMargins((int) getResources().getDimension(R.dimen.px10), 0, 0, 0);
                mediaParams.addRule(RelativeLayout.CENTER_VERTICAL);
                mBgView.setLayoutParams(mediaParams);
                mBgView.setBackground(media);
                if (null != apkIcon) {
                    mBgView.setDefaultPic(apkIcon);
                }
                break;
            case FOLDER:
                mediaParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px200), (int) getResources().getDimension(R.dimen.px155));
                mediaParams.addRule(RelativeLayout.CENTER_VERTICAL);
                mBgView.setLayoutParams(mediaParams);
                mBgView.setDefaultPic(R.drawable.folder_wj);
                break;
            case UNKNOW:
                mBgView.setDefaultPic(R.drawable.ic_other_filetype);
            default:
                break;
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        animateSelection(selected);
    }

    void animateView(final float from, final float to) {
        mAnimation = new AlphaAnimation(from, to);
        mAnimation.setDuration(1000);
        mAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isSelected())
                    animateView(to, from);
            }
        });
    }

    boolean needReDraw() {
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (mAnimation != null && mAnimation.hasEnded() == false) {
            if (mAnimation.hasStarted() == false)
                mAnimation.setStartTime(currentTime);
            mAnimation.getTransformation(currentTime, mDrawingTransform);
            mAnimateRate = mDrawingTransform.getAlpha();
            return true;
        }
        return false;
    }

    void animateSelection(boolean select) {
        if (select) {
            animateView(0, 1);
            isShow = true;
        } else {
            if (mAnimation != null) {
                mAnimation.cancel();
                mAnimation = null;
            }
            isShow = false;
        }
        mAnimateRate = 0;
        invalidate();
    }

    public void setUsbPaths(List<String> usbRootPas) {
        this.usbRootPaths = usbRootPas;
    }

    public List<String> getUsbPaths() {
        return usbRootPaths;
    }
}