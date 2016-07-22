package com.cantv.media.center.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils {
	
	public static NetworkInfo getNetInfo(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}
	
	public static String getWifiName(Context context){
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);   
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getSSID();
	}
	
	public static String getWiFiIp(Context context){
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);   
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();   
		int ipAddress = wifiInfo.getIpAddress();   
		
		String ip = String.format("%d.%d.%d.%d",   
				(ipAddress & 0xff),   
				(ipAddress >> 8 & 0xff),   
				(ipAddress >> 16 & 0xff),   
				(ipAddress >> 24 & 0xff));   
		return ip;
	}
	
	public static String getEthernetIp(Context context){
		try {
			String ip = "";
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() 
                    		&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                    	ip = inetAddress.getHostAddress().toString();
                    	break;
                    }
                }
            }
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
	}
	
	/**
	 * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
	 * @return
	 */
	public static final boolean ping(String host) {

		String result = null;
		try {
			String ip = host;// ping 的地址，可以换成任何一种可靠的外网
			Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
			// 读取ping的内容，可以不加
			InputStream input = p.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			StringBuffer stringBuffer = new StringBuffer();
			String content = "";
			while ((content = in.readLine()) != null) {
				stringBuffer.append(content);
			}
			Log.d("------ping-----", "result content : " + stringBuffer.toString());
			// ping的状态
			int status = p.waitFor();
			if (status == 0) {
				result = "success";
				return true;
			} else {
				result = "failed";
			}
		} catch (IOException e) {
			result = "IOException";
		} catch (InterruptedException e) {
			result = "InterruptedException";
		} finally {
			Log.d("----result---", "result = " + result);
		}
		return false;
	}
}