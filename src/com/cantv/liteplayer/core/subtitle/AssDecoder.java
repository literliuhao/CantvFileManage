package com.cantv.liteplayer.core.subtitle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.graphics.Color;

public class AssDecoder extends StDecoder {
	private static Map<Integer, String> assLanMap = new TreeMap<Integer, String>();
	static {
		assLanMap.put(0, "ANSI");
		assLanMap.put(1, "DEFAULT");
		assLanMap.put(2, "SYMBOL");
		assLanMap.put(128, "SHIFTJIS");
		assLanMap.put(129, "HANGEUL");
		assLanMap.put(134, "GB2312");
		assLanMap.put(136, "CHINESEBIG5");
		assLanMap.put(255, "OEM");
		assLanMap.put(130, "JOHAB");
		assLanMap.put(177, "HEBREW");
		assLanMap.put(178, "ARABIC");
		assLanMap.put(161, "GREEK");
		assLanMap.put(162, "TURKISH");
		assLanMap.put(163, "VIETNAMESE");
		assLanMap.put(222, "THAI");
		assLanMap.put(238, "EASTEUROPE");
		assLanMap.put(204, "RUSSIAN");
		assLanMap.put(77, "MAC");
		assLanMap.put(186, "BALTIC");
	}

	private static int getAssTime(String str) {
		int targetTime = 0;
		int hour, minute, second, micosecond;
		String strHour, strMinute, strSecond, strMicosecond;

		String[] strArray = str.split("\\.");
		String[] dotArray = strArray[0].split(":");
		strHour = dotArray[0];
		strMinute = dotArray[1];
		strSecond = dotArray[2];
		strMicosecond = strArray[1];

		hour = Integer.parseInt(strHour);
		minute = Integer.parseInt(strMinute);
		second = Integer.parseInt(strSecond);
		micosecond = Integer.parseInt(strMicosecond);

		targetTime = hour * 3600000 + minute * 60000 + second * 1000 + micosecond;

		return targetTime;
	}

