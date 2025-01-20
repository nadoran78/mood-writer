package com.example.moodwriter.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.moodwriter.domain.notification.dao.NotificationScheduleRepository;
import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    UUID recipient1Id = UUID.randomUUID();
    UUID recipient2Id = UUID.randomUUID();
    UUID schedule1Id = UUID.randomUUID();
    UUID schedule2Id = UUID.randomUUID();
    NotificationRecipient recipient1 = mock(NotificationRecipient.class);
    NotificationRecipient recipient2 = mock(NotificationRecipient.class);
    NotificationSchedule schedule1 = spy(NotificationSchedule.builder()
        .recipient(recipient1)
        .scheduledTime(LocalTime.of(10, 0))
        .build());
    NotificationSchedule schedule2 = spy(NotificationSchedule.builder()
        .recipient(recipient2)
        .scheduledTime(LocalTime.of(11, 0))
        .build());

    Set<String> notificationIds = Set.of(notification1Id.toString(),
        notification2Id.toString());

    given(recipient1.getId()).willReturn(recipient1Id);
    given(recipient2.getId()).willReturn(recipient2Id);
    given(schedule1.getId()).willReturn(schedule1Id);
    given(schedule2.getId()).willReturn(schedule2Id);
    given(
        redisNotificationService.getNotificationsToSend(any(LocalTime.class))).willReturn(
        notificationIds);
    given(notificationScheduleRepository.findById(eq(notification1Id))).willReturn(
        Optional.of(schedule1));
    given(notificationScheduleRepository.findById(eq(notification2Id))).willReturn(
        Optional.of(schedule2));

    // Act
    notificationScheduler.processNotifications();

    // Assert
    verify(redisNotificationService, times(1)).getNotificationsToSend(
        any(LocalTime.class));
    verify(notificationScheduleRepository, times(1))
        .findById(notification1Id);
    verify(notificationScheduleRepository, times(1))
        .findById(notification2Id);

    ArgumentCaptor<NotificationScheduleDto> captor = ArgumentCaptor.forClass(NotificationScheduleDto.class);
    verify(notificationSender, times(2)).sendBySchedule(captor.capture());

    List<NotificationScheduleDto> capturedDtos = captor.getAllValues();
    assertEquals(2, capturedDtos.size());

    NotificationScheduleDto scheduleDto1 = capturedDtos.get(0);
    NotificationScheduleDto scheduleDto2 = capturedDtos.get(1);
    assertEquals(scheduleDto1.getRecipientId(), recipient1Id);
    assertEquals(scheduleDto2.getRecipientId(), recipient2Id);
    assertEquals(scheduleDto1.getId(), schedule1.getId());
    assertEquals(scheduleDto2.getId(), schedule2.getId());

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