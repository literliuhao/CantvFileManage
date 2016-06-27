package com.cantv.media.center.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.SourceType;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;

public class Video extends Media {

	public Video(SourceType type, String uri) {
		super(type, uri);
	}

	@Override
	public MediaFormat getMediaFormat() {
		return MediaFormat.VIDEO;
	}

	@Override
	public Bitmap getThumbnails() {
		return getImageThumbnail(1200,1200, android.provider.MediaStore.Video.Thumbnails.MICRO_KIND);
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	@SuppressLint("NewApi")
	private Bitmap getImageThumbnail(int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		String imagePath = getUri();
		bitmap = ThumbnailUtils.createVideoThumbnail(imagePath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;

		// MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		// mmr.setDataSource(imagePath);
		// bitmap = mmr.getFrameAtTime();
		// mmr.release();
		// return bitmap;

		// bitmap = createVideoThumbnail(imagePath);
		// return bitmap;
		//
		// bitmap = createVideoThumbnail(imagePath,600,600);
		// return bitmap;

	}

	@SuppressLint("NewApi")
	private Bitmap createVideoThumbnail(String url, int width, int height) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		int kind = MediaStore.Video.Thumbnails.MINI_KIND;
		try {
			if (Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(url, new HashMap<String, String>());
			} else {
				retriever.setDataSource(url);
			}
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		if (kind == MediaStore.Video.Thumbnails.MICRO_KIND && bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

	public static Bitmap createVideoThumbnail(String filePath) throws InvocationTargetException {
		// MediaMetadataRetriever is available on API Level 8
		// but is hidden until API Level 10
		Class<?> clazz = null;
		Object instance = null;
		try {
			clazz = Class.forName("android.media.MediaMetadataRetriever");
			instance = clazz.newInstance();

			Method method = clazz.getMethod("setDataSource", String.class);
			method.invoke(instance, filePath);

			// The method name changes between API Level 9 and 10.
			if (Build.VERSION.SDK_INT <= 9) {
				return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
			} else {
				byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
				if (data != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					if (bitmap != null)
						return bitmap;
				}
				return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
			}
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} catch (InstantiationException e) {
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalAccessException e) {
		} finally {
			try {
				if (instance != null) {
					clazz.getMethod("release").invoke(instance);
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}
	// public void saveBitmap(Bitmap bitmap) throws IOException
	// {
	// File file = new File("/sdcard/"+"888.jpg");
	// FileOutputStream out;
	// try{
	// out = new FileOutputStream(file);
	// out.flush();
	// out.close();
	// }
	// catch (FileNotFoundException e)
	// {
	// e.printStackTrace();
	// }
	// catch (IOException e)
	// {
	// e.printStackTrace();
	// }
	// }

}
