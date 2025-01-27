package com.skyegibney.finar;

import com.skyegibney.finar.authorization.User;
import com.skyegibney.finar.authorization.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@SpringBootApplication
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class FinarBackendApplication {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public static void main(String[] args) {
    SpringApplication.run(FinarBackendApplication.class, args);
  }

  @Bean
  Random rng() {
    return new Random();
  }

  @Bean
  CommandLineRunner insertUser() {
    return args -> {
      userRepository.save(new User(0, "astra", passwordEncoder.encode("password"), "astra"));
      userRepository.save(new User(0, "user", passwordEncoder.encode("password"), "user"));
      userRepository.save(new User(0, "kronk", passwordEncoder.encode("password"), "kronk"));
    };
  }

  @Bean(name = "defaultTaskExecutor")
  public ThreadPoolTaskExecutor defaultTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("finar-backend-default-");
    executor.setRejectedExecutionHandler(
        (r, executor1) -> log.warn("Task rejected thred pool is full and queue is also full"));
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
        (r, executor1) -> log.warn("Task rejected thred pool is full and queue is also full"));
    executor.initialize();

    return executor;
  }
}
