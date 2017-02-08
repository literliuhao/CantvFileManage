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

    public static String model;
    public static String systemVersion;
    public static String persis;
    public static String server_data;

    static {
        productmodeList = Arrays.asList("F55", "U65KE660", "W55KE590", "F55SD160", "V50SD160", "V43SD160", "X55KE560", "X55QE190", "C43SD120", "C43",
                "C42SD320", "C32KD210", "C32KD110", "C40KD120", "C50SD120", "C49CD120", "C49SD320", "CANbox C1", "CANbox Z1", "CANbox F1",
                "CANbox F2", "Can C1", "Can C2", "Can C3", "Can C4", "Can C5", "Can C6", "Can C7", "Can C8", "Can C9",
                "changhong_F3", "AMOI_B5", "AMOI_B6", "AMOI_B8", "AMOI_B9", "Linkin_H3", "Linkin_H6", "mohe_TVB10", "mohe_TVB11", "MALATA_F4",
                "JOHE.LIVE_F6", "QHTF_F5", "EARISE_K1", "YUANJING_V30", "EARISE_K2", "EARISE_K3", "EARISE_K5", "EARISE_K6", "EARISE_K8", "EARISE_K9",
                "WZHT_OS", "EARISE_M1", "EARISE_M2", "EARISE_M3", "EARISE_M5", "EARISE_M6", "EARISE_M8", "EARISE_M9", "LW8000U7", "ctv638",
                "JRX338", "TM6", "TM6_64", "HiDPTAndroid", "TT338512", "TT3381G", "TT6381GB", "H8", "H9", "N8",
                "N360", "T8", "R1", "DYOS", "SP3811", "JCG-Lebo-H3", "ADA.S338A", "X6", "HK-T.RT2968P61");
    }


    public static String get(String key) {
        String value = "";
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
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
        if (null == model || model.equals("")) {
            model = get("ro.product.model");
        }
        return model;
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
        if (null == systemVersion || systemVersion.equals("")) {
            systemVersion = get("ro.build.version.firmware");
        }
        Log.i("SystemCateUtil", get("ro.build.version.firmware"));
        return systemVersion;
    }

    /**
     * 或许系统设置，当为1时不弹出U盘对话框
     *
     * @return
     */
    public static String getPersist() {
        if (null == persis || persis.equals("")) {
            persis = get("persist.sys.burningmode");
        }
        return persis;
    }

    /**
     * 表示是否连通服务器:
     * 1 表示已经连通服务器，否则为未连同服务器
     *
     * @return
     */
    public static String getServerData() {
        if (getEnable()) {
            String status = get("cantv.sys.has_server_data");
            Log.i("SystemCateUtil", "getServerData  = " + status);
            if (null == server_data || server_data.equals("")) {
                if (status.equals("1")) {
                    server_data = "1";
                } else {
                    return status;
                }
            }
        } else {
            return "1";
        }
        return server_data;
    }

    /**
     * 表示是否需要无网弹窗功能:  0 表示开（默认） 1 表示关
     *
     * @return
     */
    public static Boolean getEnable() {
        if (get("persist.sys.network.enable").equals("0")) {
            return true;
        }
        return false;

    }

    /**
     * 用于判断系统版本号是否大于1.2版本
     *
     * @return
     */
    public static Boolean isNewVersion() {
        try {
            String versionName = getSystemVersion();
            if (null != versionName && !versionName.equals("")) {
                //V1.2.0
                Log.i("SystemCateUtil", "versionName  = " + versionName);
                Float version = Float.valueOf(versionName.substring(versionName.lastIndexOf("V") + 1));
                Log.i("SystemCateUtil", "SubString version = " + version);
                return version > 1.1f;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 需要特殊处理的内部机型
     */
    public static Boolean specialModel(String productModel) {
        return productModel.equals("X55");
    }

}
