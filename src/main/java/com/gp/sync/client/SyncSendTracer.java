package com.gp.sync.client;

import org.springframework.util.StopWatch;

/**
 * Trace the message send profiling information, eg. try times and running time.
 * the information will be used to judge the overtime etc.
 * 
 * @author gdiao
 * @version 0.1 2016-10-12
 * 
 **/
public class SyncSendTracer<T> {

	private StopWatch stopWatch = new StopWatch();
	
	private int tryCount = 0;
	
	private T sendData = null;
	private String url;
	
	/**
	 * Constructor with pushing parameters
	 * @param url the url to receive data
	 * @param T the data to be sent 
	 **/
	public SyncSendTracer(String url, T sendData) {
		this.sendData = sendData;
		this.url = url;
	}
	
	/**
	 * Count the retry times 
	 **/
	public int tryCount() {
		
		if(0 != tryCount && stopWatch.isRunning()) {
			stopWatch.stop();
		}
		tryCount ++;
		stopWatch.start("push_try__"+ tryCount);
		
		return tryCount;
	}
	
	/**
	 * Stop Tracing 
	 **/
	public void stopTrace() {
		if(stopWatch.isRunning()) {
			stopWatch.stop();
		}
	}
	
	/**
	 * Get the sending process elapse time in million second 
	 **/
	public long getElapsedTime() {
		if(stopWatch.isRunning()) {
			stopWatch.stop();
		}
		return stopWatch.getTotalTimeMillis();
	}
	
	/**
	 * Get the data to be sent  
	 **/
	public T getSendData() {
		return sendData;
	}
	
	/**
	 * Get the receive url 
	 **/
	public String getUrl() {
		return this.url;
	}
}
