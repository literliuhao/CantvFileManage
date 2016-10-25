package com.cantv.media.center.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.cantv.media.R;

import java.util.Formatter;
import java.util.Locale;

public class TimeProgressBar extends View {

	private final int DEF_BG_STROKE_WIDTH = 1;//dp
	private final int DEF_BG_STROKE_COLOR = Color.parseColor("#80FFFFFF");
	private final int DEF_PROGRESSBAR_HEIGHT = 10;// dp
	private final int DEF_PROGRESS_COLOR = Color.parseColor("#D9019dd4");
	private final int DEF_SECOND_PROGRESS_COLOR = Color.parseColor("#4D1EADDF");
	private final int DEF_TEXT_SIZE = 21;// dp
	private final int DEF_TEXT_COLOR = Color.parseColor("#CCFFFFFF");
	private final int DEF_TIMESTAMP_MARGIN = 10;// dp
	private final int DEF_CONTENT_PADDING_LEFT = 7;// dp
	private final int DEF_CONTENT_PADDING_TOP = 3;// dp
	private final int DEF_CONTENT_PADDING_RIGHT = 7;// dp
	private final int DEF_CONTENT_PADDING_BOTTOM = 3;// dp

	private int mBgStrokeWidth;
	private int mBgStrokeColor;
	private int mProgressBarHeight;
	private int mProgressColor;
	private int mSecondProgColor;
	private int mTextSize;
	private int mTextColor;
	private int mTimeLabelMargin;// 时间标签bottomMargin
	private Bitmap mTimestramBgBmp;
	private NinePatch mTimeLabelBg;
	private int mContentPaddingLeft;
	private int mContentPaddingTop;
	private int mContentPaddingRight;
	private int mContentPaddingBottom;
	private int mDividerWidth;
	private int mDividerColor;

	private Paint mPaint;
	private Formatter mFormatter;
	private StringBuilder mFormatBuilder;

	private float mTimeTextHeight;
	private float mTimeLabelWidth;
	private float mTimeLabelHeight;
	private RectF mTimeLabelRectF;
	private float mTimeLabelX;

	private float mCornerRadius;
	private float mBgStrokeHalfWidth;
	private RectF mBgRect;
	private float mBarOffsetX;
	private float mBarOffsetY;
	private Path mBarClipPath;
	private float mProgressBarWidth;
	private float mProgressX;
	private float mSecondProgX;

	private int mProgress;
	private int mDuration;
	private boolean mSecondProgressEnable;
	private int mSecondProgress;
	private String mTimeStr;
	private float mTimeTextWidth;
	


	public TimeProgressBar(Context context) {
		this(context, null, 0);
	}

