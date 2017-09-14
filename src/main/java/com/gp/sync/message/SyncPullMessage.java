package com.gp.sync.message;

import java.util.Date;

import com.gp.sync.SyncMessage;

public class SyncPullMessage extends SyncMessage{
	
	private Date lastPullTime;
	
	private String lastPullOwm;

	public String getLastPullOwm() {
		return lastPullOwm;
	}

	public void setLastPullOwm(String lastPullOwm) {
		this.lastPullOwm = lastPullOwm;
	}

	public Date getLastPullTime() {
		return lastPullTime;
	}

	public void setLastPullTime(Date lastPullTime) {
		this.lastPullTime = lastPullTime;
	}

}
