package com.example.moodwriter.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.notification.constant.NotificationTopic;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.dao.NotificationRepository;
import com.example.moodwriter.domain.notification.dao.NotificationScheduleRepository;
import com.example.moodwriter.domain.notification.dto.DailyReminderRequest;
import com.example.moodwriter.domain.notification.dto.DailyReminderResponse;
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
  private UUID scheduleId;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    scheduleId = UUID.randomUUID();

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

    if (!testInfo.getDisplayName().contains("예외처리")) {
      given(notification.getId()).willReturn(notificationId);
    }
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
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.empty());
    given(entityManager.getReference(User.class, userId))
        .willReturn(user);
    given(user.getId()).willReturn(userId);
    given(notificationScheduleRepository.save(any(NotificationSchedule.class)))
        .will(invocation -> {
          NotificationSchedule savedSchedule = invocation.getArgument(0);

          NotificationSchedule spySchedule = spy(savedSchedule);
          given(spySchedule.getId()).willReturn(scheduleId);

          return spySchedule;
        });

    // Act
    notificationSettingService.activateDailyReminder(request, userId);

    // Assert
    ArgumentCaptor<NotificationRecipient> recipientCaptor = ArgumentCaptor.forClass(
        NotificationRecipient.class);
    ArgumentCaptor<NotificationSchedule> scheduleCaptor = ArgumentCaptor.forClass(
        NotificationSchedule.class);

    verify(notificationRecipientRepository, times(2)).save(recipientCaptor.capture());
    verify(notificationScheduleRepository).save(scheduleCaptor.capture());
    verify(redisNotificationService)
        .scheduleNotification(request.getRemindTime(), scheduleId);

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
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.of(schedule));
    given(schedule.getId()).willReturn(scheduleId);
    given(notificationScheduleRepository.save(any(NotificationSchedule.class)))
        .will(returnsFirstArg());
    given(user.getId()).willReturn(userId);

    // Act
    notificationSettingService.activateDailyReminder(request, userId);

    // Assert
    ArgumentCaptor<NotificationRecipient> recipientCaptor = ArgumentCaptor.forClass(
        NotificationRecipient.class);

    verify(notificationRecipientRepository).save(recipientCaptor.capture());
    verify(notificationScheduleRepository).save(schedule);
    verify(redisNotificationService)
        .scheduleNotification(request.getRemindTime(), scheduleId);

    NotificationRecipient savedRecipient = recipientCaptor.getValue();

    assertEquals(notification, savedRecipient.getNotification());
    assertEquals(userId, savedRecipient.getUser().getId());
    assertTrue(savedRecipient.isActive());
    assertFalse(savedRecipient.isRead());
    assertNull(savedRecipient.getReadAt());
    assertEquals(savedRecipient, schedule.getRecipient());
    assertEquals(request.getRemindTime(), schedule.getScheduledTime());
  }

  @Test
  void activateDailyReminder_shouldDeactivateDailyReminder() {
    // given
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(false)
        .remindTime(LocalTime.of(10, 0))
        .build();

    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.of(schedule));
    given(notificationScheduleRepository.save(any(NotificationSchedule.class)))
        .will(returnsFirstArg());

    // when
    notificationSettingService.activateDailyReminder(request, userId);

    // then
    verify(notificationScheduleRepository).findByRecipient(recipient);
    verify(redisNotificationService).removeScheduledNotification(schedule.getId());
    verify(notificationScheduleRepository).save(schedule);
    verify(notificationRecipientRepository).save(recipient);

    assertFalse(recipient.isActive());
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
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.empty());

    // Act & Assert
    NotificationException exception = assertThrows(NotificationException.class, () ->
        notificationSettingService.activateDailyReminder(request, userId));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION_SCHEDULE, exception.getErrorCode());
  }

  @Test
  void successGetDailyReminder() {
    // given
    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.of(schedule));

    // when
    DailyReminderResponse response = notificationSettingService.getDailyReminder(
        userId);

    // then
    assertEquals(recipient.isActive(), response.isActive());
    assertEquals(schedule.getScheduledTime(), response.getRemindTime());
  }

  @Test
  @DisplayName("daily reminder 조회: Notification 엔티티 부존재 시 예외처리")
  void getDailyReminder_NotificationNotFound() {
    // given
    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.empty());

    // when & then
    NotificationException notificationException = assertThrows(
        NotificationException.class,
        () -> notificationSettingService.getDailyReminder(userId));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION, notificationException.getErrorCode());
  }

  @Test
  @DisplayName("daily reminder 조회: NotificationRecipient 엔티티 부존재 시 예외처리")
  void getDailyReminder_NotificationRecipientNotFound() {
    // given
    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.empty());

    // when & then
    NotificationException notificationException = assertThrows(
        NotificationException.class,
        () -> notificationSettingService.getDailyReminder(userId));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION_RECIPIENT,
        notificationException.getErrorCode());
  }

  @Test
  @DisplayName("daily reminder 조회: NotificationSchedule 엔티티 부존재 시 예외처리")
  void getDailyReminder_NotificationScheduleNotFound() {
    // given
    given(notificationRepository.findByTopic(NotificationTopic.DAILY_REMINDER))
        .willReturn(Optional.of(notification));
    given(notificationRecipientRepository.findByUserIdAndNotificationId(userId,
        notification.getId()))
        .willReturn(Optional.of(recipient));
    given(notificationScheduleRepository.findByRecipient(recipient))
        .willReturn(Optional.empty());

    // when & then
    NotificationException notificationException = assertThrows(
        NotificationException.class,
        () -> notificationSettingService.getDailyReminder(userId));

    assertEquals(ErrorCode.NOT_FOUND_NOTIFICATION_SCHEDULE,
        notificationException.getErrorCode());
  }


}