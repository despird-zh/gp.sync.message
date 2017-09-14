package com.gp.sync.message;

import com.gp.sync.SyncMessage;

/**
 * the distributed nodes use push structure to send information to center node or global node.
 * so the payload of content could be any operation to be synchronized.
 * 
 * @author admin
 * @version 0.1 2017-9-10
 * 
 **/
public class SyncPushMessage extends SyncMessage{

	private String node;
	
	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

}
