package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;

public interface NotificationSender {
  void sendBySchedule(NotificationSchedule schedule);

}
