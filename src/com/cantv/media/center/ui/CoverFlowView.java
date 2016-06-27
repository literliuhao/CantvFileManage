package com.cantv.media.center.ui;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;

import com.app.core.sys.MainThread;
import com.app.core.ui.ClickGesture;
import com.app.core.ui.Scrollable;
import com.app.core.ui.ViewGesture;
import com.app.core.utils.UiUtils;

public class CoverFlowView extends GalleryView {
	// ### 值域 ###
	private static final int AUTO_SCROLL_INTERVAL = 500;
	private static final int AUTO_SCROLL = 0x1;
	private boolean mPause = false;
	private static final int ANIM_DURATION = 500;
	private static final int MIN_FLIP_VELOCITY = 30; // dip
	private int[][] mOrder;
	private static final float SCALEDOWN_GRAVITY_TOP = 0.0f;
	private static final float SCALEDOWN_GRAVITY_CENTER = 0.5f;
	private static final float SCALEDOWN_GRAVITY_BOTTOM = 1.0f;
	private final Camera mCamera = new Camera();
	private boolean isScroll = true;
	public static int defaultindex = -1;
	/**
	 * 缩放中心
	 */
	private final float mScaleDownGravity = SCALEDOWN_GRAVITY_CENTER;
	private OnClickListener mClickListener = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (isScroll) {
				if (msg.what == AUTO_SCROLL && mPause == false) {
					Log.i("liujun44", "defaultindex===" + defaultindex);
					scrollToNext(defaultindex - 1);
					isScroll = false;
					// scheduleNextScroll();
				}
			}
		}
	};
	private int mFirstIndex = -1;

	// ### 接口 ###
	public static interface OnClickListener {
		void onClick(CoverFlowView view, View childView);
	}

	// ### 构造函数 ###
	public CoverFlowView(Context context) {
		this(context, null);
	}

	public CoverFlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipToPadding(false);
		int dip15 = UiUtils.dip2px(getContext(), 25);
		setPadding(0, dip15, 0, dip15);
		setChildrenDrawingOrderEnabled(true);
		setStaticTransformationsEnabled(true);
		setOnGalleryScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(Scrollable scrollable, ScrollState oldState, ScrollState newState) {
				if (newState != ScrollState.IDLE) {
					pauseAutoScroll();
				} else {
					resumeAutoScoll();
				}
			}

			@Override
			public void onScroll(Scrollable scrollable, boolean viewportChanged) {
				if (viewportChanged) {
					invalidate();
				}
			}
		});

		getScrollDetector().pushGesture(new CoverFlowClickGesture());
	}

	// ### 方法 ###
	public void startScroll() {
		resumeAutoScoll();
	}

	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}

	public void setFirstCenterIndex(int index) {
		mFirstIndex = index;
	}

	// ### 重载方法 ###
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		pauseAutoScroll();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (visibility == View.VISIBLE) {
			resumeAutoScoll();
		} else {
			pauseAutoScroll();
		}
	}

	@Override
	protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
		LayoutParams lp = (LayoutParams) child.getLayoutParams();
		lp.width = getDefaultPicWidth();

		super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (changed) {
			if (getChildCount() > 0) {
				final int nextIndex = mFirstIndex >= 0 ? mFirstIndex : getNextShowingChildIndex();
				final View nextView = getChildAt(nextIndex);
				if (nextView != null) {
					scrollBy((int) ((nextView.getLeft() + nextView.getRight()) / 2f - getViewportBounds().exactCenterX()), 0);
					// 重置自动滚动的开始时间
					pauseAutoScroll();
					resumeAutoScoll();
				}
			}
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if (i == 0) {
			// 根据offset生成绘制顺序
			mOrder = new int[childCount][2];
			for (int j = 0; j < childCount; j++) {
				mOrder[j][0] = j;
				mOrder[j][1] = Math.abs(getChildOffset(getChildAt(j)));
			}
			int temp = 0;
			for (int j = mOrder.length - 1; j > 0; --j) {
				for (int k = 0; k < j; ++k) {
					if (mOrder[k + 1][1] > mOrder[k][1]) {
						temp = mOrder[k + 1][0];
						mOrder[k + 1][0] = mOrder[k][0];
						mOrder[k][0] = temp;

						temp = mOrder[k + 1][1];
						mOrder[k + 1][1] = mOrder[k][1];
						mOrder[k][1] = temp;
					}
				}
			}
		}
		return mOrder[i][0];
	}

	/**
	 * 最大偏转角度
	 */
	private final int mMaxRotation = 25;
	/**
	 * 缩放值(0,1),1为不缩放
	 */
	private final float mScale = 0.6f;

	@Override
	protected boolean getChildStaticTransformation(View item, Transformation t) {
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			item.invalidate();
		}

		final int coverFlowWidth = getViewportBounds().width();
		final int coverFlowCenter = getViewportBounds().centerX();
		final int childWidth = item.getWidth();
		final int childHeight = item.getHeight();
		final int childCenter = item.getLeft() + childWidth / 2;

		final int actionDistance = (int) ((coverFlowWidth + childWidth) / 2.0f);
		final int distance = childCenter - coverFlowCenter;
		final float effectsAmount = Math.min(1.0f, Math.max(-1.0f, (1.0f / actionDistance * distance)));

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		final Matrix imageMatrix = t.getMatrix();

		// rotation.
		if (mMaxRotation != 0) {
			float rotationAngle = (float) (-Math.signum(effectsAmount) * Math.pow(Math.abs(effectsAmount), 0.7) * mMaxRotation);
			mCamera.save();
			mCamera.rotateY(rotationAngle);
			mCamera.getMatrix(imageMatrix);
			mCamera.restore();
		}

		// Zoom and offset.
		if (mScale != 1) {
			final float zoomAmount = (mScale - 1) * Math.abs(effectsAmount) + 1;
			final float translateX = childWidth / 2.0f;
			final float translateY = childHeight * mScaleDownGravity;
			imageMatrix.preTranslate(-translateX, -translateY);
			imageMatrix.postScale(zoomAmount, zoomAmount);
			imageMatrix.postTranslate(translateX, translateY);
			final float xOffset = (float) (-Math.signum(effectsAmount) * Math.pow(Math.abs(effectsAmount * 0.8), 1.4) / 0.8 * childWidth);
			imageMatrix.postTranslate(xOffset, 0);
		}

		return true;
	}

	// ### 实现函数 ###
	@Override
	protected Scroller newScroller() {
		return new Scroller();
	}

	public void scrollToNext(int distance) {
		if (getChildCount() > 0) {
			int showingIndex = getNextShowingChildIndex();
			final int nextIndex = (showingIndex + (distance % getChildCount()) + getChildCount()) % getChildCount();
			final View nextView = getChildAt(nextIndex);
			if (nextView != null) {
				scrollBy((int) ((nextView.getLeft() + nextView.getRight()) / 2f - getViewportBounds().exactCenterX()), 0);
				// scrollSmoothlyBy((int) ((nextView.getLeft() +
				// nextView.getRight()) / 2f -
				// getViewportBounds().exactCenterX()), 0, ANIM_DURATION, null,
				// null);
			}
		}
	}

	public void scheduleNextScroll() {
		MainThread.runLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mHandler.hasMessages(AUTO_SCROLL) == false) {
					mHandler.sendEmptyMessageDelayed(AUTO_SCROLL, AUTO_SCROLL_INTERVAL);
				}
			}
		}, 1000);
	}

	private void pauseAutoScroll() {
		mPause = true;
		mHandler.removeMessages(AUTO_SCROLL);
	}

	private void resumeAutoScoll() {
		mPause = false;
		scheduleNextScroll();
	}

	private int getChildOffset(View child) {
		return (int) getChildOffsetF(child);
	}

	private float getChildOffsetF(View child) {
		final float coverFlowCenter = getViewportBounds().exactCenterX();
		final float childWidth = child.getWidth();
		final float childCenter = child.getLeft() + childWidth / 2f;
		return childCenter - coverFlowCenter;
	}

	private int getNextShowingChildIndex() {
		int nextIndex = -1;
		float minDistance = Float.MAX_VALUE;
		for (int i = 0; i < getChildCount(); i++) {
			float distance = Math.abs(getChildOffsetF(getChildAt(i)));
			if (distance < minDistance) {
				minDistance = distance;
				nextIndex = i;
			}
		}
		return nextIndex;
	}

	private final float mPicWidthInScreen = 0.334f;

	private int getDefaultPicWidth() {
		// TODO 能拿到父亲宽度就好了, 不过这里传进来的widthMode=MeasureSpec.UNSPECIFIED
		return Math.round((Math.max(UiUtils.getScreenWidth(getContext()), UiUtils.getScreenHeight(getContext())) - getPaddingLeft() - getPaddingRight()) * mPicWidthInScreen);
	}

	// ### 内部类 ###
	private class Scroller extends GalleryView.Scroller {
		private int mCurrentIndex = -1;

		@Override
		protected void onDragStart(ScrollState prevState, float startX, float startY) {
			super.onDragStart(prevState, startX, startY);
			mCurrentIndex = getNextShowingChildIndex();
		}

		@Override
		protected void slide(float vx, float vy, Runnable onFinish, Runnable onCancel) {
			if (getChildCount() < 1) {
				super.slide(vx, vy, onFinish, onCancel);
				return;
			}

			int nextIndex = -1;
			if (Float.compare(Math.abs(vx), UiUtils.dip2px(getContext(), MIN_FLIP_VELOCITY)) >= 0) {
				// 根据投掷速度方向, 决定下一个要显示的子视图.
				int[] sequence = getChildSequence();
				int showingIndex = getNextShowingChildIndex();
				if (mCurrentIndex == showingIndex) {
					if (vx > 0) {
						for (int i = 0; i < sequence.length; i++) {
							if (sequence[i] == showingIndex) {
								nextIndex = sequence[(i + 1) % getChildCount()];
								break;
							}
						}
					} else {
						for (int i = 0; i < sequence.length; i++) {
							if (sequence[i] == showingIndex) {
								nextIndex = sequence[(i - 1 + getChildCount()) % getChildCount()];
								break;
							}
						}
					}
				}
			}

			if (nextIndex == -1) {
				nextIndex = getNextShowingChildIndex();
			}

			final View nextView = getChildAt(nextIndex);
			if (nextView != null) {
				super.slide((nextView.getLeft() + nextView.getRight()) / 2f - getViewportBounds().exactCenterX(), 0, ANIM_DURATION, false, onFinish, onCancel);
			}
		}
	}

	private class CoverFlowClickGesture extends ViewGesture {
		protected final ClickGesture mClickGesture = new ClickGesture();

		@Override
		protected void doRestart(View v, boolean reset) {
			mClickGesture.restart(v, reset || !mClickGesture.keepDetecting());
		}

		@Override
		protected void doDetect(View v, MotionEvent m, boolean delayed, GestureListener listener) {
			mClickGesture.detect(v, m, delayed, new ClickGesture.GestureListener() {
				@Override
				public void onTouchUp(View v, PointF point) {

				}

				@Override
				public void onTouchDown(View v, PointF point) {

				}

				@Override
				public void onTouchCancel(View v, PointF point) {

				}

				@Override
				public void onClick(ViewGesture g, View v, PointF clickPoint, int tapCount) {
					float viewWidth = v.getWidth();
					float viewHeight = v.getHeight();

					RectF center = new RectF(0.0f, 0.0f, viewWidth, viewHeight);
					center.inset((viewWidth - getDefaultPicWidth()) / 2f, 0);
					if (center.contains(clickPoint.x, clickPoint.y)) {
						final int nextIndex = getNextShowingChildIndex();
						final View nextView = getChildAt(nextIndex);
						if (mClickListener != null && nextView != null) {
							mClickListener.onClick(CoverFlowView.this, ((CellView) nextView).getChildAt(0));
						}
						keepDetecting(false);
						return;
					}

					if (clickPoint.x < center.left) {
						scrollToNext(-1);
						skipNextDetecting(true);

					} else if (clickPoint.x > center.right) {
						scrollToNext(1);
						skipNextDetecting(true);
					}
					keepDetecting(false);
				}
			});
		}
	}
}
