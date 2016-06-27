package com.cantv.media.center.ui;

import com.cantv.media.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ×ÖÄ»²Ëµ¥
 * 
 * @author liujun
 * 
 */

@SuppressLint("NewApi")
public class MenuItemView extends LinearLayout {
	private Drawable mFocusDrawable;
	private TextView mTextView;
	private float mAnimateRate = 0;
	private AlphaAnimation mAnimation = null;

	public MenuItemView(Context context) {
		this(context, null);
	}

	public MenuItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MenuItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		mFocusDrawable = getResources().getDrawable(R.drawable.framfocus);
		mTextView = new TextView(context);
		mTextView.setPadding(15, 15, 0, 15);
		ColorStateList csl = (ColorStateList) getResources().getColorStateList(R.color.white);
		if (csl != null) {
			mTextView.setTextColor(csl);
		}
		mTextView.setTextSize(getResources().getDimension(R.dimen.px36));
		addView(mTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	public void setText(String txt) {
		mTextView.setText(txt);
	}

	@Override
	public void draw(Canvas canvas) {
		// mFocusDrawable.setBounds(mTextView.getLeft(), mTextView.getTop(),
		// mTextView.getRight(), mTextView.getBottom());
		// mFocusDrawable.setAlpha(Math.round(mAnimateRate * 255));
		// mFocusDrawable.draw(canvas);
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
			ColorStateList csl = (ColorStateList) getResources().getColorStateList(R.color.blue);
			if (csl != null) {
				mTextView.setTextColor(csl);
			}
			mAnimateRate = 1;
			mTextView.setTextSize(getResources().getDimension(R.dimen.px42));

		} else {
			ColorStateList csl = (ColorStateList) getResources().getColorStateList(R.color.white);
			if (csl != null) {
				mTextView.setTextColor(csl);
			}
			mAnimateRate = 0;
			mTextView.setTextSize(getResources().getDimension(R.dimen.px36));
			if (mAnimation != null) {
				mAnimation.cancel();
				mAnimation = null;
			}
		}
		invalidate();
	}

}