	public TimeProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("deprecation")
	public TimeProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// init attrs
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeProgressBar);
		mBgStrokeWidth = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_bg_stroke_width, (int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_BG_STROKE_WIDTH, getResources().getDisplayMetrics()));
		mBgStrokeColor = ta.getColor(R.styleable.TimeProgressBar_bg_stroke_color, DEF_BG_STROKE_COLOR);
		mProgressBarHeight = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_progressbar_height,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_PROGRESSBAR_HEIGHT,
						getResources().getDisplayMetrics()));
		mProgressColor = ta.getColor(R.styleable.TimeProgressBar_progress_color, DEF_PROGRESS_COLOR);
		mSecondProgColor = ta.getColor(R.styleable.TimeProgressBar_second_progress_color, DEF_SECOND_PROGRESS_COLOR);
		mTextSize = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_text_size, (int) TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_TEXT_SIZE, getResources().getDisplayMetrics()));
		mTextColor = ta.getColor(R.styleable.TimeProgressBar_text_color, DEF_TEXT_COLOR);
		mDividerWidth = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_divider_width, 2);
		mDividerColor = ta.getColor(R.styleable.TimeProgressBar_divider_color,
				getResources().getColor(android.R.color.transparent));
		int timestampBgRes = ta.getResourceId(R.styleable.TimeProgressBar_timelabel_bg, 0);
		mTimeLabelMargin = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_timelabel_margin_bottom,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_TIMESTAMP_MARGIN,
						getResources().getDisplayMetrics()));
		mContentPaddingLeft = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_timelabel_padding_left,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_CONTENT_PADDING_LEFT,
						getResources().getDisplayMetrics()));
		mContentPaddingTop = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_timelabel_padding_top,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_CONTENT_PADDING_TOP,
						getResources().getDisplayMetrics()));
		mContentPaddingRight = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_timelabel_padding_right,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_CONTENT_PADDING_RIGHT,
						getResources().getDisplayMetrics()));
		mContentPaddingBottom = ta.getDimensionPixelSize(R.styleable.TimeProgressBar_timelabel_padding_bottom,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, DEF_CONTENT_PADDING_BOTTOM,
						getResources().getDisplayMetrics()));
		ta.recycle();

		// init paint
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setTextSize(mTextSize);
		mPaint.setStrokeWidth(mBgStrokeWidth);
		mPaint.setTextAlign(Align.CENTER);

		// init global params
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
		mTimeTextHeight = mPaint.descent() - mPaint.ascent();
		mCornerRadius = (mProgressBarHeight + mBgStrokeWidth) / 2;
		mBgStrokeHalfWidth = mBgStrokeWidth / 2;
		mBarOffsetX = (mPaint.measureText("00:00:00") + mContentPaddingLeft + mContentPaddingRight) / 2;

		mBarClipPath = new Path();
		mBgRect = new RectF();

		mTimeLabelRectF = new RectF();
		if (timestampBgRes != 0) {
			mTimestramBgBmp = BitmapFactory.decodeResource(getResources(), timestampBgRes);
			if (mTimestramBgBmp != null) {
				byte[] ninePatchChunk = mTimestramBgBmp.getNinePatchChunk();
				if (NinePatch.isNinePatchChunk(ninePatchChunk)) {
					mTimeLabelBg = new NinePatch(mTimestramBgBmp, ninePatchChunk);
				}
			}
		}
	}

	private void refreshParamsWithProgress(int progress, int secondProgress) {
		mTimeStr = time2String(mSecondProgressEnable ? secondProgress : progress);
		Log.i("", "mTimeStr = " + mTimeStr);
		mTimeTextWidth = mPaint.measureText(mTimeStr);
		mTimeLabelWidth = mTimeTextWidth + mContentPaddingLeft + mContentPaddingRight;
		mTimeLabelHeight = mTimeTextHeight + mContentPaddingTop + mContentPaddingBottom;

		mBarOffsetY = mTimeLabelHeight + mTimeLabelMargin;

		if (mDuration == 0) {
			mProgressX = 0;
			mSecondProgX = 0;
		} else {
			if (mSecondProgressEnable) {
				if (progress <= secondProgress) {
					mProgressX = progress * mProgressBarWidth / mDuration;
					mSecondProgX = secondProgress * mProgressBarWidth / mDuration;
					mTimeLabelX = mSecondProgX;
				} else {
					mProgressX = secondProgress * mProgressBarWidth / mDuration;
					mSecondProgX = progress * mProgressBarWidth / mDuration;
					mTimeLabelX = mProgressX;
				}
			} else {
				mProgressX = progress * mProgressBarWidth / mDuration;
				mTimeLabelX = mProgressX;
			}
		}
		mTimeLabelX += mBarOffsetX;

		float imeLabelHalfWidth = mTimeLabelWidth / 2;
		mTimeLabelRectF.left = -imeLabelHalfWidth;
		mTimeLabelRectF.right = imeLabelHalfWidth;
		mTimeLabelRectF.bottom = mTimeLabelHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode == MeasureSpec.EXACTLY) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			int reqHeight = (int) (mProgressBarHeight + mTimeLabelHeight + mTimeLabelMargin);
			if (heightMode == MeasureSpec.AT_MOST) {
				reqHeight = resolveSize(reqHeight, heightMeasureSpec);
			}
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(reqHeight, MeasureSpec.EXACTLY));
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		refreshParamsWithProgress(mProgress, mSecondProgress);
		mProgressBarWidth = w - mBarOffsetX * 2;
		RectF barClipRect = new RectF();
		barClipRect.bottom = mProgressBarHeight;
		barClipRect.right = mProgressBarWidth;
		mBarClipPath.reset();
		mBarClipPath.addRoundRect(barClipRect, mCornerRadius, mCornerRadius, Direction.CW);

		mBgRect.left = mBgStrokeHalfWidth;
		mBgRect.top = mBgStrokeHalfWidth;
		mBgRect.right = mProgressBarWidth - mBgStrokeHalfWidth;
		mBgRect.bottom = mProgressBarHeight - mBgStrokeHalfWidth;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.save();
		canvas.translate(mBarOffsetX, mBarOffsetY);
		canvas.clipPath(mBarClipPath);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
		// draw bg
		mPaint.setColor(mBgStrokeColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mBgStrokeWidth);
		canvas.drawRoundRect(mBgRect, mCornerRadius, mCornerRadius, mPaint);
		// draw progress
		mPaint.setColor(mProgressColor);
		mPaint.setStyle(Paint.Style.FILL);
		canvas.drawRect(0, 0, mProgressX, mProgressBarHeight, mPaint);
		if (mSecondProgressEnable) {
			// draw second progress
			mPaint.setColor(mSecondProgColor);
			canvas.drawRect(mProgressX, 0, mSecondProgX, mProgressBarHeight, mPaint);
		} else {
			// draw divider line
			mPaint.setColor(mDividerColor);
			mPaint.setStrokeWidth(mDividerWidth);
			canvas.drawLine(mProgressX - mBgStrokeHalfWidth, 0, mProgressX - mBgStrokeHalfWidth, getHeight(), mPaint);
		}
		canvas.restore();

		// draw time label bg
		if (mSecondProgressEnable) {
			canvas.translate(mTimeLabelX, 0);
			if (mTimeLabelBg != null) {
				mTimeLabelBg.draw(canvas, mTimeLabelRectF);
			} else if (mTimestramBgBmp != null) {
				canvas.drawBitmap(mTimestramBgBmp, null, mTimeLabelRectF, mPaint);
			}
			// draw timestamp;
			mPaint.setColor(mTextColor);
			canvas.drawText(mTimeStr, 0, mContentPaddingTop - mPaint.ascent(), mPaint);
		}
	}

	protected String time2String(int timeInMillis) {
		long totalSeconds = timeInMillis / 1000;
		long seconds = totalSeconds % 60;
		long minutes = (totalSeconds / 60) % 60;
		long hours = totalSeconds / 3600;
		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	public void setProgress(int progress) {
		if (mDuration == 0) {
			return;
		}
		mProgress = progress < 0 ? 0 : (progress > mDuration ? mDuration : progress);
		refreshParamsWithProgress(progress, mSecondProgress);
		invalidate();
	}

	public void setSecondProgress(int secondProgress) {
		if (mDuration == 0) {
			return;
		}
		mSecondProgress = secondProgress < 0 ? 0 : (secondProgress > mDuration ? mDuration : secondProgress);
		refreshParamsWithProgress(mProgress, secondProgress);
		invalidate();
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	public int getDuration() {
		return mDuration;
	}

	public int getProgress() {
		return mProgress;
	}

	public int getSecondProgress() {
		return mSecondProgress;
	}	
	public void setSecondProgressEnable(boolean enable) {
		mSecondProgressEnable = enable;
	}
	
	
}
