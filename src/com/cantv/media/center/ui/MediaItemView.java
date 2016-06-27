package com.cantv.media.center.ui;

import com.cantv.media.R;
import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.PicStretch;
import com.cantv.media.center.data.Media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MediaItemView extends FrameLayout {
	private MediaPicView mImageView;
	private MediaPicView mBgView;
	private ImageView mFocusView;
	private TextView mTextView;
	private Drawable mPlayFocusDrawable;
	private NumberDrawable mNumDrawable;
	private Media mMedia;
	private boolean isShow = false;
	//private Drawable mFocusDrawable;
	private float mAnimateRate = 0;
	private AlphaAnimation mAnimation = null;
	private Transformation mDrawingTransform = new Transformation();

	public MediaItemView(Context context) {
		this(context, null);
	}

	public MediaItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("ResourceAsColor")
	public MediaItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//mFocusDrawable = getResources().getDrawable(R.drawable.focus);
		mPlayFocusDrawable = getResources().getDrawable(R.drawable.videoplaynomal);
		mNumDrawable = new NumberDrawable(context);
		setWillNotDraw(false);
		setFocusable(false);
		setPadding(0, 0, 0, 0);
		this.setForegroundGravity(Gravity.CENTER);
		mFocusView = new ImageView(context);
		mBgView = new MediaPicView(context);
		mBgView.setPicStretch(PicStretch.SCALE_FILL);
		mBgView.setPadding(10, 10, 10, 10);
		mImageView = new MediaPicView(context);
		mImageView.setPicStretch(PicStretch.SCALE_FILL);
		mImageView.setPadding(20, 48, 0, 0);
		mTextView = new TextView(context);
		mTextView.setTextColor(getResources().getColorStateList(R.color.btn_selector));
		mTextView.setTextSize(getResources().getDimension(R.dimen.px32));
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setPadding(0, 212, 0, 0);
		mTextView.setSingleLine(true);
		float focusWidth = getResources().getDimension(R.dimen.px220);
		float focusHeight = getResources().getDimension(R.dimen.px188);
		float bgWidth = getResources().getDimension(R.dimen.px200);
		float bgHeight = getResources().getDimension(R.dimen.px168);
		float imgWidth = getResources().getDimension(R.dimen.px160);
		float imgHeight = getResources().getDimension(R.dimen.px90);
		float textWidth = getResources().getDimension(R.dimen.px300);
		addView(mFocusView, new LayoutParams((int) focusWidth, (int) focusHeight));
		addView(mBgView, new LayoutParams((int) bgWidth, (int) bgHeight));
		addView(mImageView, new LayoutParams((int) imgWidth, (int) imgHeight));
		addView(mTextView, new LayoutParams((int) textWidth, LayoutParams.WRAP_CONTENT));
	}

	public ImageView getFocusImage() {
		return mFocusView;
	}

	public void setMediaItem(Media media) {
		mMedia = media;
		mTextView.setText(media.getName());
		mNumDrawable.setNum(media.getSubMediasCount());
		switch (media.getMediaFormat()) {
		case IMAGE:
			mBgView.setMedia(media);
			mBgView.setDefaultPic(R.drawable.folder_wj);
			break;
		case AUDIO:
			mBgView.setBackground(media);
			break;
		case VIDEO:
			mImageView.setMedia(media);
			mBgView.setBackground(media);			
			break;
		case UNKNOW:
			mBgView.setMedia(media);
			mBgView.setDefaultPic(R.drawable.folder_wj);
		default:
			break;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		//mFocusDrawable.setBounds(mFocusView.getLeft() - 22, mFocusView.getTop() - 22, mFocusView.getRight() + 22, mFocusView.getBottom() + 22);
		//mFocusDrawable.setAlpha(Math.round(mAnimateRate * 255));
		//mFocusDrawable.draw(canvas);
		super.draw(canvas);
		int top = mFocusView.getTop() + 10;
		int left = mFocusView.getRight() - mNumDrawable.getIntrinsicWidth() - 5;
		mNumDrawable.setBounds(left, top, left + mNumDrawable.getIntrinsicWidth(), top + mNumDrawable.getIntrinsicHeight());
		mNumDrawable.draw(canvas);
		if (isShow) {
			if (!mMedia.isCollection()) {
				if (mMedia.getMediaFormat() == MediaFormat.VIDEO || mMedia.getMediaFormat() == MediaFormat.AUDIO) {
					int l = mFocusView.getLeft() + (mFocusView.getWidth() - mPlayFocusDrawable.getIntrinsicWidth()) / 2;
					int t = mFocusView.getTop() + (mFocusView.getHeight() - mPlayFocusDrawable.getIntrinsicHeight()) / 2;
					int r = l + mPlayFocusDrawable.getIntrinsicWidth();
					int b = t + mPlayFocusDrawable.getIntrinsicHeight();
					mPlayFocusDrawable.setBounds(l, t, r, b);
					mPlayFocusDrawable.draw(canvas);
				}
			}
		}
		if (needReDraw()) {
			invalidate();
		}
	}

	@Override
	public void setSelected(boolean selected) {
//		ViewParent parent = getParent();
//		if (parent != null && parent instanceof ViewParent) {
//			ViewGroup group = (ViewGroup) parent;
//			selected = selected & group.hasFocus();
//		}
		super.setSelected(selected);
		animateSelection(selected);

	}

	private void animateView(final float from, final float to) {
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

	private boolean needReDraw() {
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

	@SuppressLint("ResourceAsColor")
	private void animateSelection(boolean select) {
		if (select) {
			animateView(0, 1);
			isShow = true;
			mTextView.setEllipsize(TruncateAt.MARQUEE);
			mTextView.setHorizontallyScrolling(true);
			mTextView.setMarqueeRepeatLimit(-1);
			//mPlayFocusDrawable = getResources().getDrawable(R.drawable.play);
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
