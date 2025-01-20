package com.example.moodwriter.domain.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.notification.dto.NotificationScheduleDto;
import com.example.moodwriter.global.config.RabbitMQConfig;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class RabbitMQNotificationSenderTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private RabbitMQNotificationSender notificationSender;

  @Test
  void success_sendBySchedule() {
    // given
    NotificationScheduleDto schedule = NotificationScheduleDto.builder()
        .id(UUID.randomUUID())
        .recipientId(UUID.randomUUID())
        .scheduledTime(LocalTime.of(10, 0))
        .build();

    // when
    notificationSender.sendBySchedule(schedule);

    // then
    ArgumentCaptor<NotificationScheduleDto> captor = ArgumentCaptor.forClass(
        NotificationScheduleDto.class);

    verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
        eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY), captor.capture());

    NotificationScheduleDto capturedSchedule = captor.getValue();

    assertEquals(schedule.getId(), capturedSchedule.getId());
    assertEquals(schedule.getRecipientId(), capturedSchedule.getRecipientId());
    assertEquals(schedule.getScheduledTime(), capturedSchedule.getScheduledTime());
  }

}