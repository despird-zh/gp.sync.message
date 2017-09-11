package com.gp.sync.client;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@EnableAsync
public class SyncClientConfigurer implements AsyncConfigurer {

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
	SyncPushProcess pushProcess() {
		return new SyncPushProcess(restTemplate());
	}
	
	@Bean
	RestTemplate restTemplate() {
		RestTemplate rtv = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		
		return rtv;
	}
}
