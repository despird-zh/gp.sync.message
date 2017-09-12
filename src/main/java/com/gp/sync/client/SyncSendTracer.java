package com.gp.sync.client;

import org.springframework.util.StopWatch;

public class SyncSendTracer<T> {

	private StopWatch stopWatch = new StopWatch();
	
	private int tryCount = 0;
	
	private T sendData = null;
	private String url;
	
	public SyncSendTracer(String url, T sendData) {
		this.sendData = sendData;
		this.url = url;
	}
	
	public int tryCount() {
		
		if(0 != tryCount && stopWatch.isRunning()) {
			stopWatch.stop();
		}
		tryCount ++;
		stopWatch.start("push_try__"+ tryCount);
		
		return tryCount;
	}
	
	public void stopTrace() {
		if(stopWatch.isRunning()) {
			stopWatch.stop();
		}
	}
	
	public long getElapsedTime() {
		if(stopWatch.isRunning()) {
			stopWatch.stop();
		}
		return stopWatch.getTotalTimeMillis();
	}
	
	public T getSendData() {
		return sendData;
	}
	
	public String getUrl() {
		return this.url;
	}
}
