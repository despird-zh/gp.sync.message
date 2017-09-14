package com.gp.sync.client;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import com.gp.sync.SyncClientProcess;
import com.gp.sync.SyncMessages;
import com.gp.common.Synchronizes.SyncState;
import com.gp.sync.message.SyncPushMessage;
import com.gp.sync.message.SyncTriggerMessage;
import com.gp.web.ActionResult;
import com.gp.web.servlet.ServiceTokenFilter.AuthTokenState;

/**
 * Use the restTemplate to push the message
 * 
 * @author gdiao
 * @version 0.1 2016-10-12
 **/
public class SyncTriggerProcess extends SyncClientProcess{

	private RestTemplate restTemplate;

	/**
	 * Constructor with rest template 
	 **/
	public SyncTriggerProcess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	/**
	 * Send the {@link SyncPushMessage} to sync node, this method executed in async mode.
	 * 
	 * @param sendTracer the tracer to collect the elapse and retry times
	 * @param token the Authentication token 
	 **/
	@Async
	public void processSend(SyncSendTracer<SyncTriggerMessage> sendTracer, String token){

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
	        				SyncNodeClient.getInstance().clearToken();
	        				SyncNodeClient.getInstance().updateMessage(sendTracer.getSendData(), SyncState.SEND_FAIL);
	        			}else {
	        				// fail from sync-push method
	        				SyncNodeClient.getInstance().updateMessage(sendTracer.getSendData(), SyncState.SEND_FAIL);
	        			}
	        		}else {
	        			// success
	        			SyncNodeClient.getInstance().updateMessage(sendTracer.getSendData(), SyncState.SENT);
	        		}
	        }else {
	        		// net reason
	        		needResend = true;
	        		SyncNodeClient.getInstance().updateMessage(sendTracer.getSendData(), SyncState.SEND_FAIL);
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
			SyncNodeClient.getInstance().pushMessage(sendTracer);
		}
	}
	
	/**
	 * Convert the ActionResult meta code into AuthTokenState
	 * @param code the code of action result 
	 **/
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
