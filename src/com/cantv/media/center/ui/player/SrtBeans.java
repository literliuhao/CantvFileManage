package com.cantv.media.center.ui.player;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SrtBeans implements Comparable<Long> {

    private int lineIndex;
    public List<String> src = new ArrayList<String>();// 字幕元数据
    public static Pattern matchTimePattern = Pattern.compile("(\\d{1,2}:\\d{1,2}:\\d{1,2},\\d{1,3}).*(\\d{1,2}:\\d{1,2}:\\d{1,2},\\d{1,3})");
    public static Pattern convertTimePattern = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2}),(\\d{1,3})");

    private boolean timeInitialized;
    private int beginTime;// 字幕开始时间
    private int endTime;// 字幕结束时间

    boolean srtInitialized;
    public String srt;// 字幕

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int index) {
        this.lineIndex = index;
    }

    public List<String> getSrc() {
        return src;
    }

    public void setSrc(List<String> src) {
        this.src = src;
    }

    public boolean isTimeInitialized() {
        return timeInitialized;
    }

    public void setTimeInitialized(boolean timeInitialized) {
        this.timeInitialized = timeInitialized;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public boolean isSrtInitialized() {
        return srtInitialized;
    }

    public void setSrtInitialized(boolean srtInitialized) {
        this.srtInitialized = srtInitialized;
    }

    public void setSrt(String srt) {
        this.srt = srt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : src) {
            sb.append(s).append("|");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Long another) {
        if (this.containsTime(another)) {
            return 0;
        }
        if (another < this.beginTime) {
            return 1;
        }
        return -1;
    }

    /**
     * 判断当前字幕是否对应某个时间点
     *
     * @param timeInMillis
     * @return
     */
    public boolean containsTime(long timeInMillis) {
        if (!timeInitialized) {
            synchronized (SrtBeans.class) {
                if (!timeInitialized) {
                    ListIterator<String> listIterator = src.listIterator();
                    while (listIterator.hasNext()) {
                        String s = listIterator.next();
                        Matcher matcher = matchTimePattern.matcher(s);
                        if (matcher.find()) {
                            int groupCount = matcher.groupCount();
                            if (groupCount != 2) {
                                // 非法的时间 或 其他类型
                                continue;
                            }
                            beginTime = TimeToMs(matcher.group(1));
                            endTime = TimeToMs(matcher.group(2));
                            listIterator.remove();
                            break;
                        }
                    }
                    timeInitialized = true;
                }
            }
        }
        if (timeInMillis >= beginTime && timeInMillis <= endTime) {
            return true;
        }
        return false;
    }

    public String getSrt() {
        if (!srtInitialized) {
            StringBuilder sb = new StringBuilder();
            ListIterator<String> listIterator = src.listIterator();
            while (listIterator.hasNext()) {
                String line = listIterator.next();
                if (Pattern.matches("\\d{1,}", line)) {
                    // line NO.
                    lineIndex = Integer.parseInt(line);
                } else {
                    // srt
                    sb.append(line).append("\n");
                }
            }
            srt = sb.toString();
            // srt = srt.replace("。", "");
            srtInitialized = true;
        }
        return srt;
    }

    private static int TimeToMs(String time) {
        Matcher matcher = convertTimePattern.matcher(time);
        if (matcher.find() && matcher.groupCount() == 4) {
            int hour = Integer.parseInt(matcher.group(1));
            int mintue = Integer.parseInt(matcher.group(2));
            int scend = Integer.parseInt(matcher.group(3));
            int milli = Integer.parseInt(matcher.group(4));
            return (hour * 3600 + mintue * 60 + scend) * 1000 + milli;
        }
        return 0;
    }
}
