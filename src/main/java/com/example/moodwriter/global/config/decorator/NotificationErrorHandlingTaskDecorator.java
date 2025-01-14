package com.example.moodwriter.global.config.decorator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskDecorator;

@Slf4j
public class NotificationErrorHandlingTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (Exception e) {
        log.error("Error in notification async task: " + e.getMessage());
      }
    };
  }
}
