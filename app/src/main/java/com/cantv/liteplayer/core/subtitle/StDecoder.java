package com.cantv.liteplayer.core.subtitle;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.Map;

public class StDecoder {
	public final String ENCODE_UTF16_BE = "UTF16_BE"; // unicode big endian
	public final String ENCODE_UTF16_LE = "UTF16_LE"; // unicode little endian

	public Map<String, List<StContent>> decodeSubtitle(String subtitlePath, String encode) throws Exception {
		return null;
	}

	public int closeBufferReader(BufferedReader bufferedReader) {
		if (null == bufferedReader) {
			return -1;
		}
		try {
			bufferedReader.close();
			bufferedReader = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static StDecodeResult decodeSubtitle(String subtitlePath) {
		StDecodeResult result = new StDecodeResult();
		result.isPictureSub = false;
		result.isTextSub = true;
		if (subtitlePath.toLowerCase().endsWith(".srt")) {
			try {
				result.subtitleDecoder = new SrtDecoder();
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subtitlePath, null);
				result.isSuccess = true;
			} catch (Exception e) {
				try {
					result.subtitleDecoder = new SrtDecoder();
					result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subtitlePath, "ISO-8859-1");
					result.isSuccess = true;
				} catch (Exception e1) {
					result.isSuccess = false;
					result.subtitleContentMap = null;
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		} else if (subtitlePath.toLowerCase().endsWith(".ass") || subtitlePath.toLowerCase().endsWith(".ssa")) {
			try {
				result.subtitleDecoder = new AssDecoder();
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subtitlePath, null);
				result.isSuccess = true;
			} catch (Exception e) {
				result.isSuccess = false;
				result.subtitleContentMap = null;
				e.printStackTrace();
			}
		} else if (subtitlePath.toLowerCase().endsWith(".smi")) {
			try {
				result.subtitleDecoder = new SmiDecoder();
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subtitlePath, null);
				result.isSuccess = true;
			} catch (Exception e) {
				result.isSuccess = false;
				result.subtitleContentMap = null;
				e.printStackTrace();
			}
		} else if (subtitlePath.toLowerCase().endsWith(".sub")) {
			try {
				String subtitleIdxPath = subtitlePath.substring(0, subtitlePath.lastIndexOf(".")) + ".idx";
				if (new File(subtitleIdxPath).exists()) {
					result.subtitleDecoder = new SubDecoder();
					result.isPictureSub = true;
					result.isTextSub = false;
				} else {
					result.subtitleDecoder = new MicroSubDecoder();
				}
				result.subtitleContentMap = result.subtitleDecoder.decodeSubtitle(subtitlePath, null);
				result.isSuccess = true;
			} catch (Exception e) {
				result.isSuccess = false;
				result.subtitleContentMap = null;
				e.printStackTrace();
			}
		}

		return result;
	}

}
