package com.gp.sync.client;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * The configuration class for sync client 
 * 
 * @author gdiao
 * @version 0.1 2016-6-6
 * 
 **/
@Configuration
@EnableAsync
public class SyncNodeConfigurer implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {

		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setThreadNamePrefix("SyncPusher-");
        taskExecutor.initialize();
        return taskExecutor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		
		return new AsyncExcepHandler();
	}
	
	@Bean
	SyncTriggerProcess pushProcess(RestTemplate restTemplate) {
		return new SyncTriggerProcess(restTemplate);
	}
	
	@Bean
	SyncAuthenProcess authenProcess(RestTemplate restTemplate) {
		return new SyncAuthenProcess(restTemplate);
	}
	
    /**
     * Prepare the rest template for http json data requesting 
     **/
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }
    
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);//ms
        factory.setConnectTimeout(15000);//ms
        return factory;
    }
}
