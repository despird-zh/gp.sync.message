package com.gp.sync.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.gp.common.AccessPoint;
import com.gp.common.GPrincipal;
import com.gp.common.GroupUsers;
import com.gp.common.JwtPayload;
import com.gp.common.SystemOptions;
import com.gp.core.AppContextHelper;
import com.gp.core.MasterFacade;
import com.gp.core.SecurityFacade;
import com.gp.dao.info.SysOptionInfo;
import com.gp.exception.CoreException;
import com.gp.sync.message.SyncPushMessage;
import com.gp.util.JwtTokenUtils;
import com.gp.web.ActionResult;
import com.gp.web.servlet.ServiceTokenFilter.AuthTokenState;
import com.gp.web.util.ExWebUtils;

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
	
	public static SyncHttpClient getInstance() {
		
		if(null == httpClient) {
			
			httpClient = new SyncHttpClient();
		}
		return httpClient;
	}
	
	public void setAuthenSetting(String user, String password, String authenUrl, String audience) {
		this.user = user;
		this.password = password;
		this.authenUrl = authenUrl;
		this.audience = audience;
	}
	
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
	 * @return AuthTokenState there is 3 cases:
	 * 		
	 **/
	AuthTokenState refreshToken() {
		
		if(StringUtils.isBlank(token)) {
			authLock.lock();
			if(StringUtils.isNotBlank(token)) {
				authLock.unlock();
				return AuthTokenState.VALID_TOKEN;
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
					return AuthTokenState.VALID_TOKEN;
				}else {
					if(AuthTokenState.FAIL_AUTHC.name().equals(result.getMeta().getCode())) {
						return AuthTokenState.FAIL_AUTHC;
					}else {
						return AuthTokenState.UNKNOWN;
					}
				}
			}finally {
				authLock.unlock();
			}
		}else {
			return AuthTokenState.VALID_TOKEN;
		}
	}
	
	/**
	 * Try to clean the token, indicate to refresh the token to valid one. 
	 **/
	void clearToken() {
		this.token = null;
	}

}
