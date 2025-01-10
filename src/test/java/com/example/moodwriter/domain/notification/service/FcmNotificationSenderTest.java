package com.example.moodwriter.domain.notification.service;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import com.example.moodwriter.domain.user.entity.User;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmNotificationSenderTest {

  @Mock
  private FcmService fcmService;

  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @InjectMocks
  private FcmNotificationSender fcmNotificationSender;

  private NotificationSchedule schedule;
  private NotificationRecipient recipient;
  private Notification notification;
  private List<FcmToken> fcmTokens;

  @BeforeEach
  void setUp() {
    // Mock Notification
    User mockUser = mock(User.class);

    notification = spy(Notification.builder()
        .title("Test Title")
        .body("Test Body")
        .data(Map.of("key", "value"))
        .build());

    // Mock NotificationRecipient
    recipient = spy(NotificationRecipient.builder()
        .notification(notification)
        .user(mockUser)
        .build());

    // Mock NotificationSchedule
    schedule = spy(NotificationSchedule.builder()
        .recipient(recipient)
        .scheduledTime(LocalTime.now())
        .build());

    // Mock FcmTokens
    FcmToken token1 = spy(FcmToken.builder()
        .fcmToken("token1")
        .user(recipient.getUser())
        .isActive(true)
        .build());

    FcmToken token2 = spy(FcmToken.builder()
        .fcmToken("token2")
        .user(recipient.getUser())
        .isActive(true)
        .build());

    fcmTokens = List.of(token1, token2);
  }

  @Test
  void sendBySchedule_shouldSendNotificationToAllTokens() {
    // Arrange
    given(fcmTokenRepository.findAllByUserAndIsActiveTrue(recipient.getUser()))
        .willReturn(fcmTokens);

    String notificationTitle = notification.getTitle();
    String notificationBody = notification.getBody();
    Map<String, String> notificationData = notification.getData();

    // Act
    fcmNotificationSender.sendBySchedule(schedule);

    // Assert
    for (FcmToken token : fcmTokens) {
      verify(fcmService, times(1)).sendNotificationByToken(
          eq(token.getFcmToken()),
          eq(notificationTitle),
          eq(notificationBody),
          eq(notificationData)
      );
    }

    verifyNoMoreInteractions(fcmService);
  }

  @Test
  void sendBySchedule_shouldHandleEmptyFcmTokens() {
    // Arrange
    given(fcmTokenRepository.findAllByUserAndIsActiveTrue(recipient.getUser()))
        .willReturn(Collections.emptyList());

    // Act
    fcmNotificationSender.sendBySchedule(schedule);

    // Assert
    verify(fcmService, never()).sendNotificationByToken(anyString(), anyString(),
        anyString(), anyMap());
  }

}