package com.cantv.liteplayer.core.subtitle;

import java.util.List;
import java.util.Map;

public class StDecodeResult {

	public boolean isSuccess = true;
	public boolean isPictureSub;
	public boolean isTextSub;
	public Map<String, List<StContent>> subtitleContentMap;
	public StDecoder subtitleDecoder;

	public void setResult(StDecodeResult result) {
		isSuccess = result.isSuccess;
		isPictureSub = result.isPictureSub;
		isTextSub = result.isTextSub;
		subtitleContentMap = result.subtitleContentMap;
		subtitleDecoder = result.subtitleDecoder;
	}

}
