package com.example.moodwriter.domain.notification.dto;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationScheduleDto {

  private UUID id;
  private UUID recipientId;
  private LocalTime scheduledTime;

  public static NotificationScheduleDto from(NotificationSchedule schedule) {
    return NotificationScheduleDto.builder()
        .id(schedule.getId())
        .recipientId(schedule.getRecipient().getId())
        .scheduledTime(schedule.getScheduledTime())
        .build();
  }

}
