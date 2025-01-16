package com.example.moodwriter.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RabbitMQConstants {

  NOTIFICATION_QUEUE("notificationQueue"),
  NOTIFICATION_EXCHANGE("notificationExchange"),
  NOTIFICATION_ROUTING_KEY("notificationRoutingKey");

  private final String value;
}
