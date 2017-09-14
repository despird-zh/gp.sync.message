package com.gp.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.gp.sync.client.SyncTriggerProcess;
import com.gp.util.CommonUtils;
import com.gp.web.ActionResult;
import com.gp.web.ActionResult.Meta;

/**
 * Super class for message sending process
 * provide method to parse the response from remote server 
 **/
public abstract class SyncClientProcess {
	
	public static Logger LOGGER = LoggerFactory.getLogger(SyncTriggerProcess.class);
	
	/**
	 * Parse the remove response content
	 * @return  ActionResult the action result
	 **/
	public ActionResult parse(String response) {
		
		ActionResult rtv = new ActionResult();
		try {
			
			JsonNode root = CommonUtils.JSON_MAPPER.readTree(response);
			
			Meta meta = new Meta(
					root.path("meta").path("state").textValue(),
					root.path("meta").path("message").textValue()
				);
			meta.setCode(root.path("meta").path("code").textValue());
			rtv.setMeta(meta);
			
			rtv.setData(root.path("data").textValue());
			
		} catch (Exception e) {
			
			rtv = ActionResult.error("Error parse the response body:");
			LOGGER.error("Error parse the response body:{}", response);
		}
		
		return rtv;
	}
}
