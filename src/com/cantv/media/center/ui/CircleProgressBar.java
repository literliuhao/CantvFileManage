package com.cantv.media.center.ui;

import com.cantv.media.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class CircleProgressBar extends View {

	private int mProgressUnReachWidth;
	private int mProgressUnReachColor;
	private int mProgressReachColor;// 进度条颜色
	private int mProgressReachWidth;// 进度条宽度

	private int mMaxStrokeWidth;
	private int mHalfOfStrokeWidthDiff;
	private int mProgressRadius;
	private int mCirclePointX;
	private int mCirclePointY;

	private int mMaxProgress;// 总进度
	private int mCurrProgress;// 当前进度
	private Paint mProgressPaint;
	private RectF mProgressRectF;
	private ValueAnimator anim;
	private int mDestProgress;

	public CircleProgressBar(Context context) {
		this(context, null);
	}

	public CircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
		mProgressUnReachWidth = attributes.getDimensionPixelSize(R.styleable.CircleProgressBar_bgBarWidth, 2);
		mProgressUnReachColor = attributes.getColor(R.styleable.CircleProgressBar_bgBarColor,
				Color.parseColor("#7FFFFFFF"));
		mProgressReachWidth = attributes.getDimensionPixelSize(R.styleable.CircleProgressBar_progressBarWidth, 7);
		mProgressReachColor = attributes.getColor(R.styleable.CircleProgressBar_progressBarColor,
				Color.parseColor("#32AEFD"));
		mMaxProgress = attributes.getInt(R.styleable.CircleProgressBar_maxProgress, 0);
		attributes.recycle();

		mProgressPaint = new Paint();
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setDither(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
		mProgressPaint.setStrokeJoin(Paint.Join.ROUND);

		mMaxStrokeWidth = Math.max(mProgressReachWidth, mProgressUnReachWidth);
		mHalfOfStrokeWidthDiff = (Math.abs(mProgressReachWidth - mProgressUnReachWidth)) / 2;

		mProgressRectF = new RectF();
		mProgressRectF.left = mMaxStrokeWidth - mHalfOfStrokeWidthDiff;
		mProgressRectF.top = mMaxStrokeWidth - mHalfOfStrokeWidthDiff;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int viewSize = Math.min(w, h) - getPaddingLeft() - getPaddingRight();
		mCirclePointX = viewSize / 2;
		mCirclePointY = mCirclePointX;
		mProgressRadius = mCirclePointX - mMaxStrokeWidth;
		int mProgressDiam = mProgressRadius * 2 + mProgressReachWidth;
		mProgressRectF.right = mProgressDiam + mHalfOfStrokeWidthDiff;
		mProgressRectF.bottom = mProgressDiam + mHalfOfStrokeWidthDiff;
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		mProgressPaint.setColor(mProgressUnReachColor);
		mProgressPaint.setStrokeWidth(mProgressUnReachWidth);
		canvas.drawCircle(mCirclePointX, mCirclePointY, mProgressRadius, mProgressPaint);

		if (mMaxProgress != 0 && mCurrProgress != 0) {
			mProgressPaint.setColor(mProgressReachColor);
			mProgressPaint.setStrokeWidth(mProgressReachWidth);
			float sweepAngle = mCurrProgress * 1.0f / mMaxProgress * 360;
			canvas.drawArc(mProgressRectF, -90, sweepAngle, false, mProgressPaint);
		}
	}

	public void setMax(int max) {
		mMaxProgress = max;
		invalidate();
	}

	public boolean setProgress(int progress) {
		if(mCurrProgress == progress){
			return false;
		}
		if (anim != null && anim.isStarted()) {
			mDestProgress = progress;
			return true;
		}
		anim = ValueAnimator.ofObject(new ProgressEvaluator(), mCurrProgress, progress);
		anim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mCurrProgress = (Integer) animation.getAnimatedValue();
				invalidate();
			}
		});
		anim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mDestProgress != 0) {
					setProgress(mDestProgress);
					mDestProgress = 0;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		anim.setInterpolator(new LinearInterpolator());
		anim.setDuration(500);
		anim.start();
		return true;
	}

	public class ProgressEvaluator implements TypeEvaluator<Integer> {

		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			return (int) (startValue + (endValue - startValue) * fraction);
		}
	}
}
