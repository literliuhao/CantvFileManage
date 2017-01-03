package com.cantv.media.center.ui.directory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.core.cache.BitmapCache;
import com.app.core.cache.Cache;
import com.app.core.cache.FileCache;
import com.app.core.common.Public;
import com.app.core.webservices.WebService;
import com.app.core.webservices.WebSession.CacheStrategy;
import com.app.core.webservices.WebSessionException;
import com.cantv.media.center.Listener.PicViewDecoder;
import com.cantv.media.center.Listener.PicViewListener;
import com.cantv.media.center.constants.PicStretch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PicView extends ImageView {
    private static final ScheduledExecutorService mPicLoadTaskExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final Paint mPaint = new Paint();
    private static final Rect mDrawingRect = new Rect();
    private static final BitmapCache mPicCache;
    private static final BitmapCache mTunedBitmapCache;
    private static final LinkedList<PicView> mAllUsedPicViews = new LinkedList<>();
    private static AtomicBoolean mTunedBitmapLock = new AtomicBoolean();
    private String mPicUri = null;
    private String mUserAgent = null;
    private BitmapCache.CacheKey mCacheKey = null;
    private Drawable mDefaultPicDrawable = null;
    private Drawable mPicForegroundDrawable = null;
    private PicStretch mPicStretch = PicStretch.SCALE_INSIDE;
    private PicLoadTask mPicLoadTask = null;
    private PicViewListener mPicListener = null;
    private PicViewDecoder mPicDecoder = null;
    private boolean mIsPicVisible = false;
    private boolean mHasPicError = false;
    private boolean mIsPicSaved = false;
    private File mSavePicAs = null;
    private CompressFormat mCompressFormat = CompressFormat.PNG;
    private Bitmap.Config mBitmapConfig = Bitmap.Config.RGB_565;
    private int mCompressQuality = 100;
    private float mCornerRadius = 0.0f;

    private Bitmap mLocalTunedBitmap;
    private TunedBitmapCacheKey mLocalTunedBitmapCacheKey;

    static {
        mPicCache = new BitmapCache("pic", 10, new FileCache("pic", 200, new File(Environment.getExternalStorageDirectory(), "Pic")));
        mPicCache.setMemLimit(1024 * 1024 * 1);

        mTunedBitmapCache = new BitmapCache("picview_tuned_bitmap", 20);
        long cacheSize = 1024L * 1024L * 50;
        mTunedBitmapCache.setMemLimit((int) cacheSize);
    }

    public static class TunedBitmapCacheKey extends BitmapCache.CacheKey {
        public final int mDstWidth;
        public final int mDstHeight;
        public final PicStretch mPicStretch;
        public final float mCornerRadius;
        private Rect mPicRectInDstArea;

        public TunedBitmapCacheKey(Object tag, int dstWidth, int dstHeight, PicStretch picStretch, float cornerRadius) {
            super(tag);
            mDstWidth = dstWidth;
            mDstHeight = dstHeight;
            mPicStretch = picStretch;
            mCornerRadius = cornerRadius;
        }

        @Override
        public int hashCode() {
            return mBitmapTag.hashCode() + (mDstWidth * mDstHeight) << 5;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof TunedBitmapCacheKey) {
                TunedBitmapCacheKey another = (TunedBitmapCacheKey) o;
                return mBitmapTag.equals(another.mBitmapTag) && mDstWidth == another.mDstWidth && mDstHeight == another.mDstHeight && mPicStretch == another.mPicStretch && Float.compare(mCornerRadius, another.mCornerRadius) == 0;
            } else {
                return false;
            }
        }

        public Rect getPicRectInDstArea() {
            return this.mPicRectInDstArea;
        }

        public void setPicRectInDstArea(Rect picRectInDstArea) {
            mPicRectInDstArea = picRectInDstArea;
        }
    }

    private class PicLoadTask {
        private String mTaskPicUri = null;
        private PicViewDecoder mTaskPicDecoder = null;
        private File mTaskSavePicAs = null;
        private CompressFormat mTaskCompressFormat = CompressFormat.PNG;
        private int mTaskCompressQuality = 100;
        private boolean mTaskIsPicSaved = false;
        private Bitmap.Config mTaskBitmapConfig;
        private TunedBitmapCacheKey mTaskTunedBitmapCacheKey;
        private String mTaskUserAgent;
        private boolean mIsCancelled = false;
        private boolean mIsFinished = false;
        private Runnable mLoadCachedPicRunnable;
        private PicViewSession mLoadNetworkPicSession;
        private Paint mTaskPaint;

        public PicLoadTask() {
            // 复制会话参数, 保证多线程下参数不受干扰.
            mTaskPicUri = mPicUri;
            mTaskPicDecoder = mPicDecoder;
            mTaskSavePicAs = mSavePicAs;
            mTaskIsPicSaved = mIsPicSaved;
            mTaskCompressFormat = mCompressFormat;
            mTaskCompressQuality = mCompressQuality;
            mTaskBitmapConfig = mBitmapConfig;
            mTaskTunedBitmapCacheKey = getTunedBitmapCacheKey();
            mTaskUserAgent = mUserAgent;
            mTaskPaint = new Paint();
            mTaskPaint.setFilterBitmap(true);
            mTaskPaint.setAntiAlias(true);
        }

        public void start() {
            mLoadCachedPicRunnable = new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    if (!mIsCancelled) {
                        Cache.CacheSlot<Bitmap> cacheSlot = mTunedBitmapCache.aquireCachedSlot(mTaskTunedBitmapCacheKey);
                        if (cacheSlot != null) {
                            mTunedBitmapCache.releaseSlot(cacheSlot);
                            PicView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mIsCancelled) {
                                        onTaskSucceeded();
                                        onTaskClosed();
                                    } else {
                                        onTaskCancelled();
                                        onTaskClosed();
                                    }
                                }
                            });
                        } else {
                            final boolean isLocalPic = isLocalPic(Uri.parse(mTaskPicUri));
                            Bitmap picBitmap = null;
                            BitmapCache.CacheKey cacheKey = new BitmapCache.CacheKey(mTaskPicUri, mTaskBitmapConfig);
                            cacheKey.setPaint(mTaskPaint);
                            cacheSlot = mPicCache.aquireCachedSlot(cacheKey);
                            // 获取图片位图
                            if (cacheSlot == null && isLocalPic) {
                                picBitmap = fetchLocalBitmap();
                                picBitmap = adjustBitmap(picBitmap);
                                cacheSlot = cacheBitmap(picBitmap);
                            }
                            if (picBitmap == null && cacheSlot != null) {
                                picBitmap = cacheSlot.getValue();
                            }
                            saveBitmap(picBitmap);
                            picBitmap = tuneBitmap(picBitmap);
                            final Bitmap resultBitmap = picBitmap;
                            PicView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mIsCancelled) {
                                        if (resultBitmap != null) {
                                            onTaskSucceeded();
                                            onTaskClosed();
                                        } else if (isLocalPic) {
                                            onTaskFailed();
                                            onTaskClosed();
                                        } else {
                                            startLoadNetworkPic();
                                        }
                                    } else {
                                        onTaskCancelled();
                                        onTaskClosed();
                                    }
                                }
                            });
                        }
                    } else {
                        PicView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                onTaskCancelled();
                                onTaskClosed();
                            }
                        });
                    }
                }

                private Bitmap fetchLocalBitmap() {
                    Bitmap picBitmap = null;
                    if (mTaskPicDecoder != null) {
                        picBitmap = mTaskPicDecoder.decodePic(mTaskPicUri, null);
                    }
                    if (picBitmap == null) {
                        picBitmap = BitmapFactory.decodeFile(Uri.parse(mTaskPicUri).getPath());
                    }
                    return picBitmap;
                }
            };
            mPicLoadTaskExecutor.schedule(mLoadCachedPicRunnable, 0, TimeUnit.MILLISECONDS);
        }

        public void cancel() {
            mIsCancelled = true;
            if (mLoadNetworkPicSession != null) {
                mLoadNetworkPicSession.close();
            }
        }

        public boolean isFinished() {
            return mIsFinished;
        }

        private void startLoadNetworkPic() {
            mLoadNetworkPicSession = new PicViewSession() {
                @Override
                protected void onSessionTry() throws Exception {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    if (!mIsCancelled) {
                        Bitmap picBitmap = null;
                        BitmapCache.CacheKey cacheKey = new BitmapCache.CacheKey(mTaskPicUri, mTaskBitmapConfig);
                        cacheKey.setPaint(mTaskPaint);
                        Cache.CacheSlot<Bitmap> cacheSlot = mPicCache.aquireCachedSlot(cacheKey);
                        if (cacheSlot == null) {
                            picBitmap = fetchNetworkBitmap();
                            picBitmap = adjustBitmap(picBitmap);
                            cacheSlot = cacheBitmap(picBitmap);
                        }
                        if (picBitmap == null && cacheSlot != null) {
                            picBitmap = cacheSlot.getValue();
                        }
                        if (cacheSlot != null) mPicCache.releaseSlot(cacheSlot);
                        saveBitmap(picBitmap);
                        picBitmap = tuneBitmap(picBitmap);
                        // 检查图片是否获取成功
                        if (picBitmap == null) {
                            throw new WebSessionException();
                        }
                    }
                }

                @Override
                protected void onSessionSucceeded() {
                    if (!mIsCancelled) {
                        onTaskSucceeded();
                    } else {
                        onTaskCancelled();
                    }
                }

                @Override
                protected void onSessionCancelled() {
                    onTaskCancelled();
                }

                @Override
                protected void onSessionFailed() {
                    onTaskFailed();
                }

                @Override
                protected void onSessionClosed() {
                    onTaskClosed();
                }

                private Bitmap fetchNetworkBitmap() throws Exception {
                    Bitmap picBitmap = null;
                    if (mTaskPicDecoder != null) {
                        HttpGet httpRequest = new HttpGet(mTaskPicUri);
                        HttpResponse httpResponse = execute(httpRequest);
                        HttpEntity entity = httpResponse.getEntity();

                        InputStream picStream = entity.getContent();
                        picBitmap = mTaskPicDecoder.decodePic(mTaskPicUri, picStream);
                        picStream.close();
                        entity.consumeContent();
                    }

                    if (picBitmap == null) {
                        WebService picService = new WebService(this);
                        picBitmap = picService.getBitmapContent(mTaskPicUri);
                    }

                    return picBitmap;
                }
            };

            mLoadNetworkPicSession.setDefaultUserAgent(mTaskUserAgent);
            mLoadNetworkPicSession.open(CacheStrategy.DISABLE_CACHE);
        }

        private Bitmap tuneBitmap(Bitmap originalPicBitmap) {
            if (originalPicBitmap == null) {
                return null;
            }

            final Bitmap tunedBitmap;
            final Rect dstRect = new Rect(0, 0, mTaskTunedBitmapCacheKey.mDstWidth, mTaskTunedBitmapCacheKey.mDstHeight);
            final Rect drawingRect = new Rect();
            final RectF drawingRectF = new RectF();
            synchronized (originalPicBitmap) {
                if (mTaskTunedBitmapCacheKey.mCornerRadius > 0) {
                    tunedBitmap = Public.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(tunedBitmap);
                    final int color = 0xff424242;
                    final Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    canvas.drawARGB(0, 0, 0, 0);
                    paint.setColor(color);
                    drawingRectF.set(dstRect);
                    canvas.drawRoundRect(drawingRectF, mTaskTunedBitmapCacheKey.mCornerRadius, mTaskTunedBitmapCacheKey.mCornerRadius, paint);
                    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                    drawingRect.set(dstRect);
                    PicView.drawPicAtDstRect(canvas, drawingRect, originalPicBitmap, paint, mTaskTunedBitmapCacheKey.mPicStretch);
                    mTaskTunedBitmapCacheKey.setPicRectInDstArea(new Rect(drawingRect));
                } else {
                    final float[] scales = new float[]{1.0f, 1.0f};
                    scales[0] = 1.0f;
                    scales[1] = 1.0f;
                    PicView.calcPicScales(scales, dstRect.width(), dstRect.height(), originalPicBitmap.getWidth(), originalPicBitmap.getHeight(), mTaskTunedBitmapCacheKey.mPicStretch);
                    boolean isSizeTuned = scales[0] < 0.9f && scales[1] < 0.9f;
                    if (isSizeTuned) {
                        Rect picRectInDstRect = new Rect();
                        PicView.calcPicRectInDstRect(picRectInDstRect, dstRect, originalPicBitmap.getWidth(), originalPicBitmap.getHeight(), mTaskTunedBitmapCacheKey.mPicStretch);
                        if (picRectInDstRect.left < 0) {
                            picRectInDstRect.left = 0;
                        }
                        if (picRectInDstRect.top < 0) {
                            picRectInDstRect.top = 0;
                        }
                        if (picRectInDstRect.right > dstRect.width()) {
                            picRectInDstRect.right = dstRect.width();
                        }
                        if (picRectInDstRect.bottom > dstRect.height()) {
                            picRectInDstRect.bottom = dstRect.height();
                        }
                        tunedBitmap = Public.createBitmap(picRectInDstRect.width(), picRectInDstRect.height(), mTaskBitmapConfig);
                        Canvas canvas = new Canvas(tunedBitmap);
                        canvas.clipRect(picRectInDstRect);
                        PicView.drawPicAtDstRect(canvas, dstRect, originalPicBitmap, mTaskPaint, mTaskTunedBitmapCacheKey.mPicStretch);
                        mTaskTunedBitmapCacheKey.setPicRectInDstArea(picRectInDstRect);
                    } else {
                        tunedBitmap = originalPicBitmap.copy(originalPicBitmap.getConfig(), true);
                        mTaskTunedBitmapCacheKey.setPicRectInDstArea(new Rect(0, 0, dstRect.width(), dstRect.height()));
                    }
                }
            }

            Cache.CacheSlot<Bitmap> tunedBitmapCacheSlot = mTunedBitmapCache.aquireNewSlot(mTaskTunedBitmapCacheKey, tunedBitmap);
            if (tunedBitmapCacheSlot != null) {
                mTunedBitmapCache.releaseSlot(tunedBitmapCacheSlot);
            }

            return tunedBitmap;
        }

        private final boolean isLocalPic(Uri picUri) {
            return picUri.getScheme().equalsIgnoreCase("file");
        }

        private Bitmap adjustBitmap(Bitmap picBitmap) {
            if (picBitmap != null) {
                synchronized (picBitmap) {
                    Bitmap.Config originalConfig = picBitmap.getConfig();
                    if (mTaskBitmapConfig != null && originalConfig != mTaskBitmapConfig) {
                        Bitmap convertedBitmap = Public.createBitmap(picBitmap.getWidth(), picBitmap.getHeight(), mTaskBitmapConfig);
                        Canvas canvas = new Canvas(convertedBitmap);
                        Rect rect = new Rect(0, 0, picBitmap.getWidth(), picBitmap.getHeight());
                        canvas.drawBitmap(picBitmap, rect, rect, mTaskPaint);
                        picBitmap.recycle();
                        picBitmap = convertedBitmap;
                    }
                }
            }
            return picBitmap;
        }

        private Cache.CacheSlot<Bitmap> cacheBitmap(Bitmap picBitmap) {
            if (picBitmap != null) {
                return mPicCache.aquireNewSlot(new BitmapCache.CacheKey(mTaskPicUri, mTaskCompressFormat, mTaskCompressQuality, mTaskBitmapConfig), picBitmap);
            } else {
                return null;
            }
        }

        private void saveBitmap(Bitmap picBitmap) {
            // 保存图片位图
            if (mTaskSavePicAs != null && mTaskIsPicSaved == false && picBitmap != null) {
                mTaskIsPicSaved = saveBitmapAs(mTaskSavePicAs, picBitmap, mTaskCompressFormat, mTaskCompressQuality);
            }
        }

        private final boolean saveBitmapAs(File saveAsFile, Bitmap bitmap, CompressFormat compressFormat, int compressQuality) {
            if (saveAsFile != null) {
                FileOutputStream fileStream = null;
                try {
                    fileStream = new FileOutputStream(saveAsFile);
                    synchronized (bitmap) {
                        bitmap.compress(compressFormat, compressQuality, fileStream);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    try {
                        fileStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
            return false;
        }

        private void onTaskSucceeded() {
            if (mPicLoadTask == PicLoadTask.this) {
                invalidate();
            }
        }

        private void onTaskFailed() {
            if (mPicLoadTask == PicLoadTask.this) {
                mHasPicError = true;
                notifyPicError();
                requestLayout();
                invalidate();
            }
        }

        private void onTaskCancelled() {
        }

        private void onTaskClosed() {
            if (mPicLoadTask == PicLoadTask.this) {
                mPicLoadTask = null;
                mIsPicSaved = mTaskIsPicSaved;
            }
            mIsFinished = true;
        }
    }

    public static void clearPicCache() {
        mTunedBitmapCache.clear();
        mPicCache.clear();
        System.gc();
    }

    public static void clearPicBitmaps() {
        mTunedBitmapCache.clear();
        mPicCache.clear();
        synchronized (mAllUsedPicViews) {
            try {
                acquireLockOfTunedBitmaps();
                for (PicView picView : mAllUsedPicViews) {
                    if (picView.mLocalTunedBitmap != null) {
                        picView.mLocalTunedBitmap.recycle();
                    }
                    picView.mLocalTunedBitmap = null;
                    picView.mLocalTunedBitmapCacheKey = null;
                    picView.postInvalidate();
                }
            } finally {
                releaseLockOfTunedBitmaps();
            }
        }
        System.gc();
    }

    private static void acquireLockOfTunedBitmaps() {
        while (!mTunedBitmapLock.compareAndSet(false, true)) {
        }
    }

    private static void releaseLockOfTunedBitmaps() {
        mTunedBitmapLock.set(false);
    }

    // ### 构造函数 ###
    public PicView(Context context) {
        this(context, null);
    }

    public PicView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint.setFilterBitmap(true);
        mPaint.setAntiAlias(true);

        setWillNotDraw(false);
        setWillNotCacheDrawing(false);
        setDrawingCacheEnabled(false);
        setPicVisibleState(getVisibility() == VISIBLE);
    }

    // ### 属性 ###
    public final Drawable getPicForeground() {
        return mPicForegroundDrawable;
    }

    public final void setPicForeground(Drawable drawable) {
        if (mPicForegroundDrawable != drawable) {
            mPicForegroundDrawable = drawable;
            invalidate();
        }
    }

    public final void setDefaultPic(int resId) {
        mDefaultPicDrawable = getResources().getDrawable(resId);
        invalidate();
    }

    public final void setDefaultPic(Drawable defaultPic) {
        mDefaultPicDrawable = defaultPic;
        invalidate();
    }

    public final String getPicUri() {
        return mPicUri;
    }

    public final void setPicUri(String picUri) {
        if (TextUtils.equals(mPicUri, picUri) == false) {
            closePicSession();

            mPicUri = picUri;
            mCacheKey = null;
            mIsPicSaved = false;
            mHasPicError = false;
            invalidate();
            requestLayoutIfNeeded();
        }
    }

    public final void setUserAgent(String userAgent) {
        mUserAgent = userAgent;
    }

    public final void setPicStretch(PicStretch picStretch) {
        if (mPicStretch != picStretch) {
            mPicStretch = picStretch;

            invalidate();
        }
    }

    public final void setPicListener(PicViewListener picListener) {
        mPicListener = picListener;
    }

    public final void setPicDecoder(PicViewDecoder picDecoder) {
        mPicDecoder = picDecoder;
    }

    public final void setSavePicAs(File saveAsFile) {
        mSavePicAs = saveAsFile;
    }

    public final void setCompressFormat(CompressFormat compressFormat) {
        mCompressFormat = compressFormat;
    }

    public final void setCompressQuality(int compressQuality) {
        mCompressQuality = compressQuality;
    }

    public final void setBitmapConfig(Bitmap.Config config) {
        if (mBitmapConfig != config) {
            mBitmapConfig = config;
        }
    }

    public final void setCornerRadius(float radius) {
        mCornerRadius = radius;
    }

    // ### 重写函数 ###
    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap picBitmap = null;
        TunedBitmapCacheKey picKey = null;

        if (!mHasPicError && !TextUtils.isEmpty(mPicUri)) {
            final TunedBitmapCacheKey newKey = getTunedBitmapCacheKey();
            if (!newKey.equals(mLocalTunedBitmapCacheKey)) {
                mLocalTunedBitmap = null;
                mLocalTunedBitmapCacheKey = null;
            }
            if (mLocalTunedBitmap != null) {
                picBitmap = mLocalTunedBitmap;
                picKey = mLocalTunedBitmapCacheKey;
            }
            if (picBitmap == null) {
                final Pair<Bitmap, TunedBitmapCacheKey> picBitmapPair = getTunedBitmap();
                picBitmap = picBitmapPair == null ? null : picBitmapPair.first;
                picKey = picBitmapPair == null ? null : picBitmapPair.second;
                if (this.isHardwareCanvas(canvas)) {
                    mLocalTunedBitmap = picBitmap;
                    mLocalTunedBitmapCacheKey = picKey;
                }
            }
        }

        try {
            acquireLockOfTunedBitmaps();
            if (picBitmap != null && !picBitmap.isRecycled()) {
                final Rect dstRect = new Rect();
                calcDstRect(dstRect);
                dstRect.offset(picKey.getPicRectInDstArea().left, picKey.getPicRectInDstArea().top);
                dstRect.right = dstRect.left + picKey.getPicRectInDstArea().width();
                dstRect.bottom = dstRect.top + picKey.getPicRectInDstArea().height();
                drawPicAtDstRect(canvas, dstRect, picBitmap, mPaint);
            } else {
                calcPicRect(mDrawingRect, getWidth(), getHeight());
                if (mDefaultPicDrawable != null) {
                    mDefaultPicDrawable.setBounds(mDrawingRect.left, mDrawingRect.top, mDrawingRect.right, mDrawingRect.bottom);
                    mDefaultPicDrawable.draw(canvas);
                }
            }
        } finally {
            releaseLockOfTunedBitmaps();
        }

        // 绘制图片前景
        if (mPicForegroundDrawable != null) {
            mPicForegroundDrawable.setBounds(mDrawingRect.left, mDrawingRect.top, mDrawingRect.right, mDrawingRect.bottom);
            mPicForegroundDrawable.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 测量图片尺寸
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        boolean isPicVisible = visibility == VISIBLE;
        setPicVisibleState(isPicVisible);
        if (isPicVisible == false) {
            closePicSession();
        } else {
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        synchronized (mAllUsedPicViews) {
            mAllUsedPicViews.add(this);
        }
        setPicVisibleState(getVisibility() == VISIBLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLocalTunedBitmap = null;
        closePicSession();
        setPicVisibleState(false);
        synchronized (mAllUsedPicViews) {
            mAllUsedPicViews.remove(this);
        }
    }

    private TunedBitmapCacheKey getTunedBitmapCacheKey() {
        Rect dstRect = new Rect();
        calcDstRect(dstRect);
        return new TunedBitmapCacheKey(mPicUri, dstRect.width(), dstRect.height(), mPicStretch, mCornerRadius);
    }

    private Pair<Bitmap, TunedBitmapCacheKey> getTunedBitmap() {
        TunedBitmapCacheKey cacheKey = getTunedBitmapCacheKey();
        Cache.CacheSlot<Bitmap> cacheSlot = mTunedBitmapCache.aquireCachedSlot(cacheKey);
        if (cacheSlot != null) {
            mTunedBitmapCache.releaseSlot(cacheSlot);
            if (cacheSlot.getValue() != null) {
                return new Pair<Bitmap, TunedBitmapCacheKey>(cacheSlot.getValue(), (TunedBitmapCacheKey) cacheSlot.getKey());
            }
        }
        this.openPicSession();
        return null;
    }

    // ### 实现函数 ###
    private final void notifyPicError() {
        if (mPicListener != null) {
            mPicListener.onPicError(this);
        }
    }

    private final BitmapCache.CacheKey getCacheKey() {
        if (mCacheKey != null) return mCacheKey;

        mCacheKey = new BitmapCache.CacheKey(mPicUri, mCompressFormat, mCompressQuality, mBitmapConfig);
        return mCacheKey;
    }

    @SuppressLint("NewApi")
    private final boolean isHardwareCanvas(Canvas canvas) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && canvas.isHardwareAccelerated();
    }

    private final void drawPicAtDstRect(Canvas canvas, Rect dstRect, Bitmap picBitmap, Paint paint) {
        drawPicAtDstRect(canvas, dstRect, picBitmap, paint, mPicStretch);
    }

    private static final void drawPicAtDstRect(Canvas canvas, Rect dstRect, Bitmap picBitmap, Paint paint, PicStretch picStretch) {
        // 计算图片最终尺寸及位置
        Rect picRect = new Rect();
        calcPicRectInDstRect(picRect, dstRect, picBitmap.getWidth(), picBitmap.getHeight(), picStretch);
        canvas.save();
        canvas.clipRect(dstRect);
        canvas.drawBitmap(picBitmap, new Rect(0, 0, picBitmap.getWidth(), picBitmap.getHeight()), picRect, paint);
        canvas.restore();
    }

    private final void calcDstRect(Rect dstRect) {
        final int dstX = getPaddingLeft();
        final int dstY = getPaddingTop();
        final int dstWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int dstHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        dstRect.set(dstX, dstY, dstWidth, dstHeight);
    }

    private final void calcPicRect(Rect picRect, int picWidth, int picHeight) {
        Rect dstRect = new Rect();
        calcDstRect(dstRect);
        calcPicRectInDstRect(picRect, dstRect, picWidth, picHeight);
    }

    private final void calcPicRectInDstRect(Rect picRectInDstRect, Rect dstRect, int picWidth, int picHeight) {
        final float[] scales = new float[]{1.0f, 1.0f};
        calcPicScales(scales, dstRect.width(), dstRect.height(), picWidth, picHeight);

        final float finalWidth = picWidth * scales[0];
        final float finalHeight = picHeight * scales[1];
        final float finalX;
        final float finalY;
        switch (mPicStretch) {
            case CENTER:
            case SCALE_CROP:
            case SCALE_INSIDE:
            case SCALE_FILL:
                finalX = dstRect.left + (dstRect.width() - finalWidth) / 2.0f;
                finalY = dstRect.top + (dstRect.height() - finalHeight) / 2.0f;
                break;
            default:
                finalX = 0.0f;
                finalY = 0.0f;
                assert false;
        }

        picRectInDstRect.set((int) finalX, (int) finalY, (int) finalWidth, (int) finalHeight);
        // picRectInDstRect.set(dstRect.left, dstRect.top, dstRect.width(),
        // dstRect.height());
    }

    private static final void calcPicRectInDstRect(Rect picRectInDstRect, Rect dstRect, int picWidth, int picHeight, PicStretch picStretch) {
        final float[] scales = new float[]{1.0f, 1.0f};
        calcPicScales(scales, dstRect.width(), dstRect.height(), picWidth, picHeight, picStretch);

        final float finalWidth = picWidth * scales[0];
        final float finalHeight = picHeight * scales[1];
        final float finalX;
        final float finalY;
        switch (picStretch) {
            case CENTER:
            case SCALE_CROP:
            case SCALE_INSIDE:
            case SCALE_FILL:
                finalX = dstRect.left + (dstRect.width() - finalWidth) / 2.0f;
                finalY = dstRect.top + (dstRect.height() - finalHeight) / 2.0f;
                break;
            default:
                finalX = 0.0f;
                finalY = 0.0f;
                assert false;
        }

        picRectInDstRect.set((int) finalX, (int) finalY, (int) finalWidth, (int) finalHeight);
    }

    private final void calcPicScales(float[] scales, int dstWidth, int dstHeight, int picWidth, int picHeight) {
        calcPicScales(scales, dstWidth, dstHeight, picWidth, picHeight, mPicStretch);
    }

    private static final void calcPicScales(float[] scales, int dstWidth, int dstHeight, int picWidth, int picHeight, PicStretch picStretch) {
        switch (picStretch) {
            case CENTER:
                scales[0] = scales[1] = 1.0f;
                break;
            case SCALE_CROP:
                float maxScale = Math.max((float) dstWidth / picWidth, (float) dstHeight / picHeight);
                scales[0] = scales[1] = maxScale;
                break;
            case SCALE_INSIDE:
                float minScale = Math.min((float) dstWidth / picWidth, (float) dstHeight / picHeight);
                scales[0] = scales[1] = minScale;
                break;
            case SCALE_FILL:
                scales[0] = (float) dstWidth / picWidth;
                scales[1] = (float) dstHeight / picHeight;
                break;
            default:
                assert false;
                scales[0] = scales[1] = 1.0f;
        }
    }

    private final void openPicSession() {
        if (mPicLoadTask != null && !mPicLoadTask.isFinished()) return;
        mPicLoadTask = new PicLoadTask();
        mPicLoadTask.start();
    }

    private final void closePicSession() {
        if (mPicLoadTask != null) {
            mPicLoadTask.cancel();
            mPicLoadTask = null;
        }
    }

    private final void requestLayoutIfNeeded() {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null && (params.width == ViewGroup.LayoutParams.WRAP_CONTENT || params.height == ViewGroup.LayoutParams.WRAP_CONTENT)) {
            requestLayout();
        }
    }

    private final void setPicVisibleState(boolean isPicVisible) {
        if (mIsPicVisible != isPicVisible) {
            mIsPicVisible = isPicVisible;
        }
    }
}
