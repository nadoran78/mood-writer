package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import com.example.moodwriter.domain.notification.exception.NotificationException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {

  private final FcmService fcmService;
  private final FcmTokenRepository fcmTokenRepository;
  private final NotificationRecipientRepository notificationRecipientRepository;

  @Override
  @Async("notificationTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendBySchedule(NotificationScheduleDto schedule) {
    log.info("Sending notification by schedule: {}", schedule.getId());

    NotificationRecipient recipient = notificationRecipientRepository
        .findById(schedule.getRecipientId())
        .orElseThrow(() -> new NotificationException(
            ErrorCode.NOT_FOUND_NOTIFICATION_RECIPIENT));

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
