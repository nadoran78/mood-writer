package com.example.moodwriter.domain.notification.service;

import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQNotificationSender implements NotificationSender {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void sendBySchedule(NotificationScheduleDto schedule) {
    log.info("Sending notification via RabbitMQ for schedule: {}", schedule.getId());

    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
        RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
        schedule);

    log.info("Notification sent to RabbitMQ: {}", schedule);
  }
}
