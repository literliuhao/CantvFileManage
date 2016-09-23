package com.cantv.media.center.ui;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cantv.media.R;
import com.cantv.media.center.data.LyricInfo;
import com.cantv.media.center.data.LyricInfo.Lyric;

import java.util.List;

public class LyricView extends View {

    public final int TEXT_ALIGN_LEFT = 1;
    public final int TEXT_ALIGN_CENTER = 2;
    public final int TEXT_ALIGN_RIGHT = 3;

    private int WIDTH_DEFAULT;
    private int HEIGHT_DEFAULT;

    private Paint mPaint;
    private int mTextSize;
    private int mTextColor;
    private int mFocusTextSize;
    private int mFocusTextColor;
    private int mTextAlign;
    private int mLineSpacing;
    private float mFocusLinePosiPercent;// 竖直方向上 当前正在播的歌词的位置百分比(0 ~ 1.0)

    private int mWidth;
    private int mHeight;
    private float mFocusLineY;
    private float mTmpFocusLineY;
    private float mTextAlignBaseLineX;

    private FontMetrics mTextFontMtx;
    private FontMetrics mFocusTextFontMtx;
    private float mTextHeight;
    private float mFocusTextHeight;

    private LyricInfo mLyricInfo;
    private long mCurrTime;
    private int mTimeOffset;
    private int mFocusedLineIndex = -1;
    private int mDestLineIndex;
    private GradientInfo mGradientInfo;
    private ValueAnimator anim;
    boolean needOffset = false;
    private long mDestTime;
    private float mMaxTopDrawDist = 0;
    private float mMaxBottomDrawDist = 0;

    private float[] textColorFrom;
    private float[] textColorTo;
    private float[] focusTextColorFrom;
    private float[] focusTextColorTo;
    private float[] tmpTextColor;

    public LyricView(Context context) {
        this(context, null, 0);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LyricView);
        mTextSize = ta.getDimensionPixelSize(R.styleable.LyricView_textSize, 36);
        mTextColor = ta.getColor(R.styleable.LyricView_textColor, Color.parseColor("#CCFFFFFF"));
        mFocusTextSize = ta.getDimensionPixelSize(R.styleable.LyricView_textSizeFocused, 50);
        mFocusTextColor = ta.getColor(R.styleable.LyricView_textColorFocused, Color.parseColor("#FDF352"));
        mTextAlign = ta.getInt(R.styleable.LyricView_textAlign, TEXT_ALIGN_LEFT);
        mLineSpacing = ta.getDimensionPixelSize(R.styleable.LyricView_lineSpacing, 20);
        mFocusLinePosiPercent = ta.getFloat(R.styleable.LyricView_lineFocusedPosiPercent, 0.5f);
        ta.recycle();

        if (mFocusLinePosiPercent < 0 || mFocusLinePosiPercent > 1) {
            mFocusLinePosiPercent = 0.5f;
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        WIDTH_DEFAULT = displayMetrics.widthPixels / 2;
        HEIGHT_DEFAULT = displayMetrics.heightPixels;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(mTextAlign == TEXT_ALIGN_CENTER ? Align.CENTER : (mTextAlign == TEXT_ALIGN_LEFT ? Align.LEFT : Align.RIGHT));
        mPaint.setDither(true);

        mTextFontMtx = new FontMetrics();
        mFocusTextFontMtx = new FontMetrics();
        mPaint.setTextSize(mTextSize);
        mPaint.getFontMetrics(mTextFontMtx);
        mPaint.setTextSize(mFocusTextSize);
        mPaint.getFontMetrics(mFocusTextFontMtx);

        mTextHeight = mTextFontMtx.descent - mTextFontMtx.ascent;
        mFocusTextHeight = mFocusTextFontMtx.descent - mFocusTextFontMtx.ascent;

        textColorFrom = new float[3];
        textColorTo = new float[3];
        focusTextColorFrom = new float[3];
        focusTextColorTo = new float[3];
        tmpTextColor = new float[3];

        mGradientInfo = new GradientInfo();
        mGradientInfo.textColor = mTextColor;
        mGradientInfo.focusTextColor = mFocusTextColor;

        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = measuredWidth;
        } else {
            mWidth = WIDTH_DEFAULT;
            if (widthMode == MeasureSpec.AT_MOST) {
                mWidth = resolveSize(mWidth, widthMeasureSpec);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = measuredHeight;
        } else {
            mHeight = HEIGHT_DEFAULT;
            if (heightMode == MeasureSpec.AT_MOST) {
                mHeight = resolveSize(mHeight, heightMeasureSpec);
            }
        }
        setMeasuredDimension(mWidth, mHeight);

        if (mTextAlign == TEXT_ALIGN_LEFT) {
            mTextAlignBaseLineX = getPaddingLeft();
        } else if (mTextAlign == TEXT_ALIGN_CENTER) {
            mTextAlignBaseLineX = (mWidth + getPaddingLeft() - getPaddingRight()) / 2.0f;
        } else {
            mTextAlignBaseLineX = mWidth - getPaddingRight();
        }

        mTmpFocusLineY = (mHeight - getPaddingTop() - getPaddingBottom()) * mFocusLinePosiPercent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyricInfo == null) {
            return;
        }

