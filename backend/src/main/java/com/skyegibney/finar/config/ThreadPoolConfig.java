package com.skyegibney.finar.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class ThreadPoolConfig {
  @Bean(name = "defaultTaskExecutor")
  public ThreadPoolTaskExecutor defaultTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("finar-backend-default-");
    executor.setRejectedExecutionHandler(
        (r, executor1) -> log.warn("Task rejected thread pool is full and queue is also full"));
    executor.initialize();

    return executor;
  }

  @Bean(name = "timeUpdateTaskExecutor")
  public ThreadPoolTaskExecutor timeUpdateTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("finar-backend-time-update-");
    executor.setRejectedExecutionHandler(
        (r, executor1) -> log.warn("Task rejected thread pool is full and queue is also full"));
    executor.initialize();

    return executor;
  }
}
