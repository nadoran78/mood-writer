package com.example.moodwriter.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.moodwriter.domain.notification.dao.NotificationScheduleRepository;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

  @Mock
  private RedisNotificationService redisNotificationService;

  @Mock
  private NotificationSender notificationSender;

  @Mock
  private NotificationScheduleRepository notificationScheduleRepository;

  @InjectMocks
  private NotificationScheduler notificationScheduler;

  @Test
  void processNotifications_shouldProcessNotifications() {
    // Arrange
    UUID notification1Id = UUID.randomUUID();
    UUID notification2Id = UUID.randomUUID();
    NotificationSchedule schedule1 = mock(NotificationSchedule.class);
    NotificationSchedule schedule2 = mock(NotificationSchedule.class);
    Set<String> notificationIds = Set.of(notification1Id.toString(),
        notification2Id.toString());

    given(
        redisNotificationService.getNotificationsToSend(any(LocalTime.class))).willReturn(
        notificationIds);
    given(notificationScheduleRepository.findById(notification1Id)).willReturn(
        Optional.of(schedule1));
    given(notificationScheduleRepository.findById(notification2Id)).willReturn(
        Optional.of(schedule2));

    // Act
    notificationScheduler.processNotifications();

    // Assert
    verify(redisNotificationService, times(1)).getNotificationsToSend(
        any(LocalTime.class));
    verify(notificationScheduleRepository, times(1)).findById(notification1Id);
    verify(notificationScheduleRepository, times(1)).findById(notification2Id);

    verify(notificationSender).sendBySchedule(schedule1);
    verify(notificationSender).sendBySchedule(schedule2);

    verifyNoMoreInteractions(redisNotificationService, notificationScheduleRepository,
        notificationSender);
  }

  @Test
  void processNotifications_shouldHandleEmptyNotificationIds() {
    // Arrange
    given(
        redisNotificationService.getNotificationsToSend(any(LocalTime.class))).willReturn(
        Set.of());

    // Act
    notificationScheduler.processNotifications();

    // Assert
    verify(redisNotificationService, times(1)).getNotificationsToSend(
        any(LocalTime.class));
    verifyNoMoreInteractions(redisNotificationService);
    verifyNoInteractions(notificationSender, notificationScheduleRepository);
  }


}