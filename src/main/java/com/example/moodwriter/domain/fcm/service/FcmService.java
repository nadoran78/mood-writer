package com.example.moodwriter.domain.fcm.service;

import com.example.moodwriter.domain.fcm.exception.FcmException;
import com.example.moodwriter.global.config.FirebaseConfig;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmService {

  private final FirebaseConfig firebaseConfig;

  public String sendNotificationByToken(String targetToken, String title, String body,
      Map<String, String> data) {
    firebaseConfig.initializeFirebaseApp();

    try {
      Message message = Message.builder()
          .setToken(targetToken)
          .setNotification(Notification.builder()
              .setTitle(title)
              .setBody(body)
              .build())
          .putAllData(data)
          .build();

      String response = FirebaseMessaging.getInstance().send(message);

      return "Notification sent successfully: " + response;
    } catch (FirebaseMessagingException e) {
      throw new FcmException(ErrorCode.FAIL_TO_SEND_FCM_MESSAGE);
    }
  }
}
