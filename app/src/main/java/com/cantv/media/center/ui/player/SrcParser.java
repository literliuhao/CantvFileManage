package com.cantv.media.center.ui.player;

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
import android.text.TextUtils;

public class SrcParser {

	private List<SrtBeans> srtList = new ArrayList<SrtBeans>();
	
	public List<SrtBeans> getSrtList() {
		return srtList;
	}

	public void parseFromPath(String path) {
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
				if (!line.trim().equals("")) {
					if (srt == null) {
						srt = new SrtBeans();
						addLineCount = 0;
					}
					srt.src.add(line);
					addLineCount++;
					
				} else {
					if (addLineCount < 2 && srt != null && srtList.size() > 0) {
						srtList.get(srtList.size() - 1).src.addAll(srt.src);
					} else if (srt != null) {
						srtList.add(srt);
					}
					srt = null;
				}
			}
			if (srt != null) {
				srtList.add(srt);// 把最后一条字幕添加到字幕集中
				srt = null;
			}
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
		String srt = null;
		if (timeInMillis <= 1 * 60 * 1000) {
			// 顺序查找
			for (int i = 0, size = srtList.size(); i < size; i++) {
				SrtBeans sb = srtList.get(i);
				if (sb.containsTime(timeInMillis)) {
					srt = sb.getSrt();
					break;
				}else if(sb.lessTime(timeInMillis)){
					break;
				}
			}
		} else {
			// 二分查找
			int index = binarySearch(srtList, timeInMillis);
			if (index >= 0) {
				return srtList.get(index).getSrt();
			}
		}
		return srt == null ? "" : srt;
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
}
