package com.cantv.liteplayer.core.subtitle;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MicroSubDecoder extends StDecoder {

	@Override
	public Map<String, List<StContent>> decodeSubtitle(String subtitlePath, String encode) throws Exception {

		String encodeType = StUtil.getEncodeType(subtitlePath);
		BufferedReader bufferReaderSub = StUtil.getBufferReader(subtitlePath, encodeType);

		Map<String, List<StContent>> subTitleMap = new TreeMap<String, List<StContent>>();
		StContent content = null;
		String subTitleLine = null;
		String langStr = null;
		int time = 0;
		int endTime = 0;
		int indexCount = 0;
		while ((subTitleLine = bufferReaderSub.readLine()) != null) {
			subTitleLine = subTitleLine.trim();

			String str[] = subTitleLine.split("\\}");
			content = new StContent();
			content.setSubtitleIndex(indexCount++);
			if (str.length == 3) {
				String str0 = str[0].substring(str[0].indexOf("{") + 1);
				String str1 = str[1].substring(str[1].indexOf("{") + 1);
				String str2 = str[2];
				if (str2.contains("|")) {
					String lines[] = str2.split("\\|");
					StringBuilder sb = new StringBuilder();
					for (int k = 0; k < lines.length; k++) {
						sb.append(lines[k]);
						if (k < lines.length - 1) {
							sb.append("<bufferReaderSub>");
						}
					}
					str2 = sb.toString();
				}
				langStr = "SUB" + 0;
				if (!subTitleMap.containsKey(langStr)) {
					subTitleMap.put(langStr, new ArrayList<StContent>());
				}
				time = (int) (Integer.valueOf(str0));
				endTime = (int) (Integer.valueOf(str1));
				content.setSubtitleStartTime(time);
				content.setSubtitleEndTime(endTime);
				content.setSubtitleLine(str2);
				subTitleMap.get(langStr).add(content);
			}
		}

		super.closeBufferReader(bufferReaderSub);
		return subTitleMap;
	}

}
