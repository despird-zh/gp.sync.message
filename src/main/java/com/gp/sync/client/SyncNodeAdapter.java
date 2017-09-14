package com.gp.sync.client;

import com.gp.common.Synchronizes.SyncState;
import com.gp.info.InfoId;
import com.gp.sync.message.SyncTriggerMessage;

public interface SyncNodeAdapter {

	/**
	 * Persist the sync push message
	 **/
	public InfoId<?> persistMessage(SyncTriggerMessage pushMsg);
	
	/**
	 * Update the sync push message 
	 **/
	public void changeMessageState(InfoId<Long> outId, SyncState state);
}
