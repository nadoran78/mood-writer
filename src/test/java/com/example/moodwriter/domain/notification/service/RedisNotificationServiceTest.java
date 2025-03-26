package com.example.moodwriter.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class RedisNotificationServiceTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ZSetOperations<String, String> zSetOperations;

  @InjectMocks
  private RedisNotificationService redisNotificationService;

  private final String REDIS_NOTIFICATION_KEY = "scheduled_notifications";

  @BeforeEach
  void setUp() {
    given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
  }

  @Test
  void scheduleNotification_shouldAddToRedis() {
    // Arrange
    NotificationSchedule schedule = spy(NotificationSchedule.builder()
        .scheduledTime(LocalTime.of(8, 30))
        .build());
    UUID scheduleId = UUID.randomUUID();

    double expectedScore = schedule.getScheduledTime().toSecondOfDay();

    // Act
    redisNotificationService.scheduleNotification(schedule.getScheduledTime(), scheduleId);

    // Assert
    verify(zSetOperations, times(1))
        .add(REDIS_NOTIFICATION_KEY, scheduleId.toString(), expectedScore);
  }

  @Test
  void getNotificationsToSend_shouldReturnIds() {
    // Arrange
    LocalTime currentTime = LocalTime.of(9, 0);
    double currentScore = currentTime.plusMinutes(30).toSecondOfDay();
    double previousScore = currentTime.minusMinutes(30).toSecondOfDay();

    Set<String> mockIds = Set.of("id1", "id2");
    given(zSetOperations.rangeByScore(REDIS_NOTIFICATION_KEY, previousScore,
        currentScore)).willReturn(mockIds);

    // Act
    Set<String> result = redisNotificationService.getNotificationsToSend(currentTime);

    // Assert
    assertEquals(mockIds, result);
    verify(zSetOperations, times(1)).rangeByScore(REDIS_NOTIFICATION_KEY, previousScore,
        currentScore);
  }

  @Test
  void removeSentNotification_shouldRemoveFromRedis() {
    // Arrange
    UUID notificationScheduleId = UUID.randomUUID();

    // Act
    redisNotificationService.removeScheduledNotification(notificationScheduleId);

    // Assert
    verify(zSetOperations, times(1)).remove(REDIS_NOTIFICATION_KEY,
        notificationScheduleId.toString());
  }
}