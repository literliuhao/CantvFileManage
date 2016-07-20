package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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
    private MediaPicView mBgView;
    private ImageView mFocusView;
    private TextView mTvName;
    private TextView mTvSize;
    private TextView mTvDate;
    private Drawable mPlayFocusDrawable;
    private NumberDrawable mNumDrawable;
    private Media mMedia;
    private boolean isShow = false;
    // private Drawable mFocusDrawable;
    private float mAnimateRate = 0;
    private AlphaAnimation mAnimation = null;
    private Transformation mDrawingTransform = new Transformation();
    private Context mContext;

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
        relativeLayout.setPadding(35, 0, 0, 0);
        int relativeLayoutWidth = (int) getResources().getDimension(R.dimen.px1764);
        int relativeLayoutHeight = (int) getResources().getDimension(R.dimen.px200);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(relativeLayoutWidth, relativeLayoutHeight);
        relativeLayout.setLayoutParams(relativeLayoutParams);

        mContext = context;
        mPlayFocusDrawable = getResources().getDrawable(R.drawable.videoplaynomal);
        mNumDrawable = new NumberDrawable(context);
        setWillNotDraw(false);
        setFocusable(false);

        mFocusView = new ImageView(context);
        LayoutParams mFocusParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.px200));
        mFocusParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFocusParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mFocusView.setLayoutParams(mFocusParams);

        mBgView = new MediaPicView(context);
        mBgView.setId(0x559584);
        mBgView.setPicStretch(PicStretch.SCALE_CROP);
        LayoutParams mediaParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px200), (int) getResources().getDimension(R.dimen.px168));
        mediaParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mBgView.setLayoutParams(mediaParams);

        mImageView = new MediaPicView(context);
        mImageView.setPicStretch(PicStretch.SCALE_CROP);
        LayoutParams imageParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px160), (int) getResources().getDimension(R.dimen.px90));
        imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        // imageParams.setMargins(20,63,0,0);
//		imageParams.setMargins(FileUtil.dip2px(mContext, 14),
//				FileUtil.dip2px(mContext, 42), 0, 0);
        imageParams.setMargins(FileUtil.dip2px(mContext, 14), 0, 0, 0);
        mImageView.setLayoutParams(imageParams);

        mTvName = new TextView(context);
        mTvName.setId(0x559586);
        mTvName.setTextColor(getResources().getColorStateList(R.color.btn_selector));
        mTvName.setTextSize(getResources().getDimension(R.dimen.px24));
        mTvName.setSingleLine(true);
        LayoutParams tvParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.addRule(RelativeLayout.RIGHT_OF, mBgView.getId());
        tvParams.setMargins(30, 60, 0, 10);
        mTvName.setLayoutParams(tvParams);

        mTvSize = new TextView(context);
        mTvSize.setId(0x559585);
        mTvSize.setTextColor(getResources().getColorStateList(R.color.list_item_font_selector));
        mTvSize.setTextSize(getResources().getDimension(R.dimen.px18));
        LayoutParams tvSizeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvSizeParams.addRule(RelativeLayout.RIGHT_OF, mBgView.getId());
        tvSizeParams.addRule(RelativeLayout.BELOW, mTvName.getId());
        tvSizeParams.setMargins(30, 0, 0, 0);
        mTvSize.setLayoutParams(tvSizeParams);

        mTvDate = new TextView(context);
        mTvDate.setTextSize(getResources().getDimension(R.dimen.px18));
        mTvDate.setTextColor(getResources().getColorStateList(R.color.list_item_font_selector));
        LayoutParams tvDateParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvDateParams.addRule(RelativeLayout.RIGHT_OF, mTvSize.getId());
        tvDateParams.addRule(RelativeLayout.BELOW, mTvName.getId());
        tvDateParams.addRule(RelativeLayout.ALIGN_BASELINE, mTvSize.getId());
        tvDateParams.setMargins(27, 0, 0, 0);
        mTvDate.setLayoutParams(tvDateParams);

        relativeLayout.addView(mFocusView);
        relativeLayout.addView(mBgView);
        relativeLayout.addView(mImageView);
        relativeLayout.addView(mTvName);
        relativeLayout.addView(mTvSize);
        relativeLayout.addView(mTvDate);
        addView(relativeLayout);
    }

    public ImageView getFocusImage() {
        return mFocusView;
    }

    public void setMediaItem(Media media) {
        mMedia = media;
        mTvName.setText(media.getName());

        List<String> usbRootPaths = MediaUtils.getUsbRootPaths();

        mTvDate.setVisibility(VISIBLE);
        // 当是文件类型,并且不是外接设备的根目录(根目录是默认1970时间,无意义)
        if ((mMedia.mType == SourceType.FOLDER) && !usbRootPaths.contains(mMedia.mUri)) {
            mTvSize.setVisibility(GONE);
        } else {
            mTvSize.setVisibility(VISIBLE);

            // 设置根目录大小
            if (usbRootPaths.contains(mMedia.mUri)) {
                mTvDate.setVisibility(GONE);
                String free = MediaUtils.getFree(mMedia.mUri); // 可用大小
                String total = MediaUtils.getTotal(mMedia.mUri); // 总大小
                mTvSize.setText("总大小: " + total + "  可用大小: " + free);
            } else {
                mTvSize.setText("大小: " + MediaUtils.fileLength(media.getFileSize()));
            }
        }
        mTvDate.setText("日期: " + DateUtil.onDate2String(new Date(media.modifiedDate), "yyyy.MM.dd HH:mm"));
        mNumDrawable.setNum(media.getSubMediasCount());
        switch (media.getMediaFormat()) {
            case IMAGE:
                mImageView.setMedia(media);
                mBgView.setBackground(media);
                break;
            case AUDIO:
                mBgView.setBackground(media);
                break;
            case VIDEO:
                mImageView.setMedia(media);
                mBgView.setBackground(media);
                break;
            case APP:
                Drawable apkIcon = FileUtil.getApkIcon(mContext, media.mUri);
                mBgView.setBackground(media);
                mBgView.setDefaultPic(apkIcon);
                break;
            case FOLDER:
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
                if (isSelected()) animateView(to, from);
            }
        });
    }

    boolean needReDraw() {
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        if (mAnimation != null && mAnimation.hasEnded() == false) {
            if (mAnimation.hasStarted() == false) mAnimation.setStartTime(currentTime);
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
            // mTvName.setEllipsize(TruncateAt.MARQUEE);
            // mTvName.setHorizontallyScrolling(true);
            // mTvName.setMarqueeRepeatLimit(-1);
            // mPlayFocusDrawable = getResources().getDrawable(R.drawable.play);
        } else {
            if (mAnimation != null) {
                mAnimation.cancel();
                mAnimation = null;
            }
            isShow = false;
            mPlayFocusDrawable = getResources().getDrawable(R.drawable.videoplaynomal);
        }
        mAnimateRate = 0;
        invalidate();
    }

}