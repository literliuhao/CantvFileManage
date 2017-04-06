package com.cantv.media.center.directory.Listener;

import android.graphics.Bitmap;

import java.io.InputStream;

public interface PicViewDecoder {
    Bitmap decodePic(String picUri, InputStream picStream);
}
