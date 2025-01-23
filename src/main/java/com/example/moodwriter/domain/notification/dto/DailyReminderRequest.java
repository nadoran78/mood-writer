package com.example.moodwriter.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyReminderRequest {

  @NotNull
  @JsonProperty("isActivate")
  private boolean isActivate;

  @NotNull
  private LocalTime remindTime;

  public static DailyReminderRequest firstRequest() {
    return DailyReminderRequest.builder()
        .isActivate(true)
        .remindTime(LocalTime.of(18, 0))
        .build();
  }
}
