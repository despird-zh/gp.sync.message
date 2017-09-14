package com.gp.sync;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import com.google.common.collect.Sets;
import com.gp.sync.SyncType;
/**
 * static methods to clean the property map which inherit from {@link CoreEventPayload}  
 * the cleaned property map will be sent as sync command pay load.
 * 
 * @author gdiao
 * @version 0.1 2017-7-6
 **/
public class SyncPayloads {
	
	public static SyncType CMD_UPD_SOURCE = new SyncType("cmd", "upd", "source");
	
	/**
	 * Include the entry with specified
	 * @author gdiao
	 * @param payload the paylod map to be clean
	 * @param includeKeys the keys to reserve
	 **/
	public static Map<String, Object> includePayload(Map<String, Object> payload, String ... includeKeys) {
		
		return cleanPayload(payload, true, includeKeys);
	}
	
	/**
	 * Exclude the entry with specified
	 * @author gdiao
	 * @param payload the paylod map to be clean
	 * @param excludeKeys the keys to remove
	 **/
	public static Map<String, Object> excludePayload(Map<String, Object> payload, String ... excludeKeys) {
		
		return cleanPayload(payload, false, excludeKeys);
	}
	
	/**
	 * Clean the payload of operation message 
	 * 
	 **/
	private static Map<String, Object> cleanPayload(Map<String, Object> payload, boolean include, String ... keys) {
		
		if(MapUtils.isEmpty(payload)) return Collections.emptyMap();
		
		Map<String, Object> rtv = new HashMap<String, Object>();
		
		Set<String> keylist = Sets.newHashSet(keys);
		for(Map.Entry<String, Object> entry: payload.entrySet()) {
			
			if((keylist.contains(entry.getKey())) && include ) {
				rtv.put(entry.getKey(), entry.getValue());
			}
			else if((!keylist.contains(entry.getKey())) && !include ) {
				rtv.put(entry.getKey(), entry.getValue());
			}
		}
		
		return rtv;
	}
}
