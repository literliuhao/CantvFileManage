package com.cantv.media.center.utils;

import android.util.Log;

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
        productmodeList = Arrays.asList("F55", "U65", "W55", "F55S", "V50S", "V43S", "X55", "C43S", "C43", "C42S", "C32K", "C40K", "C50S", "C49C", "C49S", "CANbox C1", "CANbox F1", "CANbox F2", "CANbox Z1", "QZH5");
    }


    public static String get(String key) {
        String value = "";
        Class<?> cls = null;
        try {
            cls = Class.forName("android.os.SystemProperties");
            Method hideMethod = cls.getMethod("get", String.class);
            Object object = cls.newInstance();
            value = (String) hideMethod.invoke(object, key);
        } catch (Exception e) {
            Log.e("SystemCateUtil", "get error() ", e);
        }

        return value;
    }

    /**
     * 获取产品型号
     *
     * @return
     */
    public static String productModel() {
        Log.i("SystemCateUtil", get("ro.product.model"));
        return get("ro.product.model");
    }

    /**
     * 判断是否有当前型号/或者对当前型号进行特殊处理
     *
     * @return
     */
    public static boolean isContainsCurrModel() {
        Log.i("SystemCateUtil", "productmodeList.contains(productModel() " + productmodeList.contains(productModel()));
        return productmodeList.contains(productModel());
    }

    /**
     * 得到系统版本(固件版本)
     *
     * @return
     */
    public static String getSystemVersion() {
        Log.i("SystemCateUtil", get("ro.build.version.firmware"));
        return get("ro.build.version.firmware");
    }


    public static Boolean isNewVersion(){
        String versionName = getSystemVersion();
        Float version = Float.valueOf(versionName.substring(versionName.lastIndexOf("V") + 1));
        if(version > 1.1f){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 需要特殊处理的内部机型
     */
    public static Boolean specialModel(String productModel) {
        if (productModel.equals("X55")) {
            return true;
        } else {
            return false;
        }
    }

}
