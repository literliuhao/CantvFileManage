package com.cantv.media.center.utils;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class ImageUtils {

	// 存储图片的集合(存在内存中)
	private static Hashtable<Integer, SoftReference<Bitmap>> mCacheBitmapTable = new Hashtable<Integer, SoftReference<Bitmap>>();

	/**
	 * 根据id获得bitmap对象, 注意: 采用了SoftReference缓存,
	 * 
	 * @param context
	 * @param imageID
	 * @return 先去集合中根据imageID去取相对应的图片, 如果有, 直接返回 如果没有,
	 *         调用getInvertImage方法得到一个对象返回
	 */
	public static Bitmap getBitmap(Context context, int imageID, int position) {

		SoftReference<Bitmap> softReference = mCacheBitmapTable.get(imageID);
		if (softReference != null) {
			Bitmap bitmap = softReference.get();
			if (bitmap != null) {
				Log.i("ImageUtils", "从内存中取: " + position);
				return bitmap;
			}
		}

		Log.i("ImageUtils", "重新加载: " + position);
		Bitmap invertImage = getInvertImage(context, imageID);

		// 取出来对应的图片之后, 往内存中存一份, 为了方便下次直接去内存中取
		mCacheBitmapTable.put(imageID, new SoftReference<Bitmap>(invertImage));
		return invertImage;
	}

	/**
	 * 根据给定的id获取处理(倒影, 倒影渐变)过的bitmap
	 * 
	 * @param imageID
	 * @return
	 */
	private static Bitmap getInvertImage(Context context, int imageID) {
		// 获得原图
		Options opts = new Options();
		opts.inSampleSize = 2;
		Bitmap sourceBitmap = BitmapFactory.decodeResource(context.getResources(), imageID, opts);

		// 倒影图片
		Matrix matrix = new Matrix();
		// 设置图片的反转为, 垂直反转
		matrix.setScale(1.0f, -1.0f);
		// float[] values = {
		// 1.0f, 0f, 0f,
		// 0f, -1.0f, 0f,
		// 0f, 0f, 1.0f
		// };
		// 倒影图片
		Bitmap invertBitmap = Bitmap.createBitmap(sourceBitmap, 0, sourceBitmap.getHeight() / 2, sourceBitmap.getWidth(), sourceBitmap.getHeight() / 2, matrix, false);

		// 合成一张图片
		Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), (int) (sourceBitmap.getHeight() * 1.5 + 5), Config.ARGB_8888);

		// 把原图添加到合成图片的左上角
		Canvas canvas = new Canvas(resultBitmap); // 指定画板画在合成图片上
		canvas.drawBitmap(sourceBitmap, 0, 0, null); // 把原图绘制到合成图上

		// 把倒影图片绘制到合成图片上
		// canvas.drawBitmap(invertBitmap, 0, sourceBitmap.getHeight() + 5,
		// null);

		Rect rect = new Rect(0, sourceBitmap.getHeight() + 5, resultBitmap.getWidth(), resultBitmap.getHeight());
		Paint paint = new Paint();

		/**
		 * TileMode.CLAMP 指定渲染边界以外的控件以最后的那个颜色继续往下渲染
		 */
		LinearGradient shader = new LinearGradient(0, sourceBitmap.getHeight() + 5, 0, resultBitmap.getHeight(), 0x70FFFFFF, 0x00FFFFFF, TileMode.CLAMP);

		// 设置为取交集模式
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// 指定渲染器为线性渲染器
		paint.setShader(shader);
		canvas.drawRect(rect, paint);

		return resultBitmap;
	}

	public static Bitmap createImageThumbnail(String filePath) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inSampleSize = computeSampleSize(opts, -1, 1920 * 1080);
		opts.inJustDecodeBounds = false;

		try {
			bitmap = BitmapFactory.decodeFile(filePath, opts);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return bitmap;
	}

	// 查看Android源码，我们得知，为了得到恰当的inSampleSize，Android提供了一种动态计算的方法。
	// public static int computeSampleSize(BitmapFactory.Options options, int
	// minSideLength, int maxNumOfPixels) {
	// int initialSize = computeInitialSampleSize(options, minSideLength,
	// maxNumOfPixels);
	// int roundedSize;
	// if (initialSize <= 8) {
	// roundedSize = 1;
	// while (roundedSize < initialSize) {
	// roundedSize <<= 1;
	// }
	// } else {
	// roundedSize = (initialSize + 7) / 8 * 8;
	// }
	// return roundedSize;
	// }
	//
	// private static int computeInitialSampleSize(BitmapFactory.Options
	// options, int minSideLength, int maxNumOfPixels) {
	// double w = options.outWidth;
	// double h = options.outHeight;
	// int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w
	// * h / maxNumOfPixels));
	// int upperBound = (minSideLength == -1) ? 128 : (int)
	// Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
	// if (upperBound < lowerBound) {
	// // return the larger one when there is no overlapping zone.
	// return lowerBound;
	// }
	// if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
	// return 1;
	// } else if (minSideLength == -1) {
	// return lowerBound;
	// } else {
	// return upperBound;
	// }
	// }
	public static Bitmap createBitmap(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = computeSampleSize(opts, -1, 1920 * 1080);
		opts.inJustDecodeBounds = false;
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError err) {
		}

		return bmp;
	}

	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

}
