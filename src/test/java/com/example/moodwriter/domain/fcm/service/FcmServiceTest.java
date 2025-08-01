package com.example.moodwriter.domain.fcm.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.fcm.exception.FcmException;
import com.example.moodwriter.global.config.FirebaseConfig;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

  @Mock
  private FirebaseMessaging firebaseMessaging;

  @Mock
  private FirebaseConfig firebaseConfig;

  @InjectMocks
  private FcmService fcmService;

  private MockedStatic<FirebaseMessaging> mockedFirebaseMessaging;

  @BeforeEach
  public void setUp() {
    mockedFirebaseMessaging = mockStatic(FirebaseMessaging.class);
    mockedFirebaseMessaging.when(FirebaseMessaging::getInstance)
        .thenReturn(firebaseMessaging);
  }

  @AfterEach
  public void tearDown() {
    mockedFirebaseMessaging.close();
  }

  @Test
  void successSendNotificationByToken() throws FirebaseMessagingException {
    // Given
    String targetToken = "testToken";
    String title = "Test Title";
    String body = "Test Body";
    Map<String, String> data = Map.of("key1", "value1", "key2", "value2");
    String mockResponse = "mockResponse";

    given(firebaseMessaging.send(any(Message.class))).willReturn(mockResponse);

    // When
    String response = fcmService.sendNotificationByToken(targetToken, title, body, data);

    // Then
    assertEquals("Notification sent successfully: " + mockResponse, response);
    verify(firebaseMessaging, times(1)).send(any(Message.class));
  }

  @Test
  void failSendNotificationByToken() throws FirebaseMessagingException {
    // Given
    String targetToken = "testToken";
    String title = "Test Title";
    String body = "Test Body";
    Map<String, String> data = Map.of("key1", "value1", "key2", "value2");

    given(firebaseMessaging.send(any(Message.class))).willThrow(
        mock(FirebaseMessagingException.class));

    // When & Then
    FcmException exception = assertThrows(FcmException.class, () ->
        fcmService.sendNotificationByToken(targetToken, title, body, data));

    assertEquals(ErrorCode.FAIL_TO_SEND_FCM_MESSAGE, exception.getErrorCode());
    verify(firebaseMessaging, times(1)).send(any(Message.class));
  }
}