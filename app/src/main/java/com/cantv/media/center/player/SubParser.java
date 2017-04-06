package com.cantv.media.center.player;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 为了解析SUB字幕内容,不带IDX文件的sub内容
 * Created by yibh on 2017/1/22.
 */

public class SubParser {
    private List<SrtBeans> srtList = new ArrayList<>();

    public void onlySubFromPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            return;
        }

        srtList.clear();
        String charset = getCharset(path);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            SrtBeans srt = null;
            String line = null;
            int addLineCount = 0;
            while ((line = reader.readLine()) != null) {
                Log.w("while","");
                //时间格式 {xxx}{xxx}
                if (!line.trim().equals("") && line.trim().startsWith("{")
                        && line.contains("}{")
                        && line.indexOf("{") > line.indexOf("}{")

                        ) {
                    Log.w("内容 ",line);
                    //开始时间
                    String st = line.substring(1, line.indexOf("}")).trim();
                    String substring = line.substring(line.indexOf("}{") + 1);
                    //结束时间
                    String end = substring.substring(0, substring.indexOf("}"));
                    //字幕内容
                    String content = substring.substring(substring.indexOf("}") + 1);
                    try {
                        int tSt = Integer.parseInt(st);
                        int tEnd = Integer.parseInt(end);
                        if (srt == null) {
                            srt = new SrtBeans();
//                            addLineCount = 0;
                        }
                        srt.setBeginTime(tSt);
                        srt.setEndTime(tEnd);
//                        srt.src.add(content);
                        srt.mContent=content;
                        srtList.add(srt);
//                        addLineCount++;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Log.w("异常","解析sub异常");
                        continue;
                    }


                }
            }
//            if (srt != null) {
//                srtList.add(srt);// 把最后一条字幕添加到字幕集中
//                srt = null;
//            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public String getSrtByTime(long timeInMillis) {
//        if (timeInMillis <= 1 * 60 * 1000) {


        // 顺序查找
        for (int i = 0, size = srtList.size(); i < size; i++) {
            SrtBeans beans = srtList.get(i);
            int beginTime = beans.getBeginTime();
            int endTime = beans.getEndTime();
            if (timeInMillis >= beginTime && timeInMillis <= endTime) {
                Log.w("字幕", beans.mContent);
                return beans.mContent;
            }
//                }
        }
//        } else {
//            // 二分查找
//            int index = binarySearch(srtList, timeInMillis);
//            if (index >= 0) {
//                return srtList.get(index).getSrt();
//            }
//        }
        return "";
    }

    public static <T> int binarySearch(List<SrtBeans> list, Long object) {
        if (list == null) {
            throw new NullPointerException("list == null");
        }
        if (list.isEmpty()) {
            return -1;
        }
        int low = 0, mid = list.size(), high = mid - 1, result = 1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            result = list.get(mid).compareTo(object);
            if (result < 0) {
                low = mid + 1;
            } else if (result == 0) {
                return mid;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }


    public static String getCharset(String fileName) {
        String code = "UTF-8";
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(fileName));
            int p = (bis.read() << 8) + bis.read();
            switch (p) {
                case 0xefbb:
                    code = "UTF-8";
                    break;
                case 0xfffe:
                    code = "Unicode";
                    break;
                case 0xfeff:
                    code = "UTF-16BE";
                    break;
                default:
                    code = "GBK";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return code;
    }

    /**
     * 搜索对应时间的字幕
     *
     * @return
     */
    private String searchContent1(SrtBeans beans, long timeInMillis) {
        int beginTime = beans.getBeginTime();
        int endTime = beans.getEndTime();
        if (timeInMillis >= beginTime && timeInMillis <= endTime) {
            Log.w("字幕", beans.getSrt());
            return beans.getSrt();
        } else {
            return "";
        }
    }

}
