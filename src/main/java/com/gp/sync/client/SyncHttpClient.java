package com.gp.sync.client;

import com.gp.core.AppContextHelper;
import com.gp.sync.message.SyncPushMessage;

public class SyncHttpClient {
	
	private static SyncHttpClient httpClient;
	
	private SyncPushProcess pushProcess;
	
	public static SyncHttpClient getInstance() {
		
		if(null == httpClient) {
			
			httpClient = new SyncHttpClient();
		}
		
		return httpClient;
	}
	
	private SyncHttpClient() {
		
		this.pushProcess = AppContextHelper.getSpringBean(SyncPushProcess.class);
	}
	
	public void setUrl(String url) {
		this.pushProcess.setUrl(url);
	}
	
	public void pushMessage(SyncPushMessage pushMessage) {
		
		this.pushProcess.processPush(pushMessage);
		
	}
}
