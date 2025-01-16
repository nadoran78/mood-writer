package com.example.moodwriter.domain.notification.service;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.domain.notification.entity.Notification;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import com.example.moodwriter.domain.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class FcmNotificationSenderAsyncTest {

  @MockBean
  private FcmService fcmService;

  @MockBean
  private FcmTokenRepository fcmTokenRepository;

  @MockBean
  private NotificationRecipientRepository notificationRecipientRepository;

  @Autowired
  private NotificationSender notificationSender;

  @Test
  void sendBySchedule_shouldRunInDifferentThread() {
    // Arrange
    AtomicReference<String> threadName = new AtomicReference<>();

    FcmToken fcmToken = mock(FcmToken.class);
    NotificationSchedule schedule = mock(NotificationSchedule.class);
    NotificationRecipient recipient = mock(NotificationRecipient.class);
    User user = mock(User.class);
    Notification notification = Notification.builder()
        .title("title")
        .body("body")
        .data(Map.of("key", "value"))
        .build();

    given(notificationRecipientRepository.findById(any(UUID.class))).willReturn(
        Optional.of(recipient));
    given(fcmTokenRepository.findAllByUserAndIsActiveTrue(any()))
        .willReturn(List.of(fcmToken));

    doAnswer(invocation -> {
      threadName.set(Thread.currentThread().getName());
      return null;
    }).when(fcmService)
        .sendNotificationByToken(anyString(), anyString(), anyString(), anyMap());

    given(schedule.getId()).willReturn(UUID.randomUUID());
    given(schedule.getRecipient()).willReturn(recipient);
    given(recipient.getId()).willReturn(UUID.randomUUID());
    given(fcmToken.getFcmToken()).willReturn("fcmToken");
    given(schedule.getRecipient()).willReturn(recipient);
    given(recipient.getUser()).willReturn(user);
    given(recipient.getNotification()).willReturn(notification);

    // Act
    notificationSender.sendBySchedule(NotificationScheduleDto.from(schedule));

    // Assert
    await().atMost(5, SECONDS).untilAsserted(() -> {
      assertNotNull(threadName.get(), "Thread name should not be null");
      assertTrue(threadName.get().startsWith("Notification-"),
          "Expected async thread but found: " + threadName.get());
    });
  }
}