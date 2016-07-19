package com.cantv.media.center.utils;

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
	
	
}
