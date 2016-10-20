package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
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
import com.cantv.media.center.data.Media;
import com.cantv.media.center.utils.FileUtil;
import com.cantv.media.center.utils.MediaUtils;

@SuppressLint("NewApi")
public class MediaGridItemView extends MediaItemView {
    private MediaPicView mImageView;
    private ImageView mPicImageView;
    private MediaPicView mBgView;
    private ImageView mFocusView;
    private TextView mTextView;
    private NumberDrawable mNumDrawable;
    private Media mMedia;
    private boolean isShow = false;
    private float mAnimateRate = 0;
    private AlphaAnimation mAnimation = null;
    private Transformation mDrawingTransform = new Transformation();
    private Context mContext;

    public MediaGridItemView(Context context) {
        this(context, null);
    }

    public MediaGridItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ResourceAsColor")
    public MediaGridItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        int relativeLayoutWidth = (int) getResources().getDimension(R.dimen.px280);
        int relativeLayoutHeight = (int) getResources().getDimension(R.dimen.px280);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(relativeLayoutWidth, relativeLayoutHeight);
        relativeLayoutParams.alignWithParent = true;
        relativeLayout.setLayoutParams(relativeLayoutParams);
        mContext = context;
        mNumDrawable = new NumberDrawable(context);
        setWillNotDraw(false);
        setFocusable(false);
        mFocusView = new ImageView(context);
        RelativeLayout.LayoutParams mFocusParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px280), (int) getResources().getDimension(R.dimen.px220));
        mFocusParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFocusParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mFocusView.setLayoutParams(mFocusParams);
        mBgView = new MediaPicView(context);
        mBgView.setId(R.id.bg_grid_background);
        mBgView.setPicStretch(PicStretch.SCALE_CROP);
        RelativeLayout.LayoutParams mediaParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px200), (int) getResources().getDimension(R.dimen.px155));
        mediaParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mediaParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mBgView.setLayoutParams(mediaParams);

        mImageView = new MediaPicView(context);

        mImageView.setPicStretch(PicStretch.SCALE_CROP);
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px160), (int) getResources().getDimension(R.dimen.px90));
        imageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mImageView.setLayoutParams(imageParams);

        mPicImageView = new ImageView(context);
        RelativeLayout.LayoutParams picImageParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px160), (int) getResources().getDimension(R.dimen.px90));
        picImageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        picImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mPicImageView.setLayoutParams(picImageParams);

        // 从xml里添加这个TextView是为了轮播的时候两边有渐变效果,动态添加则没有
        mTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.marquee_textview, null);
        mTextView.setTextColor(getResources().getColorStateList(R.color.btn_selector));
        mTextView.setTextSize(getResources().getDimension(R.dimen.px22));
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.addRule(RelativeLayout.BELOW, mBgView.getId());
        tvParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tvParams.setMargins(0, (int) getResources().getDimension(R.dimen.px18), 0, 0);
        mTextView.setLayoutParams(tvParams);
        relativeLayout.addView(mFocusView);
        relativeLayout.addView(mBgView);
        relativeLayout.addView(mImageView);
        relativeLayout.addView(mPicImageView);
        relativeLayout.addView(mTextView);
        addView(relativeLayout);
    }

    public ImageView getFocusImage() {
        return mFocusView;
    }

    public void setMediaItem(Media media, int position) {
        mMedia = media;
        mTextView.setText(media.getName());
        mNumDrawable.setNum(media.getSubMediasCount());
        LayoutParams mediaParams;
        switch (media.getMediaFormat()) {
            case IMAGE:
                // mImageView.setMedia(media);
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
//                if (!media.isSharing) {
//                    mImageView.setMedia(media);
//                }
                mBgView.setBackground(media);
                break;
            case APP:
                Drawable apkIcon = FileUtil.getApkIcon(mContext, media.mUri);
                mediaParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.px155), (int) getResources().getDimension(R.dimen.px155));
                mediaParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                mediaParams.addRule(RelativeLayout.CENTER_VERTICAL);
                mBgView.setLayoutParams(mediaParams);
                mBgView.setBackground(media);
                if (null != apkIcon) {
                    mBgView.setDefaultPic(apkIcon);
                }
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
        // ViewParent parent = getParent();
        // if (parent != null && parent instanceof ViewParent) {
        // ViewGroup group = (ViewGroup) parent;
        // selected = selected & group.hasFocus();
        // }
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

    @SuppressLint("ResourceAsColor")
    void animateSelection(boolean select) {
        if (select) {
            animateView(0, 1);
            isShow = true;
            mTextView.setEllipsize(TruncateAt.MARQUEE);
            mTextView.setHorizontallyScrolling(true);
            mTextView.setMarqueeRepeatLimit(-1);
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
}