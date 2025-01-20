package com.example.moodwriter.global.config;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.service.FcmNotificationSender;
import com.example.moodwriter.domain.notification.service.NotificationSender;
import com.example.moodwriter.domain.notification.service.RabbitMQNotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NotificationSenderConfig {

  @Value("${notification.sender}")
  private String sender;

  @Bean
  public NotificationSender notificationSender(FcmService fcmService,
      FcmTokenRepository fcmTokenRepository,
      NotificationRecipientRepository notificationRecipientRepository,
      RabbitTemplate rabbitTemplate) {
    if (sender.equals("rabbitmq")) {
      log.info("notification sender is ready to work by rabbitmq sender");
      return new RabbitMQNotificationSender(rabbitTemplate);
    } else {
      log.info("notification sender is ready to work by fcm sender");
      return new FcmNotificationSender(fcmService, fcmTokenRepository, notificationRecipientRepository);
    }
  }
//
//  @Bean
//  @ConditionalOnProperty(name = "spring.fcm.enabled", havingValue = "true", matchIfMissing = true)
//  public NotificationSender fcmNotificationSender() {
//    return new FcmNotificationSender(fcmService, fcmTokenRepository,
//        notificationRecipientRepository);
//  }
//
//  @Bean
//  public NotificationSender notificationSender(RabbitTemplate rabbitTemplate) {
//    return new RabbitMQNotificationSender(rabbitTemplate);
//  }

}
