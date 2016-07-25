package com.cantv.media.center.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yibh on 2016/6/28.
 */

public class DateUtil {
    /**
     * date转换成String类型
     *
     * @param date
     * @param format 可选参数,传递将要转换的日期格式,默认是"yyyy-MM-dd"
     * @return
     */
    public static String onDate2String(Date date, String... format) {
        String defaFormat = "";
        if (format != null && format.length > 0) {
            defaFormat = new SimpleDateFormat(format[0]).format(date);
        } else {
            defaFormat = new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return defaFormat;
    }

    public static String longToDate(Long millSec, String dateFormat) {
        if (null == dateFormat) {
            dateFormat = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(millSec);
        return sdf.format(date);
    }
}
