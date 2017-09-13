package com.gp.sync.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gp.core.AppContextHelper;
import com.gp.sync.message.SyncPushMessage;
import com.gp.web.ActionResult;
import com.gp.web.servlet.ServiceTokenFilter.AuthTokenState;

/**
 * Provide a singleton instance to send message to remove server, the client will maintain the valid token for sync message pushing.
 * This package is prepared for the client system that need to push sync message to sync node.
 * 
 * @author gdiao
 * @version 0.1 2016-12-12
 * 
 **/
public class SyncHttpClient {
	
	static Logger LOGGER = LoggerFactory.getLogger(SyncHttpClient.class);
	
	private static SyncHttpClient httpClient;
	
	private SyncPushProcess pushProcess;
	private SyncAuthenProcess authenProcess;

	private Lock authLock = new ReentrantLock();
	
	private String user;
	private String password;
	private String authenUrl;
	private String audience;
	private String token;
	
	private AuthTokenState tokenState;
	
	/**
	 * Get the singleton instance 
	 * 
	 **/
	public static SyncHttpClient getInstance() {
		
		if(null == httpClient) {
			
			httpClient = new SyncHttpClient();
		}
		return httpClient;
	}
	
	/**
	 * Set the authentication setting for token generation.
	 * 
	 * @param user
	 * @param password
	 * @param authenUrl
	 * @param audience 
	 **/
	public void setAuthenSetting(String user, String password, String authenUrl, String audience) {
		this.user = user;
		this.password = password;
		this.authenUrl = authenUrl;
		this.audience = audience;
	}
	
	/**
	 * Hidden constructor, here initialize the message push process {@link SyncPushProcess} 
	 * and authenticate process {@link SyncAuthenProcess}
	 **/
	private SyncHttpClient() {
		
		this.pushProcess = AppContextHelper.getSpringBean(SyncPushProcess.class);
		this.authenProcess = AppContextHelper.getSpringBean(SyncAuthenProcess.class);
	}
	
	/**
	 * Push Sync Message to server for first time
	 * 
	 * @param url the URL path
	 * @param pushMessage the push message 
	 **/
	public void pushMessage(String url, SyncPushMessage pushMessage) {
		
		this.pushProcess.persistMessage(pushMessage);
		AuthTokenState state = refreshToken();
		
		if(state == AuthTokenState.VALID_TOKEN) {
			SyncSendTracer<SyncPushMessage> pushTracer = new SyncSendTracer<SyncPushMessage>(url, pushMessage);
			this.pushProcess.processPush(pushTracer, this.token);
		}else if(state == AuthTokenState.FAIL_AUTHC) {
			LOGGER.debug("Ignore push sync message, fail to issue token coz of wrong account or pwd");
		}else if(state == AuthTokenState.UNKNOWN) {
			LOGGER.debug("Ignore push sync message, coz of network broken or unknow error");
		}else {
			LOGGER.debug("Ignore push sync message, coz of network broken or unknow error");
		}
	}
	
	/**
	 * Push sync message again, here we check the overtime and other situations.
	 * 
	 * @param pushTracer the sending tracer.
	 **/
	void pushMessage(SyncSendTracer<SyncPushMessage> pushTracer) {
				
		AuthTokenState state = refreshToken();
		if(state == AuthTokenState.VALID_TOKEN) {
			this.pushProcess.processPush(pushTracer, this.token);
		}else if(state == AuthTokenState.FAIL_AUTHC) {
			LOGGER.debug("Ignore push sync message, fail to issue token coz of wrong account or pwd");
		}else if(state == AuthTokenState.UNKNOWN) {
			LOGGER.debug("Ignore push sync message, coz of network broken or unknow error");
		}else {
			LOGGER.debug("Ignore push sync message, coz of network broken or unknow error");
		}
	}
	
	/**
	 * Try to refresh the token. 
	 * 
	 * @return AuthTokenState there is 3 cases:
	 * <ul>		
	 * 	<li>VALID_TOKEN - the token is valid</li>
	 *  <li>FAIL_AUTHC - fail authenticate</li>
	 *  <li>UNKNOWN - unknown reason, possible network broken</li>
	 * </ul>
	 **/
	AuthTokenState refreshToken() {
		
		if(StringUtils.isBlank(token)) {
			authLock.lock();
			if(StringUtils.isNotBlank(token)) {
				authLock.unlock();
				return tokenState = AuthTokenState.VALID_TOKEN;
			}
			try {
				Map<String, String> dataMap = new HashMap<String, String>();
				dataMap.put("principal", user);
				dataMap.put("credential", password);
				dataMap.put("audience", audience);
				SyncSendTracer<Map<String, String>> authTracer = new SyncSendTracer<Map<String, String>>(authenUrl, dataMap);
				
				ActionResult result = this.authenProcess.tryIssueToken(authTracer);
				if(result.isSuccess()) {
					this.token = (String) result.getData();
					return tokenState = AuthTokenState.VALID_TOKEN;
				}else {
					if(AuthTokenState.FAIL_AUTHC.name().equals(result.getMeta().getCode())) {
						return tokenState = AuthTokenState.FAIL_AUTHC;
					}else {
						return tokenState = AuthTokenState.UNKNOWN;
					}
				}
			}catch(Exception e){
				
				LOGGER.error("fail to send authen message to remote server.", e);
				return tokenState = AuthTokenState.UNKNOWN;
			}finally {
				authLock.unlock();
			}
		}else {
			return tokenState = AuthTokenState.VALID_TOKEN;
		}
	}
	
	/**
	 * Try to clean the token, indicate to refresh the token to valid one. 
	 **/
	void clearToken() {
		this.token = null;
	}

	/**
	 * Get the state token
	 * 
	 * @return AuthTokenState there is 3 cases:
	 * <ul>		
	 * 	<li>VALID_TOKEN - the token is valid</li>
	 *  <li>FAIL_AUTHC - fail authenticate</li>
	 *  <li>UNKNOWN - unknown reason, possible network broken</li>
	 * </ul>
	 **/
	AuthTokenState getTokenState(){
		return this.tokenState;
	}
}
