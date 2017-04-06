package com.cantv.media.center.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 图片高斯模糊
 */
public class BitmapUtils {
    /**
     * 上传服务器时把图片调用下面方法压缩后 保存到临时文件夹 图片压缩后小于200KB，失真度不明显
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static Bitmap revitionImageSize(String path) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
        int i = 0;
        Bitmap bitmap;
        // options.inJustDecodeBounds=true那么将不返回实际的bitmap对象，不给其分配内存空间但是可以得到一些解码边界信息即图片大小等信息
        // outHeight(图片原始高度)和 outWidth(图片的原始宽度)
        // inSampleSize表示缩略图大小为原始图片大小的几分之一
        // options.outWidth >> i(右移运算符)表示：outWidth/(2^i)
        while (true) {
            if ((options.outWidth >> i <= 2000) && (options.outHeight >> i <= 2000)) {
                in = new BufferedInputStream(new FileInputStream(new File(path)));
                options.inSampleSize = (int) Math.pow(2.0D, i); // 幂运算 i为几次方
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(in, null, options);
                break;
            }
            i += 1;
        }
        return bitmap;
    }

    /**
     * 将图片放大或缩小到指定尺寸
     */
    public static Bitmap resizeImage(Bitmap source, int w, int h) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
    }

    /**
     * 将图片剪裁为圆形
     */
    public static Bitmap createCircleImage(Bitmap source) {
        int length = source.getWidth() < source.getHeight() ? source.getWidth() : source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = null;
        try {
            target = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_4444);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(length / 2, length / 2, length / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    /**
     * 图片高斯模糊
     *
     * @param bitmap
     * @return
     */
    public static Drawable blurBitmap(Bitmap bitmap, Context ctx) {

        RenderScript rs = RenderScript.create(ctx);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, compress(bitmap));
        Allocation allOut = allIn;

        blurScript.setRadius(10.0f);

        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        try {
            //TODO 这里有可能发生异常 android.renderscript.RSIllegalArgumentException
            // Copy the final bitmap created by the out Allocation to the outBitmap
            allOut.copyTo(bitmap);
            rs.destroy();

            return new BitmapDrawable(ctx.getResources(), bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap compress(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 1080;
        float ww = 1920;
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 4;
        }
        newOpts.inSampleSize = be;
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);
    }

    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //修复OS-3882文件共享非必现点击登录共享设备时出现一次文件管理停止运行的情况
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }
}
