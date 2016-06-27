package com.cantv.media.center.ui;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import com.app.core.ui.FrameScrollView;
import com.app.core.ui.Scrollable;
import com.app.core.utils.UiUtils;

public class GalleryView extends FrameScrollView {
	private OnScrollListener mOnScrollListener = null;
	private Rect mLastViewport = new Rect();
	
	// ### 内部类 ###
	protected class CellView extends FrameLayout {
		public CellView(Context context) {
			super(context);
		}
	}
	protected class Scroller extends FrameScrollView.Scroller {
		@Override
		public void setContentWidth(int width) {
			super.setContentWidth(0);
		}
		@Override
		public void setContentBounds(int left, int top, int right, int bottom) {
			super.setContentBounds(- Integer.MAX_VALUE / 2, top, Integer.MAX_VALUE / 2, bottom);
		}
	}

	// ### 构造函数 ###
	public GalleryView(Context context) {
		this(context, null);
	}
	public GalleryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		setThumbEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(Scrollable scrollable,
					ScrollState oldState, ScrollState newState) {
				
				if (mOnScrollListener != null) {
					mOnScrollListener.onScrollStateChanged(scrollable, oldState, newState);
				}
			}
			@Override
			public void onScroll(Scrollable scrollable, boolean viewportChanged) {
				if (viewportChanged) {
					if (mLastViewport.isEmpty()) {
						mLastViewport.set(getViewportBounds());
					}
					
					offsetItem();
					mLastViewport.set(getViewportBounds());
				}
				
				if (mOnScrollListener != null) {
					mOnScrollListener.onScroll(scrollable, viewportChanged);
				}
			}
		});
	}
	
	// ### 方法 ###
	public void setOnGalleryScrollListener(OnScrollListener listener) {
		mOnScrollListener = listener;
	}
	public void addItemView(View child) {
		CellView childView = new CellView(getContext());
		childView.addView(child);
		final LayoutParams lp = child.getLayoutParams() == null ? generateDefaultLayoutParams() : new LayoutParams(child.getLayoutParams());
		addView(childView, lp);
	}
	
	// ### 重载 ###
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		final int measuredWidth;
		final int measuredHeight;
		int usedWidth = getPaddingLeft() + getPaddingRight();
		final int vertPadding = getPaddingTop() + getPaddingBottom();
		int maxHeight = vertPadding;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			measureChild(child, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), heightMeasureSpec);
			usedWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
			maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + vertPadding);
		}
		measuredWidth = widthMode == MeasureSpec.UNSPECIFIED ? usedWidth : widthSize;
		measuredHeight = maxHeight;
		getScroller().setContentWidth(measuredWidth);
		getScroller().setContentHeight(measuredHeight);
		setMeasuredDimension(measuredWidth, measuredHeight);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int left = getPaddingLeft();
		final int top = getPaddingTop();
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			left += lp.leftMargin;
			child.layout(left, top + lp.topMargin, left + child.getMeasuredWidth(), top + lp.topMargin + child.getMeasuredHeight());
			left += child.getMeasuredWidth() + lp.rightMargin;
		}
		getScroller().afterOnLayout(changed, l, t, r, b);
		getScroller().scrollTo(0, 0);
	}
	
	// ### 实现函数 ###
	@Override
	protected Scroller newScroller() {
		return new Scroller();
	}
	protected boolean isChildVisible(int index) {
		final Rect viewportBounds = getViewportBounds();
		
		CellView child = (CellView) getChildByOffset(index);
		RectF childRect = UiUtils.tempRectFs.acquire();
		childRect.set(0, 0, child.getWidth(), child.getHeight());
		RectF srcRect = UiUtils.tempRectFs.acquire();
		srcRect.set(childRect);
		Transformation t = new Transformation();
		if (getChildStaticTransformation(child, t)) {
			t.getMatrix().mapRect(childRect, srcRect);
		}
		childRect.offset(child.getLeft(), child.getTop());
		
		final boolean visible = childRect.intersect(viewportBounds.left, viewportBounds.top, viewportBounds.right, viewportBounds.bottom);
		UiUtils.tempRectFs.release(srcRect);
		UiUtils.tempRectFs.release(childRect);
		return visible;
	}
	/**
	 * 获取从左到右的序列
	 * @return 子View的index
	 */
	protected int[] getChildSequence() {
		int[] left = new int[getChildCount()];
		int[] sequence = new int[getChildCount()];
		for (int i = 0; i < left.length; i++) {
			sequence[i] = i;
			left[i] = getChildAt(i).getLeft();
		}
		
		int temp = 0;
		for (int j = left.length - 1; j > 0; --j) {
			for (int k = 0; k < j; ++k) {
				if(left[k + 1] < left[k]) {
					temp  = left[k + 1];
					left[k + 1] = left[k];
					left[k] = temp;
					
					temp  = sequence[k + 1];
					sequence[k + 1] = sequence[k];
					sequence[k] = temp;
				}
			}
		}
		
		return sequence;
	}
	private View getChildByOffset(int index) {
		return getChildAt(getChildSequence()[index]);
	}
	private void offsetItem() {
		if (getChildCount() > 0) {
			if (mLastViewport.exactCenterX() > getViewportBounds().exactCenterX()) {
				// 左移
				for (int i = 0; i < getChildCount(); i++) {
					if (isChildVisible(0) && isChildVisible(getChildCount() - 1) == false) {
						doOffset((CellView) getChildByOffset(getChildCount() - 1), -1);
					} else {
						break;
					}
				}
			} else if (mLastViewport.exactCenterX() < getViewportBounds().exactCenterX()) {
				// 右移
				for (int i = 0; i < getChildCount(); i++) {
					if (isChildVisible(0) == false && isChildVisible(getChildCount() - 1)) {
						doOffset((CellView) getChildByOffset(0), 1);
					} else {
						break;
					}
				}
			}
		}
	}
	private void doOffset(CellView view, int direction) {
		int length = 0;
		LinkedList<CellView> oldIndex = new LinkedList<GalleryView.CellView>();
		for (int i = 0; i < getChildCount(); i++) {
			CellView child = (CellView) getChildByOffset(i);
			oldIndex.add(child);
		}
		for (int i = 0; i < oldIndex.size(); i++) {
			CellView child = (CellView) oldIndex.get(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			length += child.getWidth() + lp.leftMargin + lp.rightMargin; 
		}
		
		view.offsetLeftAndRight(direction * length);
		invalidate();
	}
}
