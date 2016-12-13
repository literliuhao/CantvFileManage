package com.cantv.media.center.utils;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yibh on 2016/12/12.
 * 区分不同产品型号的方法
 */

public class SystemCateUtil {

    //保存现有产品的型号
    private static List productmodeList;

    static {
        productmodeList = Arrays.asList("F55", "U65", "W55", "F55S", "V50S", "V43S"
                , "X55", "C43S", "C43", "C42S", "C32K", "C40K", "C50S", "C49C", "C49S"
                , "CANbox C1", "CANbox F1", "CANbox F2", "CANbox Z1", "QZH5");
    }


    public static String get(String key) {
        String value = "";
        Class<?> cls = null;

        try {
            cls = Class.forName("android.os.SystemProperties");
            Method hideMethod = cls.getMethod("get", String.class);
            Object object = cls.newInstance();
            value = (String) hideMethod.invoke(object, key);
        } catch (ClassNotFoundException e) {
            Log.e("zhengyi.wzy", "get error() ", e);
        } catch (NoSuchMethodException e) {
            Log.e("zhengyi.wzy", "get error() ", e);
        } catch (InstantiationException e) {
            Log.e("zhengyi.wzy", "get error() ", e);
        } catch (IllegalAccessException e) {
            Log.e("zhengyi.wzy", "get error() ", e);
        } catch (IllegalArgumentException e) {
            Log.e("zhengyi.wzy", "get error() ", e);
        } catch (InvocationTargetException e) {
            Log.e("zhengyi.wzy", "get error() ", e);
        }

        return value;
    }


    /**
     * 获取产品型号
     *
     * @return
     */
    public static String productModel() {
        return get("ro.product.model");
    }

    /**
     * 判断是否有当前型号/或者对当前型号进行特殊处理
     *
     * @return
     */
    public static boolean isContainsCurrModel() {
        return productmodeList.contains(productModel());
    }

    /**
     * 得到系统版本(固件版本)
     * @return
     */
    public static String getSystemVersion(){
        return android.os.Build.VERSION.INCREMENTAL;
    }

}
