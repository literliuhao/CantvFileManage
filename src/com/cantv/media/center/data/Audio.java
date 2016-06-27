package com.cantv.media.center.data;

import java.io.File;
import java.io.IOException;

import com.cantv.media.center.constants.MediaFormat;
import com.cantv.media.center.constants.SourceType;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

@SuppressLint("NewApi")
public class Audio extends Media {

	public Audio(SourceType type, String uri) {
		super(type, uri);
	}

	@Override
	public MediaFormat getMediaFormat() {
		return MediaFormat.AUDIO;
	}

	@Override
	public Bitmap getThumbnails() {
		return getImageThumbnail(800, 800);
	}

	@Override
	protected String parseMediaName() {
		// try {
		// AbstractID3v2Tag tag = new MP3File(new File(getUri())).getID3v2Tag();
		// String songName = tag.frameMap.get("TIT2").toString();
		// int start = songName.indexOf("\"");
		// int end = songName.lastIndexOf("\"");
		// return (start >=0 && end >=0) ?songName.substring(start + 1, end) :
		// songName;
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return super.parseMediaName();
	}

	public Bitmap getImageThumbnail(int width, int height) {
		return null;
	}

}
