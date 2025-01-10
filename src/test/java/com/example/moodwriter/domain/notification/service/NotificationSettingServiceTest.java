package com.example.moodwriter.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {
  @Mock
  private RedisNotificationService redisNotificationService;

  @Mock
  private NotificationRecipientRepository notificationRecipientRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationScheduleRepository notificationScheduleRepository;

  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private NotificationSettingService notificationSettingService;

  private UUID userId;
  private Notification notification;
  private NotificationRecipient recipient;
  private NotificationSchedule schedule;
  private User user;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    user = mock(User.class);

    notification = spy(Notification.builder()
        .title("Daily Reminder")
        .body("Don't forget to write your journal!")
        .topic(NotificationTopic.DAILY_REMINDER)
        .build());

    recipient = spy(NotificationRecipient.builder()
        .notification(notification)
        .user(user)
        .build());

    schedule = spy(NotificationSchedule.builder()
        .recipient(recipient)
        .scheduledTime(LocalTime.of(8, 0))
        .build());

    if (testInfo.getDisplayName().contains("예외처리")) {
      return;
    }

    given(user.getId()).willReturn(userId);
    given(notification.getId()).willReturn(notificationId);

  }

  @Test
  void activateDailyReminder_shouldCreateNewRecipientAndSchedule() {
    // Arrange
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(true)
        .remindTime(LocalTime.of(9, 0))
        .build();

    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId, notification.getId()))
        .willReturn(Optional.empty());
    given(entityManager.getReference(User.class, userId))
        .willReturn(user);
    given(notificationScheduleRepository.save(any(NotificationSchedule.class)))
        .will(returnsFirstArg());

    // Act
    notificationSettingService.activateDailyReminder(request, userId);

    // Assert
    ArgumentCaptor<NotificationRecipient> recipientCaptor = ArgumentCaptor.forClass(NotificationRecipient.class);
    ArgumentCaptor<NotificationSchedule> scheduleCaptor = ArgumentCaptor.forClass(NotificationSchedule.class);

    verify(notificationRecipientRepository).save(recipientCaptor.capture());
    verify(notificationScheduleRepository).save(any(NotificationSchedule.class));
    verify(redisNotificationService).scheduleNotification(scheduleCaptor.capture());

    NotificationRecipient savedRecipient = recipientCaptor.getValue();
    NotificationSchedule savedSchedule = scheduleCaptor.getValue();

    assertEquals(notification, savedRecipient.getNotification());
    assertEquals(userId, savedRecipient.getUser().getId());
    assertTrue(savedRecipient.isActive());
    assertFalse(savedRecipient.isRead());
    assertNull(savedRecipient.getReadAt());
    assertEquals(savedRecipient, savedSchedule.getRecipient());
    assertEquals(request.getRemindTime(), savedSchedule.getScheduledTime());
  }

  @Test
  void activateDailyReminder_shouldUpdateExistingSchedule() {
    // Arrange
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(true)
        .remindTime(LocalTime.of(10, 0))
        .build();

    recipient.deactivate();

    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId, notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.of(schedule));
    given(notificationScheduleRepository.save(any(NotificationSchedule.class)))
        .will(returnsFirstArg());

    // Act
    notificationSettingService.activateDailyReminder(request, userId);

    // Assert
    ArgumentCaptor<NotificationRecipient> recipientCaptor = ArgumentCaptor.forClass(NotificationRecipient.class);
    ArgumentCaptor<NotificationSchedule> scheduleCaptor = ArgumentCaptor.forClass(NotificationSchedule.class);

    verify(notificationRecipientRepository).save(recipientCaptor.capture());
    verify(notificationScheduleRepository).save(schedule);
    verify(redisNotificationService).scheduleNotification(scheduleCaptor.capture());

    NotificationRecipient savedRecipient = recipientCaptor.getValue();
    NotificationSchedule savedSchedule = scheduleCaptor.getValue();

    assertEquals(notification, savedRecipient.getNotification());
    assertEquals(userId, savedRecipient.getUser().getId());
    assertTrue(savedRecipient.isActive());
    assertFalse(savedRecipient.isRead());
    assertNull(savedRecipient.getReadAt());
    assertEquals(savedRecipient, savedSchedule.getRecipient());
    assertEquals(request.getRemindTime(), savedSchedule.getScheduledTime());
  }

  @Test
  @DisplayName("Notification 엔티티 부존재 시 예외처리")
  void activateDailyReminder_shouldThrowExceptionWhenNotificationNotFound() {
    // Arrange
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(true)
        .remindTime(LocalTime.of(9, 0))
        .build();

    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.empty());

    // Act & Assert
    NotificationException exception = assertThrows(NotificationException.class, () ->
        notificationSettingService.activateDailyReminder(request, userId));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION, exception.getErrorCode());
  }

  @Test
  @DisplayName("NotificationSchedule 엔티티 부존재 시 예외처리")
  void activateDailyReminder_shouldThrowExceptionWhenNotificationScheduleNotFound() {
    // Arrange
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(true)
        .remindTime(LocalTime.of(10, 0))
        .build();

    recipient.deactivate();

    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId, notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.empty());

    // Act & Assert
    NotificationException exception = assertThrows(NotificationException.class, () ->
        notificationSettingService.activateDailyReminder(request, userId));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION_SCHEDULE, exception.getErrorCode());
  }


}