        // draw focus line
        mPaint.setColor(mGradientInfo.focusTextColor);
        mPaint.setTextSize(mFocusTextSize - mGradientInfo.textSizeOffset);
        mFocusedLineIndex = selectLyric(mCurrTime);
        float lineY = mFocusLineY - mPaint.ascent();
        if (needOffset) {
            lineY += mGradientInfo.preFocusOffsetY;
        }
        canvas.drawText(mLyricInfo.getLyricStrAt(mFocusedLineIndex), mTextAlignBaseLineX, lineY, mPaint);

        // draw focus pre-Lines
        int drawDistanceY = 0;
        float textAscent = 0;
        float offset = 0;
        for (int preLine = mFocusedLineIndex - 1; preLine >= 0; preLine--) {
            if (drawDistanceY > mMaxTopDrawDist) {
                // avoid draw unvisible area.
                break;
            }
            if (mGradientInfo.preFocusOffsetY > 0 && preLine == mDestLineIndex) {
                mPaint.setColor(mGradientInfo.textColor);
                mPaint.setTextSize(mTextSize + mGradientInfo.textSizeOffset);
                textAscent = mPaint.ascent();
                drawDistanceY += mPaint.descent() - textAscent + mLineSpacing;
            } else {
                mPaint.setColor(mTextColor);
                mPaint.setTextSize(mTextSize);
                textAscent = mTextFontMtx.ascent;
                drawDistanceY += mTextHeight + mLineSpacing;
            }
            lineY = mFocusLineY - (mFocusedLineIndex - preLine) * (mLineSpacing + mTextHeight) - textAscent;
            if (needOffset) {
                lineY += mGradientInfo.preFocusOffsetY;
            }
            canvas.drawText(mLyricInfo.getLyricStrAt(preLine), mTextAlignBaseLineX, lineY, mPaint);
        }

