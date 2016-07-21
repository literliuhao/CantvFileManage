package com.cantv.media.center.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.widget.TextView;

import com.cantv.media.R;

/**
 * Created by yibh on 2016/7/21.
 */

public class StringUtil {

	/**
	 * 文字样式,比如斜体,加粗,颜色等
	 * 
	 * @param context
	 * @param content
	 * @param style
	 * @return
	 */
	public static SpannableString setStringStyle(Context context, String content, int style) {
		SpannableString spannableString = new SpannableString(content);
		spannableString.setSpan(new TextAppearanceSpan(context, style), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}

	/**
	 * 合并两个不同样式的内容显示到一个TextView上
	 * 
	 * @param context
	 * @param textView
	 * @param style
	 * @param st1
	 * @param st2
	 */
	public static void getMergeString(Context context, TextView textView, int style, String st1, String st2) {
		SpannableString spannableStr = setStringStyle(context, st1, style);

		SpannableStringBuilder spannableString = new SpannableStringBuilder(spannableStr).append(st2);
		CharSequence charSequence = spannableString.subSequence(0, spannableString.length());
		textView.setText(charSequence);
	}

	/**
	 * 设置指定内容给指定TextView
	 * 
	 * @param content
	 * @param textView
	 */
	public static void setText(String content, TextView textView) {
		if (null != content) {
			textView.setText(content);
		}
	}

}
