package com.cantv.media.center.utils;

import com.cantv.media.center.data.LyricInfo;
import com.cantv.media.center.data.LyricInfo.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        try {
            is = new FileInputStream(file);
            return parseFromStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static LyricInfo parseFromStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()));
        String line = null;
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

    private static void parseLine(List<Lyric> lyrics, String line) {
        String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(line);
        String lyricStr = null;
        while (matcher.find()) {
            if (lyricStr == null) {
                String[] splitData = pattern.split(line);
                lyricStr = splitData[splitData.length - 1];
            }

            long time = timeStr2Long(matcher.group(1));

            Lyric lyric = new Lyric();
            lyric.setStartTime(time);
            lyric.setLyric(lyricStr);
            lyrics.add(lyric);
        }
    }

    private static long shortTimeStr2Long(String timeStr) {
        String[] splitMinute = timeStr.split(":");
        int min = Integer.parseInt(splitMinute[0]);
        int sec = Integer.parseInt(splitMinute[0]);
        return min * 60000 + sec * 1000;
    }

    private static long timeStr2Long(String timeStr) {
        String[] splitMinute = timeStr.split(":");
        int min = Integer.parseInt(splitMinute[0]);
        String[] splitSecond = splitMinute[1].split("\\.");
        int sec = Integer.parseInt(splitSecond[0]);
        int millis = Integer.parseInt(splitSecond[1]);
        return min * 60000 + sec * 1000 + millis;
    }

    private static void buildRelations(List<Lyric> lyrics) {
        Lyric preNode = null;
        for (Lyric lyric : lyrics) {
            if (preNode != null) {
                preNode.setEndTime(lyric.getStartTime() - 1);
            }
            preNode = lyric;
        }
    }
}
