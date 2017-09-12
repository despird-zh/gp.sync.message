package com.gp.sync.client;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import com.gp.sync.message.SyncMessages;
import com.gp.sync.message.SyncPushMessage;
import com.gp.web.ActionResult;
import com.gp.web.servlet.ServiceTokenFilter.AuthTokenState;

public class SyncPushProcess extends SyncClientProcess{

	private RestTemplate restTemplate;

	public SyncPushProcess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Async
	public void processPush(SyncSendTracer<SyncPushMessage> sendTracer, String token){

        ActionResult result = null;
        boolean needResend = false;
        
		sendTracer.tryCount();
		try {
			HttpHeaders headers = new HttpHeaders(); 
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		    headers.add("Authorization", token);

	        String body = SyncMessages.wrapPushMessageJson(sendTracer.getSendData());
	        if(LOGGER.isDebugEnabled()) {
	        		LOGGER.debug("trying to push message: {}", body);
	        }
	       
	        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers); 
	        
	        ResponseEntity<String> respEntity = restTemplate.exchange(sendTracer.getUrl(),  
	                HttpMethod.POST, requestEntity, String.class);  
	        HttpStatus status = respEntity.getStatusCode();  
	        	        
	        if(status.is2xxSuccessful()) {
	        		String content = respEntity.getBody();
	        		result = parse(content);
	        		if(LOGGER.isDebugEnabled()) {
	        			LOGGER.debug("success send the message");
	        		}
	        		
	        		AuthTokenState code = getMetaCode(result.getMeta().getCode());
	        		
	        		if(!result.isSuccess()) {
	        			if(AuthTokenState.BAD_TOKEN == code ||
	        					AuthTokenState.GHOST_TOKEN == code ||
	        					AuthTokenState.INVALID_TOKEN == code ||
	        					AuthTokenState.EXPIRE_TOKEN == code) {
	        				// fail caused by token
	        				needResend = true;
	        				SyncHttpClient.getInstance().clearToken();
	        				updateMessage(sendTracer.getSendData());
	        			}else {
	        				// fail from sync-push method
	        				updateMessage(sendTracer.getSendData());
	        			}
	        		}else {
	        			// success
	        			updateMessage(sendTracer.getSendData());
	        		}
	        }else {
	        		// net reason
	        		needResend = true;
	        		if(LOGGER.isDebugEnabled()) {
	        			LOGGER.debug("Fail to push message to remote server[{}].", status.toString());
	        		}
	        }

		}catch(Exception e){
			LOGGER.error("fail to push message to remote server.", e);
		}finally {
			sendTracer.stopTrace();
		}
		
		if(needResend) {
			SyncHttpClient.getInstance().pushMessage(sendTracer);
		}
	}

	public void persistMessage(SyncPushMessage pushMsg) {
		LOGGER.debug("persist the message");
	}
	
	public void updateMessage(SyncPushMessage pushMsg) {
		LOGGER.debug("update the message");
	}
	
	private AuthTokenState getMetaCode(String code) {
		if(StringUtils.isBlank(code)) {
			return AuthTokenState.UNKNOWN;
		}else {
			code = code.toUpperCase();
		}
		try {
			return AuthTokenState.valueOf(code);
		}catch(Exception e) {
			
			return AuthTokenState.UNKNOWN;
		}
	}
}
