package com.cantv.liteplayer.core.subtitle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SmiDecoder extends StDecoder {

	private List<String> getLangStr(String str) throws IOException {
		Reader reader = new StringReader(str);
		BufferedReader r = new BufferedReader(reader);
		String line;
		List<String> langList = new ArrayList<String>();

		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.startsWith(".")) {
				if (line.indexOf("{") == -1) {
					langList.add(line.substring(1).trim());
				} else {
					langList.add(line.substring(1, line.indexOf("{")).trim());
				}
			}
		}

		return langList;
	}

	private String getClassName(String str) {
		StringTokenizer st = new StringTokenizer(str, " ");
		String tmp = null;
		while (st.hasMoreTokens()) {
			tmp = st.nextToken();

			if (tmp.toUpperCase().trim().startsWith("CLASS")) {
				return tmp.trim().split("=")[1].split(" ")[0];
			}
		}

		return null;
	}

	private void getTimeStr(StContent content, String str) {
		StringTokenizer st = new StringTokenizer(str, " ");
		String tmp = null;
		while (st.hasMoreTokens()) {
			tmp = st.nextToken();
			if (tmp.toUpperCase().trim().startsWith("START")) {
				content.setSubtitleStartTime(Integer.valueOf(tmp.trim().split("=")[1].split(" ")[0]));
			} else if (tmp.toUpperCase().trim().startsWith("END")) {
				content.setSubtitleEndTime(Integer.valueOf(tmp.trim().split("=")[1].split(" ")[0]));
			}
		}
	}

	private int getStartTime(String str) {
		StringTokenizer st = new StringTokenizer(str, " ");
		String tmp = null;
		while (st.hasMoreTokens()) {
			tmp = st.nextToken();
			if (tmp.toUpperCase().trim().startsWith("START")) {
				return Integer.valueOf(tmp.trim().split("=")[1].split(" ")[0]);
			}
		}

		return 0;
	}
	@Override
	public Map<String, List<StContent>> decodeSubtitle(String subtitlePath, String encode) throws Exception {
		BufferedReader br = StUtil.getBufferReader(subtitlePath, null);
		Map<String, List<StContent>> subTitleMap = new TreeMap<String, List<StContent>>();
		List<String> langList = new ArrayList<String>();
		StContent content = null;
		int indexCount = 0;
		int preTagType = SmiTokenizer.SMI_UNKNOWN;

		SmiTokenizer smiTokenizer = new SmiTokenizer(br);
		int tagType = SmiTokenizer.SMI_UNKNOWN;
		List<StContent> subTitleList = null;
		boolean hasMetNbsp = false;
		String subTitleLine = null;

		while ((tagType = smiTokenizer.nextHtml()) != SmiTokenizer.SMI_EOF && StUtil.stopDecodeFlag == false) {
			if (tagType == SmiTokenizer.SMI_SYNC) {
				if (content != null && content.getmLanguageClass() != null && subTitleMap.get(content.getmLanguageClass()) != null) {
					if (hasMetNbsp == false && content.getSubtitleEndTime() == 0) {
						content.setSubtitleEndTime(getStartTime(smiTokenizer.sval));
					}
					if (!subTitleMap.get(content.getmLanguageClass()).contains(content))
						subTitleMap.get(content.getmLanguageClass()).add(content);
				}

				content = new StContent();
				hasMetNbsp = false;
				getTimeStr(content, smiTokenizer.sval);
				content.setSubtitleIndex(indexCount++);
			} else if (tagType == SmiTokenizer.SMI_P) {
				if (content != null) {
					content.setmLanguageClass(getClassName(smiTokenizer.sval));
				}
			} else if (tagType == SmiTokenizer.SMI_CSS) {
				langList = getLangStr(smiTokenizer.sval);
				for (int i = 0; i < langList.size(); i++) {
					subTitleMap.put(langList.get(i), new ArrayList<StContent>());
				}
			} else if (tagType == SmiTokenizer.SMI_TEXT && content != null) {
				if (subTitleLine == null)
					subTitleLine = StUtil.removeHtmlTag(smiTokenizer.sval.toLowerCase().replaceAll("[\\r]?[\\n]", "\n").trim());
				else
					subTitleLine = subTitleLine + StUtil.removeHtmlTag(smiTokenizer.sval.toLowerCase().replaceAll("[\\r]?[\\n]", "\n").trim());
				if ("".equals(subTitleLine.trim())) {
					hasMetNbsp = true;
					if (content.getmLanguageClass() != null) {
						subTitleList = subTitleMap.get(content.getmLanguageClass());
					}

					if (subTitleList != null && subTitleList.size() != 0) {
						subTitleList.get(subTitleList.size() - 1).setSubtitleEndTime(content.getSubtitleStartTime());
					}

					content = null;
				} else {
					if (preTagType == SmiTokenizer.SMI_BR) {
						content.setSubtitleLine(content.getSubtitleLine() + subTitleLine);
					} else {
						content.setSubtitleLine(subTitleLine);
					}
					subTitleLine = null;
				}
			} else if (tagType == SmiTokenizer.SMI_BR && content != null) {
				content.setSubtitleLine(content.getSubtitleLine() + "<br>");
			} else if (tagType == SmiTokenizer.SMI_BODY && content != null) {
				if (content != null && content.getmLanguageClass() != null && subTitleMap.get(content.getmLanguageClass()) != null) {
					if (hasMetNbsp == false && content.getSubtitleEndTime() == 0) {
						content.setSubtitleEndTime(Integer.MAX_VALUE);
					}
					if (!subTitleMap.get(content.getmLanguageClass()).contains(content))
						subTitleMap.get(content.getmLanguageClass()).add(content);
				}
			}

			preTagType = tagType;
		}
		try {
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		StUtil.SortAllList(subTitleMap);
		return subTitleMap;
	}

}
