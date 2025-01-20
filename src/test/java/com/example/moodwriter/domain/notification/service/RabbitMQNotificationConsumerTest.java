package com.example.moodwriter.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.exception.NotificationException;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RabbitMQNotificationConsumerTest {

  @Mock
  private FcmService fcmService;
  @Mock
  private FcmTokenRepository fcmTokenRepository;
  @Mock
  private NotificationRecipientRepository notificationRecipientRepository;
  @InjectMocks
  private RabbitMQNotificationConsumer consumer;

  @Test
  void success_processNotification() {
    // given
    UUID scheduleId = UUID.randomUUID();
    UUID recipientId = UUID.randomUUID();

    NotificationScheduleDto schedule = NotificationScheduleDto.builder()
        .id(scheduleId)
        .recipientId(recipientId)
        .scheduledTime(LocalTime.of(10, 0))
        .build();

    NotificationRecipient recipient = mock(NotificationRecipient.class);
    User user = mock(User.class);
    FcmToken fcmToken1 = FcmToken.builder()
        .fcmToken("fcmToken1")
        .build();
    FcmToken fcmToken2 = FcmToken.builder()
        .fcmToken("fcmToken2")
        .build();
    List<FcmToken> fcmTokens = List.of(fcmToken1, fcmToken2);

    given(notificationRecipientRepository.findById(recipientId))
        .willReturn(Optional.of(recipient));
    given(recipient.getUser()).willReturn(user);
    given(fcmTokenRepository.findAllByUserAndIsActiveTrue(user))
        .willReturn(fcmTokens);

    Notification notification = Notification.builder()
        .title("title")
        .body("body")
        .data(Map.of("key", "value"))
        .build();
    given(recipient.getNotification()).willReturn(notification);

    // when
    consumer.processNotification(schedule);

    // then
    verify(fcmService, times(1))
        .sendNotificationByToken(fcmToken1.getFcmToken(), notification.getTitle(),
            notification.getBody(), notification.getData());
    verify(fcmService, times(1))
        .sendNotificationByToken(fcmToken2.getFcmToken(), notification.getTitle(),
            notification.getBody(), notification.getData());
  }

  @Test
  void processNotification_shouldThrowException_whenNotificationRecipientIsNotExist() {
    // given
    UUID scheduleId = UUID.randomUUID();
    UUID recipientId = UUID.randomUUID();

    NotificationScheduleDto schedule = NotificationScheduleDto.builder()
        .id(scheduleId)
        .recipientId(recipientId)
        .scheduledTime(LocalTime.of(10, 0))
        .build();

    NotificationRecipient recipient = mock(NotificationRecipient.class);

    given(notificationRecipientRepository.findById(recipientId))
        .willReturn(Optional.empty());

    // when & then
    NotificationException notificationException = assertThrows(
        NotificationException.class, () -> consumer.processNotification(schedule));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION_RECIPIENT,
        notificationException.getErrorCode());
    verifyNoInteractions(fcmService);
  }
}