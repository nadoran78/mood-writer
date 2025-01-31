package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.constant.NotificationTopic;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.dao.NotificationRepository;
import com.example.moodwriter.domain.notification.dao.NotificationScheduleRepository;
import com.example.moodwriter.domain.notification.dto.DailyReminderRequest;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import com.example.moodwriter.domain.notification.exception.NotificationException;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.exception.code.ErrorCode;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

  private final RedisNotificationService redisNotificationService;
  private final NotificationRecipientRepository notificationRecipientRepository;
  private final NotificationRepository notificationRepository;
  private final NotificationScheduleRepository notificationScheduleRepository;
  private final EntityManager entityManager;

  @Transactional
  public void activateDailyReminder(DailyReminderRequest request, UUID userId) {
    Notification notification = notificationRepository.findByTopic(
            NotificationTopic.DAILY_REMINDER)
        .orElseThrow(() -> new NotificationException(ErrorCode.NOT_FOUND_NOTIFICATION));

    NotificationRecipient recipient = notificationRecipientRepository
        .findByUserIdAndNotificationId(userId, notification.getId())
        .orElse(null);

    NotificationSchedule schedule;
    if (recipient == null) {
      User userProxy = entityManager.getReference(User.class, userId);
      recipient = NotificationRecipient.from(notification, userProxy);

      schedule = NotificationSchedule.builder()
          .recipient(recipient)
          .scheduledTime(request.getRemindTime())
          .build();
    } else {
      schedule = notificationScheduleRepository.findByRecipient(recipient)
          .orElseThrow(
              () -> new NotificationException(ErrorCode.NOT_FOUND_NOTIFICATION_SCHEDULE));

      schedule.updateScheduledTime(request.getRemindTime());
    }

    NotificationSchedule savedSchedule = notificationScheduleRepository.save(schedule);

    if (request.isActivate()) {
      recipient.activate();
      redisNotificationService.scheduleNotification(savedSchedule);
    } else {
      recipient.deactivate();
      redisNotificationService.removeScheduledNotification(savedSchedule.getId());
    }

    notificationRecipientRepository.save(recipient);
  }
}