        // draw focus post-Lines
        drawDistanceY = 0;
        for (int postLine = mFocusedLineIndex + 1, lineCount = mLyricInfo.getLyrics().size(); postLine < lineCount; postLine++) {
//            if (drawDistanceY > mMaxBottomDrawDist) {
//                // avoid draw unvisible area.
//                break;
//            }
            if (mGradientInfo.postFocusOffsetY < 0 && postLine == mDestLineIndex) {
                mPaint.setColor(mGradientInfo.textColor);
                mPaint.setTextSize(mTextSize + mGradientInfo.textSizeOffset);
                textAscent = mPaint.ascent();
                drawDistanceY += mPaint.descent() - textAscent + mLineSpacing;
                if (needOffset) {
                    offset = mGradientInfo.postFocusOffsetY;
                }
            } else {
                mPaint.setColor(mTextColor);
                mPaint.setTextSize(mTextSize);
                textAscent = mTextFontMtx.ascent;
                drawDistanceY += mTextHeight + mLineSpacing;
                if (needOffset) {
                    offset = mGradientInfo.preFocusOffsetY;
                }
            }
            lineY = mFocusLineY + (postLine - mFocusedLineIndex - 1) * (mLineSpacing + mTextHeight) + mLineSpacing + mFocusTextHeight - textAscent + offset;
            canvas.drawText(mLyricInfo.getLyricStrAt(postLine), mTextAlignBaseLineX, lineY, mPaint);
        }
    }

    public void setLyricInfo(LyricInfo lyricInfo) {
        if (lyricInfo == null || !lyricInfo.isLegal()) {
            mLyricInfo = null;
            invalidate();
            return;
        }
        mLyricInfo = lyricInfo;
        invalidate();
    }

    /**
     * 设置延迟
     *
     * @param offset
     */
    public void setTimeOffset(int offset) {
        mTimeOffset = offset;
        invalidate();
    }

    /**
     * 调整延迟
     *
     * @param offset
     */
    public void adjustTimeOffset(int offset) {
        mTimeOffset += offset;
        invalidate();
    }

    public int getTimeOffset() {
        return mTimeOffset;
    }

    public void setCurrTime(final long timeInMillis) {
        if (mLyricInfo == null) {
            return;
        }
        mDestLineIndex = selectLyric(resolveTime(timeInMillis + mTimeOffset));
        if (mFocusedLineIndex == mDestLineIndex) {
            return;
        }
        if (anim != null && anim.isStarted()) {
            mDestTime = resolveTime(timeInMillis + mTimeOffset);
            return;
        }
        int startOffsetLineIndex = calculateStartOffsetIndex();
        needOffset = canculateFocusLineY(mDestLineIndex, startOffsetLineIndex);
        canculateMaxDrawDistance();

        float preFocusoffsetY = 0;
        float postFocusOffsetY = 0;
        if (mDestLineIndex > startOffsetLineIndex) {
            if (mDestLineIndex > mFocusedLineIndex) {
                preFocusoffsetY = -(mDestLineIndex - mFocusedLineIndex) * (mLineSpacing + mTextHeight);
                postFocusOffsetY = -(mDestLineIndex - mFocusedLineIndex - 1) * (mLineSpacing + mTextHeight) - mFocusTextHeight - mLineSpacing;
            } else if (mDestLineIndex < mFocusedLineIndex) {
                preFocusoffsetY = (mDestLineIndex - mFocusedLineIndex) * (mLineSpacing + mTextHeight);
                postFocusOffsetY = (mDestLineIndex - mFocusedLineIndex - 1) * (mLineSpacing + mTextHeight) - mFocusTextHeight - mLineSpacing;
            }
        }
        if (preFocusoffsetY != 0 || postFocusOffsetY != 0) {
            mGradientInfo.textColor = mTextColor;
            mGradientInfo.focusTextColor = mFocusTextColor;
            GradientInfo endInfo = new GradientInfo(preFocusoffsetY, postFocusOffsetY, mFocusTextSize - mTextSize, mFocusTextColor, mTextColor);
            anim = ValueAnimator.ofObject(new OffsetEvaluator(timeInMillis), mGradientInfo, endInfo);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(1000);
            anim.start();
        } else {
            mCurrTime = timeInMillis;
            mFocusedLineIndex = mDestLineIndex;
            invalidate();
        }
    }

    private long resolveTime(long timeInMillis) {
        if (timeInMillis < 0) {
            timeInMillis = 0;
        }
        return timeInMillis;
    }

    public class OffsetEvaluator implements TypeEvaluator<GradientInfo> {

        private long destTime;

        public OffsetEvaluator(long destTime) {
            this.destTime = destTime;
        }

        @Override
        public GradientInfo evaluate(float fraction, GradientInfo startValue, GradientInfo endValue) {
            startValue.preFocusOffsetY = startValue.preFocusOffsetY + (endValue.preFocusOffsetY - startValue.preFocusOffsetY) * fraction;
            startValue.postFocusOffsetY = startValue.postFocusOffsetY + (endValue.postFocusOffsetY - startValue.postFocusOffsetY) * fraction;
            startValue.textSizeOffset = startValue.textSizeOffset + (endValue.textSizeOffset - startValue.textSizeOffset) * fraction;

            // canculate focused line textColor change-offset
            Color.colorToHSV(startValue.focusTextColor, focusTextColorFrom);
            Color.colorToHSV(endValue.focusTextColor, focusTextColorTo);
            int focusAlphaFrom = Color.alpha(startValue.focusTextColor);
            int focusAlphaTo = Color.alpha(endValue.focusTextColor);
            tmpTextColor[0] = focusTextColorFrom[0] + (focusTextColorTo[0] - focusTextColorFrom[0]) * fraction;
            tmpTextColor[1] = focusTextColorFrom[1] + (focusTextColorTo[1] - focusTextColorFrom[1]) * fraction;
            tmpTextColor[2] = focusTextColorFrom[2] + (focusTextColorTo[2] - focusTextColorFrom[2]) * fraction;
            int alphaOffset = (int) (focusAlphaFrom + (focusAlphaTo - focusAlphaFrom) * fraction);
            startValue.focusTextColor = Color.HSVToColor(alphaOffset, tmpTextColor);
            Color.colorToHSV(startValue.textColor, textColorFrom);
            Color.colorToHSV(endValue.textColor, textColorTo);

            // canculate destLine textColor change-offset
            Color.colorToHSV(startValue.textColor, textColorFrom);
            Color.colorToHSV(endValue.textColor, textColorTo);
            int alphaFrom = Color.alpha(startValue.textColor);
            int alphaTo = Color.alpha(endValue.textColor);
            tmpTextColor[0] = textColorFrom[0] + (textColorTo[0] - textColorFrom[0]) * fraction;
            tmpTextColor[1] = textColorFrom[1] + (textColorTo[1] - textColorFrom[1]) * fraction;
            tmpTextColor[2] = textColorFrom[2] + (textColorTo[2] - textColorFrom[2]) * fraction;
            alphaOffset = (int) (alphaFrom + (alphaTo - alphaFrom) * fraction);
            startValue.textColor = Color.HSVToColor(alphaOffset, tmpTextColor);

            // apply
            mGradientInfo.preFocusOffsetY = startValue.preFocusOffsetY;
            mGradientInfo.postFocusOffsetY = startValue.postFocusOffsetY;
            mGradientInfo.textSizeOffset = startValue.textSizeOffset;
            mGradientInfo.focusTextColor = startValue.focusTextColor;
            mGradientInfo.textColor = startValue.textColor;
            invalidate();

            if (fraction == 1) {
                // anim finish
                mGradientInfo.reset();
                mGradientInfo.textColor = mTextColor;
                mGradientInfo.focusTextColor = mFocusTextColor;
                mCurrTime = destTime;
                mFocusedLineIndex = mDestLineIndex;
                if (mDestTime != 0) {
                    setCurrTime(mDestTime);
                }
            }
            return null;
        }
    }

    private int calculateStartOffsetIndex() {
        float offset = mTmpFocusLineY - getPaddingTop();
        int index = 0;
        while (offset > mLineSpacing + mTextHeight) {
            offset -= mLineSpacing + mTextHeight;
            index++;
        }
        return index;
    }

    private boolean canculateFocusLineY(int focusedLineIndex, int startOffsetIndex) {
        if (focusedLineIndex >= startOffsetIndex) {
            mFocusLineY = mTmpFocusLineY;
            return true;
        }
        if (focusedLineIndex == startOffsetIndex) {
            mFocusLineY = mTmpFocusLineY;
            return false;
        }
        float offset = mTmpFocusLineY - (mTextHeight + mLineSpacing) * (startOffsetIndex - focusedLineIndex);
        mFocusLineY = offset;
        return false;
    }

    private void canculateMaxDrawDistance() {
        mMaxTopDrawDist = mFocusLineY - getPaddingTop();
        mMaxBottomDrawDist = mHeight - getPaddingBottom() + 100;
    }

    public void setTextAlign(int textAlign) {
        if (textAlign == TEXT_ALIGN_LEFT || textAlign == TEXT_ALIGN_CENTER || textAlign == TEXT_ALIGN_RIGHT) {
            mTextAlign = textAlign;
            invalidate();
        }
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        invalidate();
    }

    public void setTextColor(int colorResId) {
        mTextColor = getResources().getColor(colorResId);
        invalidate();
    }

    public void setFocusTextSize(int textSizeFocused) {
        mFocusTextSize = textSizeFocused;
        invalidate();
    }

    public void setFocusTextColor(int colorResId) {
        mFocusTextColor = getResources().getColor(colorResId);
        invalidate();
    }

    private static class GradientInfo {
        public float preFocusOffsetY;
        public float postFocusOffsetY;
        public float textSizeOffset;
        public int textColor;
        public int focusTextColor;

        public GradientInfo() {
        }

        public GradientInfo(float preFocusoffsetY, float postFocusoffsetY, float textSizeOffset, int textColor, int focusTextColor) {
            super();
            this.preFocusOffsetY = preFocusoffsetY;
            this.postFocusOffsetY = postFocusoffsetY;
            this.textSizeOffset = textSizeOffset;
            this.textColor = textColor;
            this.focusTextColor = focusTextColor;
        }

        public void reset() {
            preFocusOffsetY = 0;
            postFocusOffsetY = 0;
            textSizeOffset = 0;
        }

        @Override
        public String toString() {
            return "GradientInfo [preFocusoffsetY=" + preFocusOffsetY + ", postFocusoffsetY=" + postFocusOffsetY + ", textSizeOffset=" + textSizeOffset + ", textColor=" + textColor + ", focusTextColor=" + focusTextColor + "]";
        }

    }

    private int selectLyric(long time) {
        if (mLyricInfo == null || !mLyricInfo.isLegal()) {
            return 0;
        }

        List<Lyric> lyrics = mLyricInfo.getLyrics();
        int index = 0;
        for (Lyric lyric : lyrics) {
            if (time >= lyric.getStartTime() && ((lyric.getEndTime()) == 0 || time <= lyric.getEndTime())) {
                return index;
            }
            index++;
        }
        return 0;
    }
}
