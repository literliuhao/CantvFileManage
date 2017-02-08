package com.cantv.media.center.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * 图片高斯模糊
 */
public class BitmapUtils {

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
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
        float hh = 1280;
        float ww = 1920;
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
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
