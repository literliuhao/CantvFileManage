/**
 * 
 */
package com.cantv.liteplayer.core.subtitle;

import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * @author hm
 * 
 */
public class AssTokenizer extends StreamTokenizer {

	static int ASS_TEXT = -1;
	static int ASS_BRAKET = -2;
	static int ASS_EOF = -3;
	static int ASS_UNKNOWN = -5;

	boolean outsideTag = true;

	public AssTokenizer(Reader r) {
		super(r);
		// TODO Auto-generated constructor stub
		this.resetSyntax();
		this.wordChars(0, 255);
		this.ordinaryChar('{');
		this.ordinaryChar('}');
	}

	public AssTokenizer(InputStream is) {
		super(is);
		// TODO Auto-generated constructor stub
		this.resetSyntax();
		this.wordChars(0, 255);
		this.ordinaryChar('{');
		this.ordinaryChar('}');
	}

	public int nextAssText() {
		int token;

		try {
			switch (token = this.nextToken()) {
			case StreamTokenizer.TT_EOF:
				return ASS_EOF;
			case '{':
				outsideTag = false;
				return nextAssText();
			case '}':
				outsideTag = true;
				return nextAssText();
			case StreamTokenizer.TT_WORD:
				if (sval.trim().length() == 0)
					return nextAssText();
				else if (outsideTag)
					return ASS_TEXT;
			default:
				return ASS_UNKNOWN;
			}
		} catch (Exception e) {
			return ASS_UNKNOWN;
		}
	}
}
