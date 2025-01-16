package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;

public interface NotificationSender {
  void sendBySchedule(NotificationScheduleDto schedule);

}
