package com.example.moodwriter.domain.notification.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyReminderResponse {

  private boolean isActive;
  private LocalTime remindTime;

  public static DailyReminderResponse from(boolean isActive, LocalTime remindTime) {
    return DailyReminderResponse.builder()
        .isActive(isActive)
        .remindTime(remindTime)
        .build();
  }

}
