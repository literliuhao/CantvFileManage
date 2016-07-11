package com.cantv.media.center.ui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.http.impl.client.DefaultHttpClient;
import com.app.core.network.HttpClientManager;
import com.app.core.webservices.WebSession;

public abstract class PicViewSession extends WebSession {
	private static final ScheduledExecutorService mSessionExecutor = Executors
			.newSingleThreadScheduledExecutor();
	private static final DefaultHttpClient mHttpClient = HttpClientManager
			.get().getDefaultHttpClient();

	// ### 构造函数 ###
	public PicViewSession() {
		super(mSessionExecutor, mHttpClient);
	}
}