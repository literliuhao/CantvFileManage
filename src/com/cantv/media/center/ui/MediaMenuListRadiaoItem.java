package com.cantv.media.center.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.core.sys.MainThread;
import com.cantv.media.R;

@SuppressLint("NewApi")
public class MediaMenuListRadiaoItem extends LinearLayout {
	private Drawable mFocusDrawable;
	private TextView mTextView;
	private ImageView mImageView;
	private View mView;
	private float mAnimateRate = 0;
	private AlphaAnimation mAnimation = null;
	private Transformation mDrawingTransform = new Transformation();
	private LayoutInflater mLayoutInflater;
	public MediaMenuListRadiaoItem(Context context) {
		this(context, null);
	}

	public MediaMenuListRadiaoItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MediaMenuListRadiaoItem(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		mLayoutInflater = LayoutInflater.from(context);
		mView = mLayoutInflater.inflate(R.layout.media_list_video_item, null);
		mFocusDrawable = getResources().getDrawable(R.drawable.framfocus);
		mTextView = (TextView) mView.findViewById(R.id.txt_name);
		mImageView = (ImageView) mView.findViewById(R.id.iv_radio);
		ColorStateList csl = (ColorStateList) getResources().getColorStateList(
				R.color.white);
		if (csl != null) {
			mTextView.setTextColor(csl);
		}
		mTextView.setTextSize(getResources().getDimension(R.dimen.px56));
		addView(mView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

	}

	public void setText(String txt) {
		mTextView.setText(txt);
	}
	public ImageView getImageView(){
		return mImageView;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
	}
	

	@Override
	public void setSelected(boolean selected) {

		ViewParent parent = getParent();
		if (parent != null && parent instanceof ViewParent) {
			ViewGroup group = (ViewGroup) parent;
			selected = selected & group.hasFocus();
		}
		super.setSelected(selected);
		animateSelection(selected);
	}

	@SuppressLint("ResourceAsColor")
	private void animateSelection(boolean select) {
		if (select) {
			animateView(0, 1);
			ColorStateList csl = (ColorStateList) getResources()
					.getColorStateList(R.color.blue);
			if (csl != null) {
				mTextView.setTextColor(csl);
			}
			mTextView.setEllipsize(TruncateAt.MARQUEE);

			mTextView.setHorizontallyScrolling(true);
			mTextView.setMarqueeRepeatLimit(-1);
		
			mTextView.setTextSize(getResources().getDimension(R.dimen.px62));
			MainThread.runLater(new Runnable() {
				
				@Override
				public void run() {
					mImageView.setBackground(getResources().getDrawable(
							R.drawable.radiaofocus));
				}
			});

		} else {
			ColorStateList csl = (ColorStateList) getResources()
					.getColorStateList(R.color.white);
			if (csl != null) {
				mTextView.setTextColor(csl);
			}
			mTextView.setTextSize(getResources().getDimension(R.dimen.px56));
			mImageView.setBackground(getResources().getDrawable(
					R.drawable.radiaonomal));
			if (mAnimation != null) {
				mAnimation.cancel();
				mAnimation = null;
			}
		}
		invalidate();
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

}
