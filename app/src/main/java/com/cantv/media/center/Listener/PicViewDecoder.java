package com.cantv.media.center.Listener;

import android.graphics.Bitmap;

import java.io.InputStream;

public interface PicViewDecoder {
    Bitmap decodePic(String picUri, InputStream picStream);
}
