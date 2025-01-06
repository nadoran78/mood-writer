package com.example.moodwriter.domain.notification.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyReminderRequest {

  private boolean isActivate;

  private LocalTime remindTime;
}