	@Override
	public Map<String, List<StContent>> decodeSubtitle(String subtitlePath, String encodeType) throws Exception {
		encodeType = StUtil.getEncodeType(subtitlePath);
		BufferedReader buffReaderAss = StUtil.getBufferReader(subtitlePath, encodeType);
		String subTitleLine = null;
		String langStr = null;
		String styleName = null;
		String markedStr = null;
		String lanClass = null;
		String lanClassStr = null;
		Integer langVal = 0;
		int splitCount = 0;
		int fontSizeIndex = -1;
		int fontColorIndex = -1;

		Map<String, SubtitleStyle> subTitleStypeMap = new TreeMap<String, SubtitleStyle>();
		Map<String, List<StContent>> subTitleMap = new TreeMap<String, List<StContent>>();
		Map<String, Integer> subTitleStyleLanMap = new TreeMap<String, Integer>();

		Map<Integer, StContent> subTimeMap = new TreeMap<Integer, StContent>();
		int lineCount = 0;
		StringBuffer subTitleSb = null;

		while ((subTitleLine = buffReaderAss.readLine()) != null && StUtil.stopDecodeFlag == false) {
			subTitleLine = subTitleLine.trim();
			if (subTitleLine.toUpperCase().startsWith("STYLE:")) {
				langStr = subTitleLine.substring(subTitleLine.lastIndexOf(",") + 1, subTitleLine.length());
				langVal = Integer.valueOf(langStr);
				styleName = subTitleLine.split(":")[1].trim().split(",")[0].trim();

				subTitleStyleLanMap.put(styleName, langVal);
				if (fontSizeIndex != -1 && fontColorIndex != -1) {
					String[] styleArray = subTitleLine.substring("STYLE:".length()).split(",");
					int fontsize = 0;
					int fontcolor = 0;
					String colorString = null;
					try {
						fontsize = Integer.valueOf(styleArray[fontSizeIndex]);
						colorString = styleArray[fontColorIndex].toUpperCase().replace("&H", "#");
						fontcolor = Color.parseColor(colorString);
						subTitleStypeMap.put(styleName, new SubtitleStyle(fontsize, fontcolor));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (subTitleLine.toUpperCase().startsWith("FORMAT:")) {
				String[] styleArray = subTitleLine.substring("FORMAT:".length()).split(",");
				splitCount = styleArray.length;
				for (int k = 0; k < splitCount; k++) {
					if ("FONTSIZE".equals(styleArray[k].trim().toUpperCase())) {
						fontSizeIndex = k;
					} else if ("PRIMARYCOLOUR".equals(styleArray[k].trim().toUpperCase())) {
						fontColorIndex = k;
					}
				}
			}

			if (subTitleLine.toUpperCase().startsWith("DIALOGUE:")) {
				markedStr = subTitleLine.substring(subTitleLine.indexOf(":")).replaceAll("Marked=", "").replaceAll("\\\\(n|N)", "<br>").trim();
				AssTokenizer assTokenizer = new AssTokenizer(new InputStreamReader(new ByteArrayInputStream(markedStr.getBytes(encodeType)), encodeType));
				int tagType = AssTokenizer.ASS_UNKNOWN;

				StringBuffer markedSb = new StringBuffer();
				while ((tagType = assTokenizer.nextAssText()) != AssTokenizer.ASS_EOF) {
					if (tagType == AssTokenizer.ASS_TEXT) {
						markedSb.append(assTokenizer.sval);
					}
				}

				markedStr = markedSb.toString();

				StContent content = new StContent();
				content.setSubtitleIndex(lineCount++);
				String[] markedArray = markedStr.split(",");
				if (markedArray != null && markedArray.length != 0) {
					lanClassStr = markedArray[3].trim();
					if (lanClassStr.startsWith("*"))
						lanClassStr = lanClassStr.replace("*", "");
					lanClass = String.valueOf(subTitleStyleLanMap.get(lanClassStr));

					subTitleSb = new StringBuffer();
					for (int k = splitCount - 1; k < markedArray.length; k++)
						subTitleSb.append(markedArray[k]);

					if (subTitleStypeMap != null && subTitleStypeMap.containsKey(lanClassStr)) {
						content.setSubtitleLine("<font size=\"" + subTitleStypeMap.get(lanClassStr).getFontSize() + "\" color=\""
								+ subTitleStypeMap.get(lanClassStr).getFontColor() + "\">" + subTitleSb.toString() + "</font>");
					} else {
						content.setSubtitleLine(subTitleSb.toString());
					}

					content.setSubtitleStartTime(getAssTime(markedArray[1].trim()));
					content.setSubtitleEndTime(getAssTime(markedArray[2].trim()));

					content.setmLanguageClass(lanClassStr);
					if (subTitleMap.get(lanClassStr) == null) {
						subTitleMap.put(lanClassStr, new ArrayList<StContent>());
					}

					if (!subTimeMap.containsKey(content.getSubtitleStartTime())) {
						subTimeMap.put(content.getSubtitleStartTime(), content);
					} else {
						if (content.getmLanguageClass().equals(subTimeMap.get(content.getSubtitleStartTime()).getmLanguageClass())) {
							content = subTimeMap.get(content.getSubtitleStartTime());

							if (subTitleStypeMap != null && subTitleStypeMap.containsKey(lanClassStr)) {
								content.setSubtitleLine("<font size=\"" + subTitleStypeMap.get(lanClassStr).getFontSize() + "\" color=\""
										+ subTitleStypeMap.get(lanClassStr).getFontColor() + "\">" + content.getSubtitleLine() + "<br>" + subTitleSb.toString()
										+ "</font>");
							} else {
								content.setSubtitleLine(content.getSubtitleLine() + "<br>" + subTitleSb.toString());
							}
						}
					}

					if (!subTitleMap.get(lanClassStr).contains(content))
						subTitleMap.get(lanClassStr).add(content);
				}
			}
		}
		super.closeBufferReader(buffReaderAss);
		StUtil.SortAllList(subTitleMap);
		return subTitleMap;
	}
}
