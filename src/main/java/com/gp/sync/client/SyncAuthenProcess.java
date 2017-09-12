package com.gp.sync.client;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.MapUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.gp.util.CommonUtils;
import com.gp.web.ActionResult;

public class SyncAuthenProcess extends SyncClientProcess{

	private RestTemplate restTemplate;
	
	public SyncAuthenProcess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	public ActionResult tryIssueToken(SyncSendTracer<Map<String, String>> authTracer){
		
		Map<String, String> dataMap = authTracer.getSendData();
		
		String json = CommonUtils.toJson(dataMap);
		
		HttpHeaders headers = new HttpHeaders(); 
        headers.add("Content-Type", "text/html"); 
        headers.add("Accept", "application/json;"); 
        headers.add("Accept-Encoding", "gzip, deflate, sdch"); 
        headers.add("Cache-Control", "max-age=0"); 
        
        if(LOGGER.isDebugEnabled()) {
        		LOGGER.debug("trying to issue token: {}", json);
        }
        
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers); 
        
        ResponseEntity<String> respEntity = restTemplate.exchange(authTracer.getUrl(), 
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
        		result.getMeta().setCode("NETWORK_BROKEN");
        		if(LOGGER.isDebugEnabled()) {
        			LOGGER.debug("fail send the message");
        		}
        }
        
		return result;
		
	}

}
