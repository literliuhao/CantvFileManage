package com.cantv.liteplayer.core.subtitle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.text.TextUtils;
import android.util.Log;

/*".srt", ".smi", ".ass", ".ssa", ".sub"*/
public class StUtil {
	public static int mOldSubtitleIndex = 0;
	public static boolean stopDecodeFlag = false;

	public static List<String> getSubtitlePath(String videoPath) {
		List<String> sbPathList = new ArrayList<String>();
		if (TextUtils.isEmpty(videoPath))
			return sbPathList;
		if (videoPath.contains("file")) {
			videoPath=videoPath.substring(7);
		}

		int end = videoPath.lastIndexOf("/", videoPath.length());
		String path = videoPath.substring(0, end + 1);
		end = videoPath.lastIndexOf(".", videoPath.length());
		if (-1 == end || null == path)
			return sbPathList;

		String subffix = videoPath.substring(0, end);
		File files = new File(path);
		if ((files != null) && (files.exists()) && (files.isDirectory())) {
			File[] filesInDir = files.listFiles();
			long count = filesInDir.length;
			for (int num = 0; num < count; num++) {
				String filePath = filesInDir[num].getPath();
				File subTitleFile = new File(filePath);
				if ((subTitleFile != null) && (subTitleFile.isFile()) && (subTitleFile.canRead())) {
					int pos = filePath.lastIndexOf(".", filePath.length());
					String sub = filePath.substring(pos + 1, filePath.length());
					if ((filePath.startsWith(subffix))
							&& (sub != null)
							&& ((sub.equalsIgnoreCase("srt")) || (sub.equalsIgnoreCase("ass")) || (sub.equalsIgnoreCase("smi"))
									|| (sub.equalsIgnoreCase("ssa")) )) {
						sbPathList.add(filePath);
					}
				}
			}
			if (sbPathList.size() != 0) {
				return sbPathList;
			}
		}
		return sbPathList;
	}

	public static String getEncodeType(String subtitlePath) {
		String encoding = null;
		try {
			BytesEncodingDetector encodeDetector = new BytesEncodingDetector();
			encoding = encodeDetector.detectEncoding(new File(subtitlePath));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return encoding;
	}

	public static void SortAllList(Map<String, List<StContent>> subTitleMap) {
		Set<String> keySet = subTitleMap.keySet();
		Iterator<String> keyIt = keySet.iterator();

		String key = null;
		List<StContent> subTitleList = null;
		while (keyIt.hasNext()) {
			key = keyIt.next();
			subTitleList = subTitleMap.get(key);

			Collections.sort(subTitleList);
		}
	}

	public static BufferedReader getBufferReader(String subtitlePath, String encodeType) {
		if (null == encodeType) {
			encodeType = getEncodeType(subtitlePath);
		}

		InputStreamReader streamReader = null;
		try {
			InputStream inStream = new FileInputStream(subtitlePath);
			BufferedInputStream bufferStream = new BufferedInputStream(inStream);

			if (null != encodeType) {
				encodeType = AliasUtil.getAlias(encodeType);
				streamReader = new InputStreamReader(bufferStream, encodeType);
			} else {
				streamReader = new InputStreamReader(bufferStream);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new BufferedReader(streamReader);
	}

	public static String removeHtmlTag(String input) {
		String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
		str = str.replaceAll("[(/>)<]", "");
		return str;
	}

	public synchronized static StContent get2ndSubtitleContent(int pos, int childIndex, final Map<String, List<StContent>> subtitleList) {
		int keyIndex = 0;
		if(subtitleList == null){
			return null;
		}
		Iterator<String> keyIt = subtitleList.keySet().iterator();
		List<StContent> contentList = null;
		while (keyIt.hasNext()) {
			String keyStr = keyIt.next();
			if (keyIndex == childIndex) {
				contentList = subtitleList.get(keyStr);
				break;
			} else {
				keyIndex++;
			}
		}

		if ((contentList == null) || (contentList.size() == 0)) {
			return null;
		}

		int iTryCount = 0;
		StContent tmpcontent = null;
		StContent content = null;
		while (true) {
			try {
				if (mOldSubtitleIndex + iTryCount >= contentList.size()) {
					break;
				}
				tmpcontent = contentList.get(mOldSubtitleIndex + iTryCount);
			} catch (Exception e) {
				break;
			}
			int iTimeStart = tmpcontent.getSubtitleStartTime();
			int iTimeEnd = tmpcontent.getSubtitleEndTime();
			if (iTimeStart <= pos && iTimeEnd >= pos) {
				content = tmpcontent.copy();
				mOldSubtitleIndex += iTryCount;
				return content;
			} else if (pos < iTimeStart) {
				break;
			}
			iTryCount++;
		}
		iTryCount = 0;
		while (true) {
			try {
				if (mOldSubtitleIndex - iTryCount < 0) {
					break;
				}
				tmpcontent = contentList.get(mOldSubtitleIndex - iTryCount);
			} catch (Exception e) {
				break;
			}
			int iTimeStart = tmpcontent.getSubtitleStartTime();
			int iTimeEnd = tmpcontent.getSubtitleEndTime();
			if (iTimeStart <= pos && iTimeEnd >= pos) {
				content = tmpcontent.copy();
				mOldSubtitleIndex -= iTryCount;
				return content;
			}

			iTryCount++;
		}
		return null;
	}

	public static void decodePictureSubtitle(String subtitlePath, StContent content, StDecodeResult decoderResult, int screenWidth) {
		if (decoderResult.isSuccess && decoderResult.isPictureSub) {
			SubDecoder subdecoder = (SubDecoder) decoderResult.subtitleDecoder;// Idx+Sub
			subdecoder.decodePictureSubTitle(subtitlePath, content, screenWidth);
			StContent subtitleContent = getCurrentSubtitleContent(content, decoderResult.subtitleContentMap);
			subtitleContent.setSubtitleEndTime(content.getSubtitleEndTime());
		}
	}
	private static StContent getCurrentSubtitleContent(StContent currContent, final Map<String, List<StContent>> subtitleList){
		Iterator<String> keyIt = subtitleList.keySet().iterator();
		String keyStr = null;
		List<StContent> contentList = null;
		while(keyIt.hasNext()){
			keyStr = keyIt.next();
			contentList = subtitleList.get(keyStr);
			if(contentList == null || contentList.size() == 0){
				continue;
			}
			StContent tmpcontent = null;
			int subIndex = currContent.getSubtitleIndex()-contentList.get(0).getSubtitleIndex();
			if(subIndex>=0&&subIndex<contentList.size()){
				tmpcontent = contentList.get(subIndex);
				return tmpcontent;
			}
		}
		return null;
	}
}
