package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.dao.NotificationScheduleRepository;
import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

  private final RedisNotificationService redisNotificationService;
  private final NotificationSender notificationSender;
  private final NotificationScheduleRepository notificationScheduleRepository;

  @Transactional
  public void processNotifications() {
    LocalTime now = LocalTime.now();
    log.info("Notification Scheduler start at {}", now);
    Set<String> notificationIds = redisNotificationService.getNotificationsToSend(now);
    log.info("Notification Schedule Task {}", notificationIds.size());

    for (String id : notificationIds) {
      notificationScheduleRepository.findById(UUID.fromString(id))
          .ifPresent(schedule -> notificationSender.sendBySchedule(
              NotificationScheduleDto.from(schedule)));
      log.info("notification is sent. Notification id : {}", id);
    }

    log.info("Notification Scheduler is finished!");
  }

}
