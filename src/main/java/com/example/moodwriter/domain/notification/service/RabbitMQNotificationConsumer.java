package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.exception.NotificationException;
import com.example.moodwriter.global.config.RabbitMQConfig;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQNotificationConsumer {

  private final FcmService fcmService;
  private final FcmTokenRepository fcmTokenRepository;
  private final NotificationRecipientRepository notificationRecipientRepository;

  @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processNotification(NotificationScheduleDto schedule) {
    log.info("Processing notification: {}", schedule.getId());

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
