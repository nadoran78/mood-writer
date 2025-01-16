package com.example.moodwriter.domain.notification.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataRedisTest
class RedisNotificationServiceContainerTest {

  @Container
  static GenericContainer<?> redisContainer = new GenericContainer<>(
      "redis:7.0.5").withExposedPorts(6379);

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private RedisNotificationService redisNotificationService;

  @DynamicPropertySource
  static void registerRedisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redisContainer::getHost);
    registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
  }

  @BeforeEach
  void setUp() {
    redisTemplate.getConnectionFactory().getConnection().flushAll();
    redisNotificationService = new RedisNotificationService(redisTemplate);
  }

  @Test
  void scheduleNotification_shouldAddToRedis() {
    // Given
    UUID notificationScheduleId = UUID.randomUUID();
    LocalTime scheduledTime = LocalTime.of(10, 0);
    NotificationSchedule schedule = spy(NotificationSchedule.builder()
        .scheduledTime(scheduledTime)
        .build());

    given(schedule.getId()).willReturn(notificationScheduleId);

    // When
    redisNotificationService.scheduleNotification(schedule);

    // Then
    Set<String> scheduledNotifications = redisTemplate.opsForZSet()
        .range("scheduled_notifications", 0, -1);

    assertThat(scheduledNotifications).contains(notificationScheduleId.toString());
  }

  @Test
  void getNotificationsToSend_shouldReturnCorrectIds() {
    // Given
    UUID notificationId1 = UUID.randomUUID();
    UUID notificationId2 = UUID.randomUUID();

    redisTemplate.opsForZSet().add("scheduled_notifications", notificationId1.toString(),
        LocalTime.of(9, 0).toSecondOfDay());
    redisTemplate.opsForZSet().add("scheduled_notifications", notificationId2.toString(),
        LocalTime.of(10, 0).toSecondOfDay());

    // When
    Set<String> notificationsToSend = redisNotificationService.getNotificationsToSend(
        LocalTime.of(10, 0));

    // Then
    assertEquals(1, notificationsToSend.size());
    assertTrue(notificationsToSend.contains(notificationId2.toString()));
  }

  @Test
  void removeSentNotification_shouldRemoveFromRedis() {
    // Given
    UUID notificationId = UUID.randomUUID();
    redisTemplate.opsForZSet().add("scheduled_notifications", notificationId.toString(),
        LocalTime.of(10, 0).toSecondOfDay());

    // When
    redisNotificationService.removeSentNotification(notificationId);

    // Then
    Set<String> scheduledNotifications = redisTemplate.opsForZSet()
        .range("scheduled_notifications", 0, -1);

    assertThat(scheduledNotifications).doesNotContain(notificationId.toString());
  }
}