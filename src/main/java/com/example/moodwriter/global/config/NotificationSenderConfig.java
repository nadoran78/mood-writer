package com.example.moodwriter.global.config;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.service.FcmService;
import com.example.moodwriter.domain.notification.dao.NotificationRecipientRepository;
import com.example.moodwriter.domain.notification.service.FcmNotificationSender;
import com.example.moodwriter.domain.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationSenderConfig {

  private final FcmService fcmService;
  private final FcmTokenRepository fcmTokenRepository;
  private final NotificationRecipientRepository notificationRecipientRepository;

  @Bean
  public NotificationSender notificationSender() {
    return new FcmNotificationSender(fcmService, fcmTokenRepository,
        notificationRecipientRepository);
  }

}
