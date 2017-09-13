package com.gp.sync.message;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SyncMessages {
	
	static Logger LOGGER = LoggerFactory.getLogger(SyncMessages.class);
	
	public static enum SyncState{
		PENDING, // ready for further process
		SENT, // be sent out
		SEND_FAIL, // fail send out
		RECEIVED, // be received 
		PROCESSED, // be handled
		PROCESS_ERROR, // be process error
	}
	
	public static ObjectMapper MESSAGE_MAPPER = new ObjectMapper();
	public static final DateFormat JSON_DT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
	/**
	 * decorate the mapper with necessary feature. 
	 **/
	static {
		
		MESSAGE_MAPPER.setDateFormat(JSON_DT_FORMATTER);
		if(LOGGER.isDebugEnabled())
			MESSAGE_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
		
	}
	
	/**
	 * Make ObjectMapper support the InfoId module 
	 * @param mapper
	 **/
	public static void withSyncTypeModule(final ObjectMapper mapper) {
		
		final SimpleModule module = new SimpleModule("SyncTypeSerializeModule");
		
		JsonSerializer<SyncType> serializer = new SyncTypeSerializer();
		JsonDeserializer<SyncType> deserializer = new SyncTypeDeserializer();
	    module.addDeserializer(SyncType.class, deserializer);
	    module.addSerializer(SyncType.class, serializer);
	    
	    mapper.registerModule(module);
	}
	
	/**
	 * The serializer to support InfoId 
	 **/
	public static class SyncTypeSerializer extends JsonSerializer<SyncType>{

		@Override
		public void serialize(SyncType arg0, JsonGenerator jsonGenerator, SerializerProvider arg2)
				throws IOException, JsonProcessingException {
			
			jsonGenerator.writeString(arg0.toString());
		}
	}
	
	/**
	 * the deserializer to support InfoId
	 **/
	public static class SyncTypeDeserializer extends JsonDeserializer<SyncType>{

		@Override
		public SyncType deserialize(JsonParser parser, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			String fullStr = parser.getValueAsString();
			SyncType rtv = new SyncType(fullStr);
			return rtv;
		}
		
	}
	
	/**
	 * parse the push message 
	 **/
	public static SyncPushMessage parsePushMessage(byte[] optJson) {
		
		SyncPushMessage pushMessage = null;
		try {
			pushMessage = MESSAGE_MAPPER.readValue(optJson, SyncPushMessage.class);
		} catch (IOException e) {
			LOGGER.debug("Fail to parse the PushMessage Json string", e);
		}
		
		return pushMessage;
	}
	
	/**
	 * wrap the sync push message into byte array( json string)
	 **/
	public static byte[] wrapPushMessage(SyncPushMessage pushMsg) {
		
		if(null == pushMsg) return new byte[0];
		try {
			return MESSAGE_MAPPER.writeValueAsBytes(pushMsg);
		} catch (IOException e) {
			LOGGER.debug("Fail to wrap the PushMessage into byte[]", e);
		}
		
		return new byte[0];
	}
	
	/**
	 * wrap the sync push message into json string
	 **/
	public static String wrapPushMessageJson(SyncPushMessage pushMsg) {
		
		if(null == pushMsg) return StringUtils.EMPTY;
		try {
			return MESSAGE_MAPPER.writeValueAsString(pushMsg);
		} catch (IOException e) {
			LOGGER.debug("Fail to wrap the PushMessage into String", e);
		}
		
		return StringUtils.EMPTY;
	}
	
	/**
	 * parse the notify message 
	 **/
	public static SyncNotifyMessage parseNotifyMessage(Optional<String> optJson) {
		
		SyncNotifyMessage notifyMessage = null;
		try {
			notifyMessage = MESSAGE_MAPPER.readValue(optJson.get(), SyncNotifyMessage.class);
		} catch (IOException e) {
			LOGGER.debug("Fail to parse the PushMessage Json string", e);
		}
		
		return notifyMessage;
	}
}
