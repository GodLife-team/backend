package com.god.life.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {


    @Primary
    @Bean
    public Executor executors(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 기본 스레드 수
//        executor.setQueueCapacity(100); // 최대 작업 대기 개수
//        executor.setMaxPoolSize(20); // 최대 쓰레드 수
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); //꽉차면 호출한 쓰레드에서 수행
//        executor.setWaitForTasksToCompleteOnShutdown(true); // graceful shutdown
//        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }



}
