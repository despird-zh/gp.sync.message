package com.gp.sync.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gp.common.IdKeys;
import com.gp.info.InfoId;

/**
 * The push message be send to SyncTriggerClient.
 * 
 * @author gdiao
 * @version 0.1 2016-10-12
 *  
 **/
public class SyncTriggerMessage extends SyncPushMessage{

	@JsonDeserialize(using = IdKeys.InfoIdDeserializer.class)
	@JsonSerialize(using = IdKeys.InfoIdSerializer.class)
	private InfoId<Long> infoId;

	public InfoId<Long> getInfoId() {
		return infoId;
	}

	public void setInfoId(InfoId<Long> infoId) {
		this.infoId = infoId;
	}
}
