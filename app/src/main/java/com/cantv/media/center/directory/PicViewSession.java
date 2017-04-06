package com.cantv.media.center.directory;

import com.app.core.network.HttpClientManager;
import com.app.core.webservices.WebSession;

import org.apache.http.impl.client.DefaultHttpClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class PicViewSession extends WebSession {
    private static final ScheduledExecutorService mSessionExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final DefaultHttpClient mHttpClient = HttpClientManager.get().getDefaultHttpClient();

    // ### 构造函数 ###
    public PicViewSession() {
        super(mSessionExecutor, mHttpClient);
    }
}