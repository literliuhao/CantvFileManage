/**
 * 
 */
package com.cantv.liteplayer.core.subtitle;

import java.util.Map;
import java.util.TreeMap;

public class AliasUtil {

	public static Map<String, String> aliasMap = new TreeMap<String, String>(); 
	static{
		aliasMap.put("windows-1252", "Unicode");
		aliasMap.put("GB18030", "GBK");
	}
	
	public static String getAlias(String orgName){
		if(!aliasMap.containsKey(orgName))
			return orgName;
		
		return aliasMap.get(orgName);
	}
}
