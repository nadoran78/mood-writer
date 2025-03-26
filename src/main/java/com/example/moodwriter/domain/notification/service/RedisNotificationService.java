package com.example.moodwriter.domain.notification.service;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisNotificationService {

  private final String REDIS_NOTIFICATION_KEY = "scheduled_notifications";
  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 알림 스케줄을 Redis Sorted Set에 추가
   */
  public void scheduleNotification(LocalTime scheduledTime, UUID notificationScheduleId) {
    double score = scheduledTime.toSecondOfDay();
    redisTemplate.opsForZSet()
        .add(REDIS_NOTIFICATION_KEY, notificationScheduleId.toString(), score);
  }

  /**
   * 현재 시간 이전에 예약된 알림 ID 목록 조회
   *
   * @param currentTime 현재 시간
   * @return 알림 ID 목록
   */
  public Set<String> getNotificationsToSend(LocalTime currentTime) {
    double currentScore = currentTime.plusMinutes(30).toSecondOfDay();
    double previousScore = currentTime.minusMinutes(30).toSecondOfDay();
    return redisTemplate.opsForZSet()
        .rangeByScore(REDIS_NOTIFICATION_KEY, previousScore, currentScore);
  }

  /**
   * 알림 ID를 Redis에서 제거
   *
   * @param notificationScheduleId 알림 ID
   */
  public void removeScheduledNotification(UUID notificationScheduleId) {
    redisTemplate.opsForZSet()
        .remove(REDIS_NOTIFICATION_KEY, notificationScheduleId.toString());
  }


}
