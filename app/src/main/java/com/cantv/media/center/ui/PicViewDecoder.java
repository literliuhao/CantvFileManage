package com.cantv.media.center.ui;

import android.graphics.Bitmap;

import java.io.InputStream;

public interface PicViewDecoder {
    Bitmap decodePic(String picUri, InputStream picStream);
}
