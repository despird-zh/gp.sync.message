package com.gp.sync.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.gp.sync.message.SyncMessages;
import com.gp.sync.message.SyncPushMessage;
import com.gp.util.CommonUtils;
import com.gp.web.ActionResult;
import com.gp.web.ActionResult.Meta;

public class SyncPushProcess {

	static Logger LOGGER = LoggerFactory.getLogger(SyncPushProcess.class);
	
	private RestTemplate restTemplate;
	private String url;
	
	public SyncPushProcess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Async
	public void processPush(SyncPushMessage pushMessage){
		
		HttpHeaders headers = new HttpHeaders(); 
        headers.add("Content-Type", "text/html"); 
        headers.add("Accept", "application/json;"); 
        headers.add("Accept-Encoding", "gzip, deflate, sdch"); 
        headers.add("Cache-Control", "max-age=0"); 
        headers.add("Connection", "keep-alive"); 
        
        String body = SyncMessages.wrapPushMessageJson(pushMessage);
        if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("trying to push message: {}", body);
        }
        
        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers); 
        
        ResponseEntity<String> respEntity = restTemplate.exchange(url,  
                HttpMethod.POST, requestEntity, String.class);  
        HttpStatus status = respEntity.getStatusCode();  
        
        ActionResult result = null;
        
        if(status.is2xxSuccessful()) {
        		String content = respEntity.getBody();
        		result = parse(content);
        		if(LOGGER.isDebugEnabled()) {
        			LOGGER.debug("success send the message");
        		}
        }else {
        		result = ActionResult.failure("Fail to push message to remote server.");
        		if(LOGGER.isDebugEnabled()) {
        			LOGGER.debug("fail send the message");
        		}
        }
        
		// process the response 
		
	}
	
	private ActionResult parse(String response) {
		
		try {
			
			JsonNode root = CommonUtils.JSON_MAPPER.readTree(response);
			ActionResult rtv = new ActionResult();
			
			Meta meta = new Meta(
					root.path("meta").path("state").textValue(),
					root.path("meta").path("message").textValue()
				);
			meta.setCode(root.path("meta").path("code").textValue());
			
			rtv.setMeta(meta);
			
			return rtv;
		} catch (Exception e) {
			LOGGER.debug("Error parse the response body:", response);
		}
		
		return null;
	}
}
