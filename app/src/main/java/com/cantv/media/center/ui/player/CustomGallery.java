package com.cantv.media.center.ui.player;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.GridView;
import android.widget.ImageView;

public class CustomGallery extends GridView {
    private int galleryCenterPoint;        // gallery的中心点
    private final int maxRotateAngle = 50;    // 最大旋转角度
    private Camera camera;

    public CustomGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStaticTransformationsEnabled(true);
        camera = new Camera();
    }

    /**
     * 当某一个图片需要变换时回调
     */
    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        int viewCenterPoint = getViewCenterPoint(child);
        int rotateAngle = 0;
        if (galleryCenterPoint != viewCenterPoint) {        // 代表当前是两边的图片
            float scale = (float) (galleryCenterPoint - viewCenterPoint) / (float) child.getWidth();
            rotateAngle = (int) (scale * maxRotateAngle);
            if (Math.abs(rotateAngle) > maxRotateAngle) {        // -51 -60  55  66
                rotateAngle = rotateAngle > 0 ? maxRotateAngle : -maxRotateAngle;
            }
        } else {
            rotateAngle = 0;
        }

        // 清楚之前变换的设置
        t.clear();
        // 重置变换的类型为矩阵类型
        t.setTransformationType(Transformation.TYPE_MATRIX);

        transformationItem((ImageView) child, rotateAngle, t);
        return true;
    }

    /**
     * 设置每一个item的变换
     *
     * @param iv
     * @param rotateAngle
     * @param t
     */
    private void transformationItem(ImageView iv, int rotateAngle, Transformation t) {
        camera.save();        // 保存摄像机之前的状态
        camera.translate(0, 0, 100f);
        int absRotateAngle = Math.abs(rotateAngle);        // 取得旋转角度的绝对值

        /**
         * 放大图片
         * 中间的图片的zoom值为: -250
         * 45 * 1.5 = 70 - 250 = -180
         * 50 * 1.5 = 75 - 250 = -175
         */
        int zoom = (int) (absRotateAngle * 1.5 - 250);
        camera.translate(0, 0, zoom);

        /**
         * 设置图片的透明度
         * setAlpha 的取值范围: 1 ~ 255
         * 1 	完全透明
         * 255	完全显示
         *
         * 中间的图片: 255 - 0 * 2.5 = 255
         * 两边的图片: 255 - 50 * 2.5 = 130
         */
        int alpha = (int) (255 - absRotateAngle * 2.5);
        iv.setAlpha(alpha);

        // 设置旋转
        camera.rotateY(rotateAngle);

        Matrix matrix = t.getMatrix();
        camera.getMatrix(matrix);        // 把上面一些列的操作封装到矩阵中

        // 执行所有变换之前把原图移动到: 左边, 一半宽度的距离, 上边, 一半高度的距离
        matrix.preTranslate(-iv.getWidth() / 2, -iv.getHeight() / 2);

        // 执行所有变换之后把原图移动到初始位置: 右边, 一半宽度的距离, 下边度, 一半高度的距离
        matrix.postTranslate(iv.getWidth() / 2, iv.getHeight() / 2);

        camera.restore();    // 恢复到保存之前的状态
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        galleryCenterPoint = getGalleryCenterPoint();
    }

    /**
     * 获得Gallery的中心点
     *
     * @return
     */
    private int getGalleryCenterPoint() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    /**
     * 获得图片的中心点
     *
     * @param v
     * @return
     */
    private int getViewCenterPoint(View v) {
        return v.getWidth() / 2 + v.getLeft();
    }
}
