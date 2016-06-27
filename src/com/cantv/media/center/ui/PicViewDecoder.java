package com.cantv.media.center.ui;

import java.io.InputStream;

import android.graphics.Bitmap;

public interface PicViewDecoder {
	Bitmap decodePic(String picUri, InputStream picStream);
}
