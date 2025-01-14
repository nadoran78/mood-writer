package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {

  private final FcmService fcmService;
  private final FcmTokenRepository fcmTokenRepository;

  @Override
  @Async("notificationTaskExecutor")
  public void sendBySchedule(NotificationSchedule schedule) {
    log.info("Sending notification by schedule: {}", schedule.getId());

    NotificationRecipient recipient = schedule.getRecipient();

    List<FcmToken> fcmTokens = fcmTokenRepository.findAllByUserAndIsActiveTrue(
        recipient.getUser());

    Notification notification = recipient.getNotification();

    for (FcmToken fcmToken : fcmTokens) {
      fcmService.sendNotificationByToken(fcmToken.getFcmToken(),
          notification.getTitle(),
          notification.getBody(), notification.getData());
    }
  }
}
