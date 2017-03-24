
package com.cantv.media.center.utils;

import com.cantv.media.center.data.LyricInfo;
import com.cantv.media.center.data.LyricInfo.Lyric;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {

    private static final String TAG = "LyricParser";

    public static LyricInfo parseFromLocalPath(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }

        return parseFromFile(file);
    }

    public static LyricInfo parseFromFile(File file) {

        InputStream is = null;
        FileInputStream fis = null;
        try {
            is = new FileInputStream(file);
            String chartName = "";

            /**
             * 下面是解析文件编码格式,根据格式读取歌词
             * 参考:http://www.lai18.com/content/1874723.html?from=cancel
             */
            BufferedInputStream bis = new BufferedInputStream(is);
            bis.mark(4);
            byte[] first3bytes = new byte[5];
            //找到文档的前三个字节并自动判断文档类型。
            bis.read(first3bytes);
            bis.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {// utf-8
                chartName = "UTF-8";
            } else if (first3bytes[0] == (byte) 91 && first3bytes[1] == (byte) 116 && first3bytes[2] == (byte) 105 && first3bytes[4] == (byte) 48) {//utf-8无bom编码
                //本来只比较前三个,发现GBK格式的可能和这个存在相同的情况,通过比较发现第5个字节不同 〒▽〒
                chartName = "UTF-8";
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
                chartName = "unicode";
            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
                chartName = "utf-16be";
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
                chartName = "utf-16le";
            } else {
                chartName = "GBK";
            }
            //此处直接传is会导致歌词不显示
            fis = new FileInputStream(file);
            return parseFromStream(fis, chartName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static LyricInfo parseFromStream(InputStream stream, String chartName) {
        if (stream == null) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, Charset.forName(chartName)));
        String line;
        try {
            LyricInfo lyricInfo = new LyricInfo();
            List<Lyric> lyrics = lyricInfo.getLyrics();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("﻿[ti:")) {
                    lyricInfo.setTitle(line.substring(5, line.length() - 1));
                } else if (line.startsWith("[ar:")) {
                    lyricInfo.setSinger(line.substring(4, line.length() - 1));
                } else if (line.startsWith("[al:")) {
                    lyricInfo.setAlbum(line.substring(4, line.length() - 1));
                } else if (line.startsWith("[t_time:")) {
                    lyricInfo.setDuration(shortTimeStr2Long(line.substring(9, line.length() - 1)));
                } else {
                    parseLine(lyrics, line);
                }
            }
            Collections.sort(lyrics);
            buildRelations(lyrics);
            return lyricInfo;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 传递歌词字符串
     *
     * @param lyrics1
     * @return
     */
    public static LyricInfo parseFromStream(String lyrics1) {
        if (lyrics1 == null) {
            return null;
        } else {
            String[] lrcs = lyrics1.split("\\n");
            LyricInfo lyricInfo = new LyricInfo();
            List<LyricInfo.Lyric> lyrics = lyricInfo.getLyrics();
            for (int i = 0; i < lrcs.length; i++) {
                if (lrcs[i].startsWith("﻿[ti:")) {
                    lyricInfo.setTitle(lrcs[i].substring(5, lrcs[i].length() - 1));
                } else if (lrcs[i].startsWith("[ar:")) {
                    lyricInfo.setSinger(lrcs[i].substring(4, lrcs[i].length() - 1));
                } else if (lrcs[i].startsWith("[al:")) {
                    lyricInfo.setAlbum(lrcs[i].substring(4, lrcs[i].length() - 1));
                } else if (lrcs[i].startsWith("[t_time:")) {
                    lyricInfo.setDuration(shortTimeStr2Long(lrcs[i].substring(9, lrcs[i].length() - 1)));
                } else {
                    LyricParser.parseLine(lyrics, lrcs[i]);
                }
            }
            Collections.sort(lyrics);
            buildRelations(lyrics);
            return lyricInfo;
        }

    }


    public static void parseLine(List<Lyric> lyrics, String line) {
        String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(line);
        String lyricStr = null;
        while (matcher.find()) {
            if (lyricStr == null) {
                String[] splitData = pattern.split(line);
                if (splitData == null || splitData.length != 2) {
                    //忽略无效的单行歌词
                    return;
                }
                lyricStr = splitData[splitData.length - 1];
            }

            long time = timeStr2Long(matcher.group(1));

            Lyric lyric = new Lyric();
            lyric.setStartTime(time);
            lyric.setLyric(lyricStr);
            lyrics.add(lyric);
        }
    }

    public static long shortTimeStr2Long(String timeStr) {
        String[] splitMinute = timeStr.split(":");
        int min = Integer.parseInt(splitMinute[0]);
        int sec = Integer.parseInt(splitMinute[0]);
        return min * 60000 + sec * 1000;
    }

    public static long timeStr2Long(String timeStr) {
        String[] splitMinute = timeStr.split(":");
        int min = Integer.parseInt(splitMinute[0]);
        String[] splitSecond = splitMinute[1].split("\\.");
        int sec = Integer.parseInt(splitSecond[0]);
        int millis = Integer.parseInt(splitSecond[1]);
        return min * 60000 + sec * 1000 + millis;
    }

    public static void buildRelations(List<Lyric> lyrics) {
        Lyric preNode = null;
        for (Lyric lyric : lyrics) {
            if (preNode != null) {
                preNode.setEndTime(lyric.getStartTime() - 1);
            }
            preNode = lyric;
        }
    }
}
