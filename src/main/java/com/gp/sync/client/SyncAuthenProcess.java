package com.gp.sync.client;

import java.util.Arrays;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.gp.sync.SyncClientProcess;
import com.gp.util.CommonUtils;
import com.gp.web.ActionResult;

/**
 * Process the authentication to generate token 
 * 
 * @author gdiao
 * @version 0.1 2016-12-10
 * 
 **/
public class SyncAuthenProcess extends SyncClientProcess{

	private RestTemplate restTemplate;
	
	/**
	 * Constructor with rest template
	 * @param restTemplate the template of rest request
	 **/
	public SyncAuthenProcess(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	/**
	 * Try to issue the jwt token, it runs in synchronize mode.
	 * 
	 * @param authTracer the authentication tracer 
	 * @return ActionResult the action result, when [VALID_TOKEN = meta.code] the data string is token.
	 * 
	 **/
	public ActionResult tryIssueToken(SyncSendTracer<Map<String, String>> authTracer){
		
		Map<String, String> dataMap = authTracer.getSendData();
		
		String json = CommonUtils.toJson(dataMap);
		
		HttpHeaders headers = new HttpHeaders(); 
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
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
