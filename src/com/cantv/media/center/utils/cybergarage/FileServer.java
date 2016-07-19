package com.cantv.media.center.utils.cybergarage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;

public class FileServer extends Thread implements org.cybergarage.http.HTTPRequestListener {

	public static final String CONTENT_EXPORT_URI = "/smb";
	private HTTPServerList httpServerList = new HTTPServerList();
	private int HTTPPort = 2222;
	private String bindIP = null;
	public static int mPort;

	public String getBindIP() {
		return bindIP;
	}

	public HTTPServerList getHttpServerList() {
		return httpServerList;
	}

	public int getHTTPPort() {
		return HTTPPort;
	}

	@Override
	public void run() {
		super.run();

		/**************************************************
		 * ����http�����������չ�������
		 *************************************************/
		int retryCnt = 0;
		int bindPort = getHTTPPort();
		HTTPServerList httpServerList = getHttpServerList();
		while (httpServerList.open(bindPort) == false) {
			retryCnt++;
			if (100 < retryCnt) {
				return;
			}
			bindPort++;
		}
		httpServerList.addRequestListener(this);
		httpServerList.start();
		bindIP = httpServerList.getHTTPServer(0).getBindAddress();
	}

	@Override
	public void httpRequestRecieved(HTTPRequest httpReq) {

		String uri = httpReq.getURI();

		if (uri.startsWith(CONTENT_EXPORT_URI) == false) {
			httpReq.returnBadRequest();
			return;
		}

		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String filePaths = "smb://" + uri.substring(5);
		int indexOf = filePaths.indexOf("&");
		if (indexOf != -1) {
			filePaths = filePaths.substring(0, indexOf);
		}

		try {
			SmbFile file = new SmbFile(filePaths);
			long contentLen = file.length();
			String contentType = getFileType(filePaths);
			InputStream contentIn = file.getInputStream();

			if (contentLen <= 0 || contentType.length() <= 0 || contentIn == null) {
				httpReq.returnBadRequest();
				return;
			}

			HTTPResponse httpRes = new HTTPResponse();
			httpRes.setContentType(contentType);
			httpRes.setStatusCode(HTTPStatus.OK);
			httpRes.setContentLength(contentLen);
			httpRes.setContentInputStream(contentIn);

			httpReq.post(httpRes);

			contentIn.close();
		} catch (MalformedURLException e) {
			httpReq.returnBadRequest();
			return;
		} catch (SmbException e) {
			httpReq.returnBadRequest();
			return;
		} catch (IOException e) {
			httpReq.returnBadRequest();
			return;
		}
	}

	public static String getFileType(String uri) {
		if (uri == null) {
			return "*/*";
		}

		if (uri.endsWith(".mp3")) {
			return "audio/mpeg";
		}

		if (uri.endsWith(".mp4")) {
			return "video/mp4";
		}

		return "*/*";
	}

	public void release(){
		httpServerList.stop(); 
		httpServerList.close(); 
		httpServerList.clear(); 
		interrupt(); 
	}
}
