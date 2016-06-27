package com.cantv.media.center.ui;

import java.io.File;
import java.io.InputStream;

import com.cantv.media.R;
import com.cantv.media.center.constants.SourceType;
import com.cantv.media.center.data.Media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

public class MediaPicView extends PicView implements PicViewDecoder {
	private static final String TAG = "MediaPicView"; 
	private Media mMedia;

	public MediaPicView(Context context) {
		this(context, null);
	}

	public MediaPicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPicDecoder(this);

	}

	public void setBackground(Media media) {
		mMedia = media;
		if (media.getSourceType() == SourceType.MOIVE) {
			if (media.isCollection()) {
				setDefaultPic(R.drawable.folder_movies);
			} else {
				setDefaultPic(R.drawable.folder_movie);
			}
		} else if (media.getSourceType() == SourceType.MUSIC) {
			if (media.isCollection()) {
				setDefaultPic(R.drawable.folder_wj);
			} else {
				setDefaultPic(R.drawable.folder_music);
			}
		}
	}

	public void setMedia(Media media) {
		mMedia = media;
		if (!media.isCollection()) {
			setPicUri(Uri.fromFile(new File(media.getUri())).toString());
		}
	}
	@SuppressLint("ResourceAsColor")
	@Override
	public Bitmap decodePic(String picUri, InputStream picStream) {
		if (TextUtils.isEmpty(picUri)) {
			return null;
		}
		if (mMedia != null && mMedia.getUri().equals(Uri.parse(picUri).getPath())) {
			return mMedia.getThumbnails();
		}
		return BitmapFactory.decodeFile(Uri.parse(picUri).getPath());
	}

}
