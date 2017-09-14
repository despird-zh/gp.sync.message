package com.gp.sync.message;

import com.gp.sync.SyncMessage;

/**
 * The notification is message sent from center node(global/ enterprise) to target nodes .<br>
 * It has 2 kinds content: the target node pull notification and the command aims on target node.<br>
 * 
 * @author diaogc 
 * @version 0.1 2017-5-6
 **/
public class SyncNotifyMessage extends SyncMessage{
	
	private String center;
	
	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

}
