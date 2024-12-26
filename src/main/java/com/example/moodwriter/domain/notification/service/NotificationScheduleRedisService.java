package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationScheduleRedisService {

  private final String REDIS_NOTIFICATION_KEY = "scheduled_notifications";
  private final RedisTemplate<String, String> redisTemplate;

  public void scheduleNotification(NotificationSchedule schedule) {

  }



}
