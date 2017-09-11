package com.gp.sync.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gp.util.RawJsonDeserializer;

public abstract class SyncMessage {

	@JsonDeserialize(using = SyncMessages.SyncTypeDeserializer.class)
	@JsonSerialize(using = SyncMessages.SyncTypeSerializer.class)
	private SyncType type;
	
	private String traceCode;
	
	@JsonDeserialize(using = RawJsonDeserializer.class)
	private Object payload;

	public SyncType getType() {
		return type;
	}

	public void setType(SyncType type) {
		this.type = type;
	}

	public String getTraceCode() {
		return traceCode;
	}

	public void setTraceCode(String traceCode) {
		this.traceCode = traceCode;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

}
