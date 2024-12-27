package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.dao.NotificationScheduleRepository;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

  private final RedisNotificationService redisNotificationService;
  private final NotificationSender notificationSender;
  private final NotificationScheduleRepository notificationScheduleRepository;

  @Scheduled(cron = "0 0 0/1 * * *")
  public void processNotifications() {
    LocalTime now = LocalTime.now();
    Set<String> notificationIds = redisNotificationService.getNotificationsToSend(now);

    for (String id : notificationIds) {
      notificationScheduleRepository.findById(UUID.fromString(id))
          .ifPresent(notificationSender::sendBySchedule);
    }
  }

}
