package com.example.moodwriter.domain.notification.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyReminderRequest {

  @NotNull
  private boolean isActivate;

  @NotNull
  private LocalTime remindTime;
}
