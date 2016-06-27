/**
 * 
 */
package com.cantv.liteplayer.core.subtitle;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * @author hm
 *
 */
public class SmiTokenizer extends StreamTokenizer {

	static int SMI_TEXT = -1;
	static int SMI_P = -2;
	static int SMI_SYNC = -3;
	static int SMI_BODY = -4;
	static int SMI_NBSP = -5;
	static int SMI_UNKNOWN = -6;
	static int SMI_EOF = -7;
	static int SMI_STYLE = -8;
	static int SMI_CSS = -9;
	static int SMI_TITLE = -10;
	static int SMI_BR = -11;
	
	boolean outsideTag=true;
	
	public SmiTokenizer(Reader r) {
		super(r);
		// TODO Auto-generated constructor stub
		this.resetSyntax();
		this.wordChars(0, 255);
		this.ordinaryChar('<');
		this.ordinaryChar('>');
	}
	
	public int nextHtml(){
		int token;
		
		try{
			switch(token=this.nextToken()){
			case StreamTokenizer.TT_EOF: 
				return SMI_EOF;
			case '<':
				outsideTag=false;
				return nextHtml();
			case '>':
				outsideTag=true;
				return nextHtml();
			case StreamTokenizer.TT_WORD:
				if(sval.trim().length() == 0)
					return nextHtml();
				else if(sval.toUpperCase().indexOf("STYLE") != -1 && !outsideTag)
					return SMI_STYLE;
				else if(sval.toUpperCase().indexOf("SYNC") != -1 && !outsideTag)
					return SMI_SYNC;
				else if(sval.toUpperCase().indexOf("!--") != -1 && !outsideTag)
					return SMI_CSS;
				else if(sval.toUpperCase().indexOf("P") != -1 && !outsideTag)
					return SMI_P;
				else if(sval.toUpperCase().indexOf("/BODY") != -1 && !outsideTag)
					return SMI_BODY;
				else if(sval.toUpperCase().indexOf("TITLE") != -1 && !outsideTag)
					return SMI_TITLE;
				else if(sval.toUpperCase().indexOf("BR") != -1 && !outsideTag)
					return SMI_BR;
				else if(outsideTag){
//					Log.v("+++", ">>>>>>>>>>>>SMI_TEXT:" + sval.toUpperCase());
					return SMI_TEXT;
				}else{
//					Log.v("+++", ">>>>>>>>>>>>unknown:" + sval.toUpperCase());
					return SMI_UNKNOWN;
				}
			default:
				return SMI_UNKNOWN;
			}
		}catch(IOException e){
			return SMI_UNKNOWN;
		}
	}
}
