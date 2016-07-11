package com.cantv.liteplayer.core.subtitle;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class SrtDecoder extends StDecoder {

	private int getSrtTime(String str) {
		int targetTime = 0;
		int hour, minute, second, micosecond;
		String strHour, strMinute, strSecond, strMicosecond;

		strHour = str.substring(0, 2);
		strMinute = str.substring(3, 5);
		strSecond = str.substring(6, 8);
		strMicosecond = str.substring(9, 12);

		hour = Integer.parseInt(strHour);
		minute = Integer.parseInt(strMinute);
		second = Integer.parseInt(strSecond);
		micosecond = Integer.parseInt(strMicosecond);

		targetTime = hour * 3600000 + minute * 60000 + second * 1000 + micosecond;

		return targetTime;
	}

	private boolean isNumberic(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	@Override
	public Map<String, List<StContent>> decodeSubtitle(String subtitlePath, String encode) throws Exception {
		String encodeType = StUtil.getEncodeType(subtitlePath);
		BufferedReader buffReaderSrt = StUtil.getBufferReader(subtitlePath, encodeType);

		String subtitleLine = null;
		String regex = "\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d --> \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d";
		StContent content = null;
		Map<String, List<StContent>> subTitleMap = new TreeMap<String, List<StContent>>();

		List<StContent> subTitleList = new ArrayList<StContent>();
		StringBuffer subTitleBuf = new StringBuffer("");
		String preLine = null;
		String subTitleStr = null;
		int languageIndex = 1;
		int preLineNum = 0;
		String subtitle = null;
		int starttime = 0;
		int endtime = 0;
		int prestarttime = 0;
		int lineCount = 0;

		Map<Integer, StContent> subTimeMap = new TreeMap<Integer, StContent>();

		while ((subtitleLine = buffReaderSrt.readLine()) != null && StUtil.stopDecodeFlag == false) {
			subtitleLine = subtitleLine.trim();
			if (lineCount == 0 && "1".equals(subtitleLine)) {
				preLine = subtitleLine;
				preLineNum = 1;
				continue;
			}

			if ((null != encodeType) && encodeType.equalsIgnoreCase("Big5")) {
				subtitleLine = new String(subtitleLine.getBytes("Big5"), "GB2312");
			}

			if (Pattern.matches(regex, subtitleLine)) {
				content = new StContent();
				content.setSubtitleIndex(lineCount++);
				starttime = getSrtTime(subtitleLine.split("-->")[0].trim());
				content.setSubtitleStartTime(starttime);
				endtime = getSrtTime(subtitleLine.split("-->")[1].trim());
				content.setSubtitleEndTime(endtime);

				if (starttime == 0 && endtime == 0)
					continue;

				if (prestarttime > starttime) {
					subTitleMap.put(String.valueOf(languageIndex), subTitleList);
					languageIndex++;
					subTitleList = new ArrayList<StContent>();
				}

				content.setmLanguageClass(String.valueOf(languageIndex));

				if (!subTimeMap.containsKey(starttime)) {
					subTimeMap.put(starttime, content);
				} else if (content.getmLanguageClass().equals(subTimeMap.get(starttime).getmLanguageClass())) {
					// multie subline with the same time and the same language
					// class
					content = subTimeMap.get(starttime);
				}

				prestarttime = starttime;
			} else if (isNumberic(subtitleLine) && "".equals(preLine)) {
				subTitleStr = subTitleBuf.toString().trim();
				if ("".equals(subTitleStr)) {
					continue;
				}
				subtitle = subTitleStr.substring(0, subTitleStr.length() - "<br>".length()).trim();
				if(content != null){
					if (content.getSubtitleLine() != null && content.getSubtitleLine().trim().length() > 0)
						content.setSubtitleLine(content.getSubtitleLine() + subtitle);
					else
						content.setSubtitleLine(subtitle);
					if (!subTitleList.contains(content))
						subTitleList.add(content);
				}
				content = null;
				subTitleBuf.delete(0, subTitleBuf.length());

				if (Integer.parseInt(subtitleLine) < preLineNum) {
					subTitleMap.put(String.valueOf(languageIndex), subTitleList);
					languageIndex++;
					subTitleList = new ArrayList<StContent>();
				}

				preLineNum = Integer.parseInt(subtitleLine);
			} else {
				if (!"".equals(subtitleLine.trim()))
					subTitleBuf.append(subtitleLine + "<br>");
			}

			preLine = subtitleLine;
		}

		subTitleStr = subTitleBuf.toString().trim();
		if (!"".equals(subTitleStr)) {
			subtitle = subTitleStr.substring(0, subTitleStr.length() - "<br>".length()).trim();
			if (encode == null) {
				subTitleList.add(content);
				content = null;
			} else {
				try {
					content.setSubtitleLine(subtitle);
					subTitleList.add(content);
					content = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		subTitleMap.put(String.valueOf(languageIndex), subTitleList);

		try {
			buffReaderSrt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		StUtil.SortAllList(subTitleMap);
		return subTitleMap;
	}

}
