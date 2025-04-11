package com.example.moodwriter.global.config;

import com.example.moodwriter.global.config.decorator.NotificationErrorHandlingTaskDecorator;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Value("${async.enabled:false}")
  private boolean asyncEnabled;

  @Bean(name = "notificationTaskExecutor")
  public Executor notificationTaskExecutor() {
    if (asyncEnabled) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(10);
      executor.setMaxPoolSize(20);
      executor.setQueueCapacity(50);
      executor.setThreadNamePrefix("Notification-");
      executor.setTaskDecorator(new NotificationErrorHandlingTaskDecorator());
      executor.initialize();
      return executor;
    } else {
      return Runnable::run;
    }
  }

}
