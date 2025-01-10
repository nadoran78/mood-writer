package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
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
   *
   * @param schedule 알림 스케줄 객체
   */
  public void scheduleNotification(NotificationSchedule schedule) {
    double score = schedule.getScheduledTime().toSecondOfDay();
    redisTemplate.opsForZSet()
        .add(REDIS_NOTIFICATION_KEY, schedule.getId().toString(), score);
  }

  /**
   * 현재 시간 이전에 예약된 알림 ID 목록 조회
   *
   * @param currentTime 현재 시간
   * @return 알림 ID 목록
   */
  public Set<String> getNotificationsToSend(LocalTime currentTime) {
    double currentScore = currentTime.toSecondOfDay();
    double previousScore = currentTime.minusHours(1).plusMinutes(1).toSecondOfDay();
    return redisTemplate.opsForZSet()
        .rangeByScore(REDIS_NOTIFICATION_KEY, previousScore, currentScore);
  }

  /**
   * 알림 ID를 Redis에서 제거
   *
   * @param notificationScheduleId 알림 ID
   */
  public void removeSentNotification(UUID notificationScheduleId) {
    redisTemplate.opsForZSet().remove(REDIS_NOTIFICATION_KEY, notificationScheduleId.toString());
  }


}
