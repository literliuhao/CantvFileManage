package com.cantv.liteplayer.core.interfaces;

import android.content.Intent;

/**
 * Created by liuhao on 2016/12/27.
 */

public interface IMediaListener {
    void onMounted(Intent intent);
    void onUnmounted(Intent intent);
}
