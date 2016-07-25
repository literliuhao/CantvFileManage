package com.cantv.media.center.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.util.Log;

public class SrtParse {
	private final static String EXPRESSION = "[0-9]+";
	private final static String EXPRESSION1 = "\\d{1,2}:[0-5][0-9]:[0-5][0-9],[0-9][0-9][0-9] --> \\d{1,2}:[0-5][0-9]:[0-5][0-9],[0-9][0-9][0-9]";
	
	/**
	 * @description 解析srt字幕文件
	 * @param filepath
	 * @return
	 * @version 1.0
	 * @author xq
	 * @update 2015年10月22日 下午5:02:44
	 */
	public static ArrayList<SrtBean> parseSrt(String filepath) {
		String charset = getCharset(filepath);// 判断文件编码格式
		ArrayList<SrtBean> srts = new ArrayList<SrtBean>();
		String line = null;
		String startTime, endTime;
		String nowRow = "", oldRow = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath)),
					charset));
			SrtBean srt = null;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					// 匹配为空行
				} else if (Pattern.matches(EXPRESSION, line)) {
					// 匹配为标号
					nowRow = line;
				} else if (Pattern.matches(EXPRESSION1, line)) {
					// 匹配为时间
					String[] lines = line.split(" --> ");
					// 发现字幕会延后大概半秒，所以把字幕往前提了，同理字幕前后调整也是在这调
					int start = lines[0].length() == 12?TimeToMs(lines[0].trim()) - 500:TimeToMs1(lines[0].trim()) - 500;
					int end = lines[1].length() == 12?TimeToMs(lines[1].trim()):TimeToMs1(lines[1].trim());
					//int end = TimeToMs(endTime);
					if (srt != null) {
						srts.add(srt);// 把本条字幕添加到字幕集中
						srt = null;
					}
					srt = new SrtBean();
					srt.setBeginTime(start);
					srt.setEndTime(end);
				} else {
					// 其他为内容
					if (!oldRow.equals(nowRow)) {
						byte[] b = line.getBytes();
						String str = new String(b, "utf-8");
						if (srt != null) {
							// 此while处理特效字幕,特效字幕中会包含如下东西：
							// {\fad(500,500)}{\pos(320,30)}{\fn方正粗倩简体\b1}{\bord0}{\fs20}{\c&H24EFFF&}特效&时轴：{\fs20}
							// {\c&HFFFFFF&} 土皮
							while (str.contains("{") && str.contains("}") && (str.indexOf("{") < str.indexOf("}"))) {
								str = str.replace(str.substring(str.indexOf("{"), str.indexOf("}") + 1), "");
							}
							
							//去掉句号
							while (str.contains("。")) {
								str = str.replace("。", "");
							}
							
							// 去掉黑块
							if (!str.equalsIgnoreCase("■")) {
								srt.setSrt1(str);
							}
						}
					} else {
						byte[] b = line.getBytes();
						String str = new String(b, "utf-8");
						if (srt != null) {
							// 此while处理特效字幕
							while (str.contains("{") && str.contains("}") && (str.indexOf("{") < str.indexOf("}"))) {
								str = str.replace(str.substring(str.indexOf("{"), str.indexOf("}") + 1), "");
							}
							//去掉句号
							while (str.contains("。")) {
								str = str.replace("。", "");
							}
							// 去掉黑块
							if (!str.equalsIgnoreCase("■")) {
								srt.setSrt2(str);
							}
						}
					}
					oldRow = nowRow;
				}
			}
			if (srt != null) {
				srts.add(srt);// 把最后一条字幕添加到字幕集中
				srt = null;
			}
			reader.close();
			return srts;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @description 时间轴转换为毫秒
	 * @param time
	 * @return
	 * @version 1.0
	 * @author xq
	 * @update 2015年10月22日 下午1:25:10
	 */
	private static int TimeToMs(String time) {
		int hour = Integer.parseInt(time.substring(0, 2));
		int mintue = Integer.parseInt(time.substring(3, 5));
		int scend = Integer.parseInt(time.substring(6, 8));
		int milli = Integer.parseInt(time.substring(9, 12));
		int msTime = (hour * 3600 + mintue * 60 + scend) * 1000 + milli;
		return msTime;
	}
	//少一位数的数据转化
	private static int TimeToMs1(String time) {
		int hour = Integer.parseInt(time.substring(0, 1));
		int mintue = Integer.parseInt(time.substring(2, 4));
		int scend = Integer.parseInt(time.substring(5, 7));
		int milli = Integer.parseInt(time.substring(8, 11));
		int msTime = (hour * 3600 + mintue * 60 + scend) * 1000 + milli;
		return msTime;
	}

	/**
	 * @description 判断文件的编码格式
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @version 1.0
	 * @author xq
	 * @update 2015年10月22日 下午6:50:31
	 */
	public static String getCharset(String fileName) {
		String code = "UTF-8";
		try {
			BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
			int p = (bin.read() << 8) + bin.read();
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
		}
		return code;
	}
}
