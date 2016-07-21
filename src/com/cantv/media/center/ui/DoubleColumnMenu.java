package com.cantv.media.center.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.cantv.media.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class DoubleColumnMenu extends RelativeLayout implements Observer {

    private final String TAG = "DoubleColumnMenu";

    private final int FOCUS_MODE_TMP = 1;
    private final int FOCUS_MODE_SELECTED = 2;
    private final int FOCUS_MODE_NONE = 3;

    private final int POSITION_NULL = -1;

    private final int TIME_FOCUS_TRANSLATE_ANIM = 200;
    private int mMenuWidth;
    private int mMenuBg;
    private int mMenuFocusBg;
    private int mMenuFocusMode;
    private int mSubMenuWidth;
    private int mSubMenuBg;
    private int mSubMenuFocusBg;
    private int mSubMenuFocusMode;
    private int mSubMenuPaddingLeft;
    private int mSubMenuPaddingTop;
    private int mSubMenuPaddingRight;
    private int mSubMenuPaddingBottom;

    private RelativeLayout mMenuRoot;
    private View mMenuHeader;
    private ScrollView mMenuScrollView;
    private LinearLayout mMenuContainer;
    private ImageView mMenuSelectView;
    private FrameLayout mSubMenuRoot;
    private RelativeLayout mSubMenu;
    private ScrollView mSubMenuScrollView;
    private LinearLayout mSubMenuContainer;
    private ImageView mSubMenuFocusView;

    private BaseAdapter mAdapter;
    private int mMenuSelectedPosi = POSITION_NULL;
    private int mMenuFocusTmpPosi = POSITION_NULL;
    private int mSubMenuSelectedPosi = POSITION_NULL;
    private int mSubMenuFocusTmpPosi = POSITION_NULL;
    private SparseArray<List<View>> mCacheViews;
    private SparseArray<List<View>> mMenuItemViews;
    private SparseArray<List<View>> mSubMenuItemViews;
    private Rect mOldFocusRect;
    private Rect mNewFocusRect;
    private boolean focusedMenuItem;
    private Rect mOldSelectRect;
    private Rect mNewSelectRect;
    private int mMenuHeaderHeight;
    private boolean isSubMenuShowing = true;
    private int mDestSubMenuFocusPos = -1;

    private OnItemClickListener mItemClickListener;
    private OnItemFocusChangeListener mFocusChangeListener;
    private OnKeyEventListener mOnKeyListener;

    public interface OnItemClickListener {
        public boolean onMenuItemClick(LinearLayout parent, View view, int position);

        public void onSubMenuItemClick(LinearLayout parent, View view, int position);
    }

    public interface OnItemFocusChangeListener {
        public void onMenuItemFocusChanged(LinearLayout leftViewGroup, View view, int position, boolean hasFocus);

        public void onSubMenuItemFocusChanged(LinearLayout rightViewGroup, View view, int position, boolean hasFocus);
    }

    public interface OnKeyEventListener {
        public boolean onMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event);

        public boolean onSubMenuItemKeyEvent(int position, View v, int keyCode, KeyEvent event);
    }

    public DoubleColumnMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleColumnMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        initAttributes(context, attrs);
        setupLayout(context);
        initConfig();
        initGlobalData();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.DoubleColumnMenu);

        mMenuWidth = styledAttrs.getDimensionPixelSize(R.styleable.DoubleColumnMenu_menuWidth, 300);
        mMenuBg = styledAttrs.getResourceId(R.styleable.DoubleColumnMenu_menuBackground, android.R.color.transparent);
        mMenuFocusBg = styledAttrs.getResourceId(R.styleable.DoubleColumnMenu_menuFocusResId, android.R.color.transparent);
        mMenuFocusMode = styledAttrs.getInt(R.styleable.DoubleColumnMenu_menuFocusMode, FOCUS_MODE_SELECTED);

        mSubMenuWidth = styledAttrs.getDimensionPixelSize(R.styleable.DoubleColumnMenu_subMenuWidth, 300);
        mSubMenuBg = styledAttrs.getResourceId(R.styleable.DoubleColumnMenu_subMenuBackground, android.R.color.transparent);
        mSubMenuFocusBg = styledAttrs.getResourceId(R.styleable.DoubleColumnMenu_subMenuFocusResId, android.R.color.transparent);
        mSubMenuFocusMode = styledAttrs.getInt(R.styleable.DoubleColumnMenu_subMenuFocusMode, FOCUS_MODE_NONE);
        mSubMenuPaddingLeft = styledAttrs.getDimensionPixelSize(R.styleable.DoubleColumnMenu_subMenuPaddingLeft, 0);
        mSubMenuPaddingTop = styledAttrs.getDimensionPixelSize(R.styleable.DoubleColumnMenu_subMenuPaddingTop, 0);
        mSubMenuPaddingRight = styledAttrs.getDimensionPixelSize(R.styleable.DoubleColumnMenu_subMenuPaddingRight, 0);
        mSubMenuPaddingBottom = styledAttrs.getDimensionPixelSize(R.styleable.DoubleColumnMenu_subMenuPaddingBottom, 0);
        styledAttrs.recycle();
    }

    private void setupLayout(Context context) {
        mSubMenuRoot = new FrameLayout(context);
        mSubMenuRoot.setFocusable(false);
        mSubMenuRoot.setId(generateViewId());
        RelativeLayout.LayoutParams subMenuRootLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(mSubMenuRoot, subMenuRootLp);

        mSubMenu = new RelativeLayout(context);
        mSubMenu.setFocusable(false);
        mSubMenu.setId(generateViewId());
        FrameLayout.LayoutParams subMenuLp = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mSubMenuRoot.addView(mSubMenu, subMenuLp);

        mMenuRoot = new RelativeLayout(context);
        mMenuRoot.setFocusable(false);
        mMenuRoot.setId(generateViewId());
        mMenuRoot.setBackgroundResource(mMenuBg);
        RelativeLayout.LayoutParams menuRootLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        menuRootLp.addRule(RelativeLayout.RIGHT_OF, mSubMenuRoot.getId());
        menuRootLp.addRule(RelativeLayout.END_OF, mSubMenuRoot.getId());
        addView(mMenuRoot, menuRootLp);

        mMenuScrollView = new ScrollView(context);
        mMenuScrollView.setFocusable(false);
        mMenuScrollView.setVerticalScrollBarEnabled(false);
        RelativeLayout.LayoutParams menuScrollViewLp = new RelativeLayout.LayoutParams(mMenuWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        mMenuRoot.addView(mMenuScrollView, menuScrollViewLp);

        mMenuContainer = new LinearLayout(context);
        mMenuContainer.setFocusable(false);
        mMenuContainer.setOrientation(LinearLayout.VERTICAL);
        mMenuContainer.setNextFocusUpId(mMenuContainer.getId());
        mMenuContainer.setNextFocusDownId(mMenuContainer.getId());
        mMenuContainer.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int childCount = ((LinearLayout) v).getChildCount();
                    int posi = POSITION_NULL;
                    if (mMenuFocusMode == FOCUS_MODE_SELECTED && mMenuSelectedPosi != POSITION_NULL && mMenuSelectedPosi < childCount) {
                        posi = mMenuSelectedPosi;
                    } else if (mMenuFocusMode == FOCUS_MODE_TMP && mMenuFocusTmpPosi != POSITION_NULL && mMenuFocusTmpPosi < childCount) {
                        posi = mMenuFocusTmpPosi;
                    } else if (mMenuFocusMode == FOCUS_MODE_NONE && childCount > 0) {
                        posi = 0;
                    }
                    if (posi == POSITION_NULL && childCount > 0) {
                        posi = 0;
                    }
                    if (posi != POSITION_NULL) {
                        focusMenuItem(posi);
                    }
                    v.setFocusable(false);
                }
            }
        });
        ScrollView.LayoutParams menuContainerLp = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        mMenuScrollView.addView(mMenuContainer, menuContainerLp);

        ImageView subMenuBgIv = new ImageView(context);
        subMenuBgIv.setBackgroundResource(mSubMenuBg);
        RelativeLayout.LayoutParams subMenuBgLp = new RelativeLayout.LayoutParams(mSubMenuWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
        mSubMenu.addView(subMenuBgIv, subMenuBgLp);

        mSubMenuScrollView = new ScrollView(context);
        mSubMenuScrollView.setFocusable(false);
        mSubMenuScrollView.setVerticalScrollBarEnabled(false);
        RelativeLayout.LayoutParams subMenuScrollViewLp = new RelativeLayout.LayoutParams(mSubMenuWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        subMenuScrollViewLp.leftMargin = mSubMenuPaddingLeft;
        subMenuScrollViewLp.topMargin = mSubMenuPaddingTop;
        subMenuScrollViewLp.rightMargin = mSubMenuPaddingRight;
        subMenuScrollViewLp.bottomMargin = mSubMenuPaddingBottom;
        mSubMenu.addView(mSubMenuScrollView, subMenuScrollViewLp);

        mSubMenuContainer = new LinearLayout(context);
        mSubMenuContainer.setFocusable(false);
        mSubMenuContainer.setOrientation(LinearLayout.VERTICAL);
        mSubMenuContainer.setNextFocusUpId(mSubMenuContainer.getId());
        mSubMenuContainer.setNextFocusDownId(mSubMenuContainer.getId());
        mSubMenuContainer.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int childCount = ((LinearLayout) v).getChildCount();
                    int posi = POSITION_NULL;
                    if (mDestSubMenuFocusPos != -1) {
                        posi = mDestSubMenuFocusPos;
                        mDestSubMenuFocusPos = -1;
                    } else if (mSubMenuFocusMode == FOCUS_MODE_SELECTED) {
                        if (mSubMenuSelectedPosi != POSITION_NULL && mSubMenuSelectedPosi < childCount) {
                            posi = mSubMenuSelectedPosi;
                        }
                    } else if (mSubMenuFocusMode == FOCUS_MODE_TMP && mSubMenuFocusTmpPosi < childCount) {
                        if (mSubMenuFocusTmpPosi != POSITION_NULL) {
                            posi = mSubMenuFocusTmpPosi;
                        }
                    } else if (mMenuFocusMode == FOCUS_MODE_NONE && childCount > 0) {
                        posi = 0;
                    }
                    if (posi == POSITION_NULL && childCount > 0) {
                        posi = 0;
                    }
                    if (posi != POSITION_NULL) {
                        focusSubMenuItem(posi);
                    }
                    v.setFocusable(false);
                }
            }
        });
        ScrollView.LayoutParams subMenuContainerLp = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        mSubMenuScrollView.addView(mSubMenuContainer, subMenuContainerLp);

        mMenuSelectView = new ImageView(getContext());
        mMenuSelectView.setImageResource(mMenuFocusBg);
        mMenuSelectView.setScaleType(ScaleType.FIT_XY);
        RelativeLayout.LayoutParams menuFocusViewFl = new RelativeLayout.LayoutParams(mMenuWidth, 0);
        menuFocusViewFl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mMenuSelectView.setLayoutParams(menuFocusViewFl);
        mMenuRoot.addView(mMenuSelectView, 0);

        mSubMenuFocusView = new ImageView(getContext());
        mSubMenuFocusView.setImageResource(mSubMenuFocusBg);
        mSubMenuFocusView.setScaleType(ScaleType.FIT_XY);
        RelativeLayout.LayoutParams subMenuFocusViewFl = new RelativeLayout.LayoutParams(mMenuWidth, 0);
        subMenuFocusViewFl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mSubMenuFocusView.setLayoutParams(subMenuFocusViewFl);
        addView(mSubMenuFocusView);
    }

    private void initConfig() {
        setFocusable(false);
        mSubMenuRoot.setClipToPadding(true);
    }

    private void initGlobalData() {
        mCacheViews = new SparseArray<List<View>>();
        mMenuItemViews = new SparseArray<List<View>>();
        mSubMenuItemViews = new SparseArray<List<View>>();
        mNewFocusRect = new Rect();
        mNewSelectRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMenuWidth != 0 && mSubMenuWidth != 0) {
            int mWidth = mMenuWidth + mSubMenuWidth + mSubMenuPaddingLeft + mSubMenuPaddingRight;
            setMeasuredDimension(mWidth, MeasureSpec.getSize(heightMeasureSpec));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mSubMenuFocusView.layout(mNewFocusRect.left - 15, mNewFocusRect.top - 15, focusedMenuItem ? (mNewFocusRect.right + 20) : (mNewFocusRect.right + 15), mNewFocusRect.bottom + 15);
        if (mOldSelectRect == null && mMenuContainer.getChildCount() > 0) {
            mMenuContainer.getChildAt(0).getHitRect(mNewSelectRect);
            resolveMenuItemRect(mNewSelectRect, true);
        }
        mMenuSelectView.layout(mNewSelectRect.left, mNewSelectRect.top - 2, mNewSelectRect.right, mNewSelectRect.bottom + 5);
    }

    protected void animateFocusView(View targetView, final boolean isMenuView, final Runnable runnable) {
        final Rect newFocusRect = mNewFocusRect;
        if (mOldFocusRect == null) {
            mOldFocusRect = new Rect();
            mSubMenuFocusView.getHitRect(mOldFocusRect);
        } else {
            mOldFocusRect.set(newFocusRect);
        }
        targetView.getHitRect(newFocusRect);
        if (isMenuView) {
            focusedMenuItem = true;
            resolveMenuItemRect(newFocusRect, false);
        } else {
            focusedMenuItem = false;
            resolveSubMenuItemRect(newFocusRect);
        }

        ValueAnimator ofObject = ValueAnimator.ofObject(new TypeEvaluator<Rect>() {

            @Override
            public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
                int l = (int) (startValue.left + (endValue.left - startValue.left) * fraction);
                int t = (int) (startValue.top + (endValue.top - startValue.top) * fraction);
                int r = (int) (startValue.right + (endValue.right - startValue.right) * fraction);
                int b = (int) (startValue.bottom + (endValue.bottom - startValue.bottom) * fraction);
                newFocusRect.set(l, t, r, b);
                mSubMenuFocusView.layout(l - 15, t - 15, isMenuView ? (r + 20) : (r + 15), b + 15);
                return null;
            }
        }, mOldFocusRect, new Rect(newFocusRect));

        ofObject.setInterpolator(new AccelerateDecelerateInterpolator());
        ofObject.setDuration(TIME_FOCUS_TRANSLATE_ANIM);
        if (runnable != null) {
            ofObject.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    runnable.run();
                }

            });
        }
        ofObject.start();
    }

    protected void animateMenuSelectView(View targetView) {
        final Rect newSelectRect = mNewSelectRect;
        if (mOldSelectRect == null) {
            mOldSelectRect = new Rect();
            mMenuSelectView.getHitRect(mOldSelectRect);
        } else {
            mOldSelectRect.set(newSelectRect);
        }
        targetView.getHitRect(newSelectRect);
        resolveMenuItemRect(newSelectRect, true);

        ValueAnimator ofObject = ValueAnimator.ofObject(new TypeEvaluator<Rect>() {

            @Override
            public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
                int l = (int) (startValue.left + (endValue.left - startValue.left) * fraction);
                int t = (int) (startValue.top + (endValue.top - startValue.top) * fraction);
                int r = (int) (startValue.right + (endValue.right - startValue.right) * fraction);
                int b = (int) (startValue.bottom + (endValue.bottom - startValue.bottom) * fraction);
                newSelectRect.set(l, t, r, b);
                mMenuSelectView.layout(l, t - 2, r, b + 5);
                return null;
            }
        }, mOldSelectRect, new Rect(newSelectRect));

        ofObject.setInterpolator(new AccelerateDecelerateInterpolator());
        ofObject.setDuration(TIME_FOCUS_TRANSLATE_ANIM);
        ofObject.start();
    }

    public void openSubMenu(boolean requestFocus) {
        if (!isSubMenuShowing) {
            isSubMenuShowing = true;
            ViewPropertyAnimator vpa = mSubMenu.animate().translationX(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(TIME_FOCUS_TRANSLATE_ANIM);
            vpa.start();
        }
        if (requestFocus) {
            mSubMenuContainer.setFocusable(true);
            mSubMenuContainer.requestFocus();
        }
    }

    public void openSubMenu(boolean requestFocus, int focusPosi) {
        if (!isSubMenuShowing) {
            isSubMenuShowing = true;
            ViewPropertyAnimator vpa = mSubMenu.animate().translationX(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(TIME_FOCUS_TRANSLATE_ANIM);
            vpa.start();
        }
        mDestSubMenuFocusPos = focusPosi;
        if (requestFocus) {
            mSubMenuContainer.setFocusable(true);
            mSubMenuContainer.requestFocus();
        }
    }

    public void closeSubMenu() {
        if (isSubMenuShowing) {
            isSubMenuShowing = false;
            ViewPropertyAnimator vpa = mSubMenu.animate().translationX(mSubMenu.getMeasuredWidth()).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(TIME_FOCUS_TRANSLATE_ANIM);
            vpa.start();
        }
    }

    protected void resolveMenuItemRect(Rect rect, boolean relativeToSelf) {
        rect.top = rect.top - mMenuScrollView.getScrollY() + mMenuHeaderHeight;
        rect.bottom = rect.bottom - mMenuScrollView.getScrollY() + mMenuHeaderHeight;
        if (!relativeToSelf) {
            rect.left += mSubMenu.getMeasuredWidth();
            rect.right = rect.right + mSubMenu.getMeasuredWidth() - 10;
        }
    }

    protected void resolveSubMenuItemRect(Rect rect) {
        rect.left += mSubMenuPaddingLeft;
        rect.right += mSubMenuPaddingLeft;
        rect.top = rect.top - mSubMenuScrollView.getScrollY() + mSubMenuPaddingTop;
        rect.bottom = rect.bottom - mSubMenuScrollView.getScrollY() + mSubMenuPaddingTop;
    }

    public void scrollMenu2Posi(int position) {
        if (position >= mMenuContainer.getChildCount()) {
            StringBuilder sb = new StringBuilder("Fail to scrollMenu2Posi. [There are total ").append(mMenuContainer.getChildCount()).append(" child in menu, but your choice of position is ").append(position).append("]");
            Log.w(TAG, sb.toString());
            return; 
        }
        final View child = mMenuContainer.getChildAt(position);
        if (!isViewVisible(mMenuScrollView, child)) {
            mMenuScrollView.smoothScrollTo(0, child.getTop());
        } 
    }

    public void focusMenuItem(int posi) {
        int childCount = mMenuContainer.getChildCount();
        if (childCount == 0) {
            return;
        }

        int mPosi = 0;
        if (posi > 0 && posi < childCount) {
            mPosi = posi;
        } 
        View child = mMenuContainer.getChildAt(mPosi);
        if (child.isEnabled() && child.isFocusable()) {
            child.requestFocus();
        }
    }

    public void focusMenuItem2(final int posi) {
        mMenuContainer.post(new Runnable() {

            @Override
            public void run() {
                focusMenuItem(posi);
            }
        });
    }

    public void scrollSubMenu2Posi(int position) {
        if (position >= mSubMenuContainer.getChildCount()) {
            StringBuilder sb = new StringBuilder("Fail to scrollSubMenu2Posi. [There are total ").append(mSubMenuContainer.getChildCount()).append(" child in right List, but your choice of position is ").append(position).append("]");
            Log.w(TAG, sb.toString());
            return;
        }
        final View child = mSubMenuContainer.getChildAt(position);
        if (!isViewVisible(mSubMenuScrollView, child)) {
            mSubMenuScrollView.smoothScrollTo(0, child.getTop());
        }
    }

    public void focusSubMenuItem(int posi) {
        int childCount = mSubMenuContainer.getChildCount();
        if (childCount == 0) {
            return;
        }

        int mPosi = 0;
        if (posi > 0 && posi < childCount) {
            mPosi = posi;
        }
        View child = mSubMenuContainer.getChildAt(mPosi);
        if (child.isEnabled() && child.isFocusable()) {
            child.requestFocus();
        }
    }

    public void focusSubMenuItem2(final int posi) {
        mSubMenuContainer.post(new Runnable() {

            @Override
            public void run() {
                focusSubMenuItem(posi);
            }
        });
    }

    private void pushViewToCache(int viewType, View view) {
        if (mCacheViews.indexOfKey(viewType) >= 0) {
            mCacheViews.get(viewType).add(view);
        } else {
            LinkedList<View> typedViews = new LinkedList<View>();
            typedViews.add(view);
            mCacheViews.append(viewType, typedViews);
        }
    }

    private View pullViewFromCache(int viewType) {
        if (mCacheViews.indexOfKey(viewType) < 0) {
            return null;
        }
        List<View> typedViews = mCacheViews.get(viewType);
        if (typedViews == null || typedViews.isEmpty()) {
            return null;
        }
        return typedViews.remove(typedViews.size() - 1);
    }

    private void refreshMenu() {
        recycleMenuItemViews();
        mMenuSelectedPosi = POSITION_NULL;
        mMenuFocusTmpPosi = POSITION_NULL;
        final int menuItemCount = mAdapter.getMenuCount();
        for (int posi = 0; posi < menuItemCount; posi++) {
            int viewType = mAdapter.getMenuItemViewType(posi);
            View cacheView = pullViewFromCache(viewType);
            if (cacheView != null) {
                cacheView.setSelected(false);
                cacheView.setFocusable(true);
            }
            View newView = mAdapter.getMenuItemView(mMenuContainer, cacheView, posi);
            if (newView == null) {
                throw new NullPointerException("Null of menuItemView can't be processed.");
            }
            if (cacheView != null && cacheView != newView) {
                pushViewToCache(viewType, cacheView);
            }
            configMenuItem(newView, posi, menuItemCount);
            storeMenuItemView(viewType, newView);
            mMenuContainer.addView(newView);
        }
        setupMenuItemKeyEvent();
    }

    private void recycleMenuItemViews() {
        int viewTypeCount = mMenuItemViews.size();
        for (int posi = 0; posi < viewTypeCount; posi++) {
            int viewType = mMenuItemViews.keyAt(posi);
            List<View> views = mMenuItemViews.get(viewType);
            for (View view : views) {
                view.setNextFocusUpId(NO_ID);
                view.setNextFocusDownId(NO_ID);
                view.clearAnimation();
                pushViewToCache(viewType, view);
            }
            views.clear();
        }
        mMenuItemViews.clear();
        mMenuContainer.removeAllViews();
    }

    private void configMenuItem(View newView, final int position, int totalCount) {
        if (newView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) newView;
            viewGroup.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        newView.setId(View.generateViewId());
        newView.setNextFocusRightId(newView.getId());
        newView.setNextFocusForwardId(newView.getId());
        newView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMenuSelectedPosi = position;
                animateMenuSelectView(v);
                if (mItemClickListener != null && mItemClickListener.onMenuItemClick(mMenuContainer, v, position)) {
                    return;
                }
                openSubMenu(false);
            }
        });
        newView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mOnKeyListener != null && mOnKeyListener.onMenuItemKeyEvent(position, v, keyCode, event)) {
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    openSubMenu(true);
                    return true;
                }
                return false;
            }
        });
        newView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mMenuFocusTmpPosi = position;
                    animateFocusView(v, true, null);
                }
                if (mFocusChangeListener != null) {
                    mFocusChangeListener.onMenuItemFocusChanged(mMenuContainer, v, position, hasFocus);
                }
            }
        });
    }

    private void storeMenuItemView(int viewType, View view) {
        if (mMenuItemViews.indexOfKey(viewType) >= 0) {
            mMenuItemViews.get(viewType).add(view);
        } else {
            LinkedList<View> typedViews = new LinkedList<View>();
            typedViews.add(view);
            mMenuItemViews.append(viewType, typedViews);
        }
    }

    private void setupMenuItemKeyEvent() {
        View preView = null;
        for (int i = 0, viewCount = mMenuContainer.getChildCount(); i < viewCount; i++) {
            View child = mMenuContainer.getChildAt(i);
            if (preView != null) {
                child.setNextFocusUpId(preView.getId());
                preView.setNextFocusDownId(child.getId());
                if (i == viewCount - 1) {
                    child.setNextFocusDownId(child.getId());
                }
            } else {
                child.setNextFocusUpId(child.getId());
            }
            preView = child;
        }
    }

    private void refreshSubMenu() {
        recycleSubMenuItemViews();
        mSubMenuSelectedPosi = POSITION_NULL;
        mSubMenuFocusTmpPosi = POSITION_NULL;
        final int subMenuItemCount = mAdapter.getSubMenuCount();
        for (int i = 0; i < subMenuItemCount; i++) {
            int viewType = mAdapter.getSubItemViewType(i);
            View cacheView = pullViewFromCache(viewType);
            if (cacheView != null) {
                cacheView.setSelected(false);
                cacheView.setFocusable(true);
            }
            View newView = mAdapter.getSubMenuItemView(mSubMenuContainer, cacheView, i);
            if (newView == null) {
                throw new NullPointerException("Null of subMenuItemView can't be processed.");
            }
            if (cacheView != null && cacheView != newView) {
                pushViewToCache(viewType, cacheView);
            }
            configSubMenuItem(newView, i, subMenuItemCount);
            storeSubMenuItemView(viewType, newView);
            mSubMenuContainer.addView(newView);
        }
        setupSubMenuItemKeyEvent();
    }

    private void recycleSubMenuItemViews() {
        int viewTypeCount = mSubMenuItemViews.size();
        for (int posi = 0; posi < viewTypeCount; posi++) {
            int viewType = mSubMenuItemViews.keyAt(posi);
            List<View> views = mSubMenuItemViews.get(viewType);
            for (View view : views) {
                view.setNextFocusUpId(NO_ID);
                view.setNextFocusDownId(NO_ID);
                view.clearAnimation();
                pushViewToCache(viewType, view);
            }
            views.clear();
        }
        mSubMenuItemViews.clear();
        mSubMenuContainer.removeAllViews();
    }

    private void configSubMenuItem(final View newView, final int position, int totalCount) {
        if (newView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) newView;
            viewGroup.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        newView.setId(View.generateViewId());
        newView.setNextFocusLeftId(newView.getId());
        newView.setNextFocusForwardId(newView.getId());
        newView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mSubMenuSelectedPosi = position;
                if (mItemClickListener != null) {
                    mItemClickListener.onSubMenuItemClick(mSubMenuContainer, v, position);
                }
            }
        });
        newView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mOnKeyListener != null && mOnKeyListener.onSubMenuItemKeyEvent(position, v, keyCode, event)) {
                    return true;
                }
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    closeSubMenu();
                    mMenuContainer.setFocusable(true);
                    mMenuContainer.requestFocus();
                    return true;
                }
                return false;
            }
        });
        newView.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                v.clearAnimation();
                if (hasFocus) {
                    mSubMenuFocusTmpPosi = position;
                    animateFocusView(newView, false, new Runnable() {

                        @Override
                        public void run() {
                            ScaleAnimation mZoomInAnim = new ScaleAnimation(1, 1.125f, 1, 1.125f, ScaleAnimation.RELATIVE_TO_SELF, 0.3f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                            mZoomInAnim.setInterpolator(new DecelerateInterpolator());
                            mZoomInAnim.setFillAfter(true);
                            mZoomInAnim.setDuration(TIME_FOCUS_TRANSLATE_ANIM);
                            newView.clearAnimation();
                            newView.startAnimation(mZoomInAnim);
                        }
                    });
                } else {
                    ScaleAnimation mZoomOutAnim = new ScaleAnimation(1.125f, 1, 1.125f, 1, ScaleAnimation.RELATIVE_TO_SELF, 0.3f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                    mZoomOutAnim.setInterpolator(new DecelerateInterpolator());
                    mZoomOutAnim.setFillAfter(true);
                    mZoomOutAnim.setDuration(TIME_FOCUS_TRANSLATE_ANIM);
                    newView.clearAnimation();
                    newView.startAnimation(mZoomOutAnim);
                }
                if (mFocusChangeListener != null) {
                    mFocusChangeListener.onSubMenuItemFocusChanged(mSubMenuContainer, newView, position, hasFocus);
                }
            }
        });
    }

    private void storeSubMenuItemView(int viewType, View view) {
        if (mSubMenuItemViews.indexOfKey(viewType) >= 0) {
            mSubMenuItemViews.get(viewType).add(view);
        } else {
            LinkedList<View> typedViews = new LinkedList<View>();
            typedViews.add(view);
            mSubMenuItemViews.append(viewType, typedViews);
        }
    }

    private void setupSubMenuItemKeyEvent() {
        final int childCount = mSubMenuContainer.getChildCount();
        if (childCount > 0) {
            View firstChild = mSubMenuContainer.getChildAt(0);
            View lastChild = mSubMenuContainer.getChildAt(childCount - 1);
            firstChild.setNextFocusUpId(firstChild.getId());
            lastChild.setNextFocusDownId(lastChild.getId());
        }
    }

    public void setAdapter(BaseAdapter adapter) {
        adapter.deleteObservers();
        adapter.addObserver(this);
        mAdapter = adapter;

        mMenuItemViews.clear();
        mSubMenuItemViews.clear();
        mCacheViews.clear();

        mAdapter.notifyDataSetChanged();
    }

    public static abstract class BaseAdapter extends Observable {

        public static final String FLAG_NOTIFY_MENU_CHANGE = "1";
        public static final String FLAG_NOTIFY_SUB_MENU_CHANGE = "2";

        private final int DEFAULT_MENU_ITEM_VIEW_TYPE = 0x001;
        private final int DEFAULT_SUB_MENU_ITEM_VIEW_TYPE = 0x002;

        public abstract View getSubMenuItemView(LinearLayout parent, View convertView, int position);

        public abstract int getSubMenuCount();

        public abstract View getMenuItemView(LinearLayout parent, View convertView, int position);

        public abstract int getMenuCount();

        public abstract Object getMenuItem(int position);

        public abstract Object getSubMenuItem(int position);

        public int getViewTypeCount() {
            return 2;
        }

        public int getMenuItemViewType(int position) {
            return DEFAULT_MENU_ITEM_VIEW_TYPE;
        }

        public int getSubItemViewType(int position) {
            return DEFAULT_SUB_MENU_ITEM_VIEW_TYPE;
        }

        final public void notifyDataSetChanged() {
            notifyMenuDataSetChanged();
            notifySubMenuDataSetChanged();
        }

        final public void notifyMenuDataSetChanged() {
            setChanged();
            notifyObservers(FLAG_NOTIFY_MENU_CHANGE);
        }

        final public void notifySubMenuDataSetChanged() {
            setChanged();
            notifyObservers(FLAG_NOTIFY_SUB_MENU_CHANGE);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (BaseAdapter.FLAG_NOTIFY_MENU_CHANGE.equals(data)) {
            refreshMenu();
        } else if (BaseAdapter.FLAG_NOTIFY_SUB_MENU_CHANGE.equals(data)) {
            refreshSubMenu();
        }
    }

    public void setOnItemsClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnItemsFocusChangeListener(OnItemFocusChangeListener listener) {
        mFocusChangeListener = listener;
    }

    public void setOnItemKeyEventListener(OnKeyEventListener listener) {
        mOnKeyListener = listener;
    }

    protected boolean isViewVisible(ScrollView scrollView, View child) {
        int scrollY = scrollView.getScrollY();

        int top = child.getTop();
        if (top >= scrollY) {
            return false;
        }

        int bottom = child.getBottom();
        if (bottom <= scrollY) {
            return false;
        }

        return true;
    }

    public void setMenuHeader(View view) {
        view.setId(generateViewId());
        mMenuHeader = view;
        mMenuRoot.addView(mMenuHeader, 0);
        mMenuHeaderHeight = view.getLayoutParams().height;

        RelativeLayout.LayoutParams rlp = (LayoutParams) mMenuScrollView.getLayoutParams();
        rlp.addRule(BELOW, mMenuHeader.getId());
        mMenuScrollView.requestLayout();
    }

    public void setMenuSelectPosi(int position) {
        mMenuSelectedPosi = position;
    }

    public void setSubMenuSelectPosi(int position) {
        mSubMenuSelectedPosi = position;
    }
}
