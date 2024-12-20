package com.example.moodwriter.domain.fcm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.dto.FcmTokenRequest;
import com.example.moodwriter.domain.fcm.dto.FcmTokenResponse;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.exception.FcmTokenException;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.exception.code.ErrorCode;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {
  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private FcmTokenService fcmTokenService;

  @Test
  void successSaveFcmToken_whenNoExistingToken_saveNewToken() {
    // given
    UUID userId = UUID.randomUUID();
    FcmTokenRequest request = FcmTokenRequest.builder()
        .deviceId("device123")
        .fcmToken("fcm_token_new")
        .deviceType("ANDROID")
        .build();

    User userProxy = mock(User.class);

    given(fcmTokenRepository.findByDeviceIdAndUserId(request.getDeviceId(), userId))
        .willReturn(Optional.empty());
    given(entityManager.getReference(User.class, userId))
        .willReturn(userProxy);
    given(fcmTokenRepository.save(any(FcmToken.class))).will(returnsFirstArg());

    // when
    FcmTokenResponse response = fcmTokenService.saveFcmToken(request, userId);

    // then
    assertEquals(request.getDeviceId(), response.getDeviceId());
    assertEquals(request.getFcmToken(), response.getFcmToken());
    assertEquals(request.getDeviceType(), response.getDeviceType());
    assertTrue(response.isActive());
    verify(fcmTokenRepository, times(1)).save(any(FcmToken.class));
  }

  @Test
  void successSaveFcmToken_whenExistingTokenIsDifferent_DeactivateAndSaveNewToken() {
    // given
    UUID userId = UUID.randomUUID();
    FcmTokenRequest request = FcmTokenRequest.builder()
        .deviceId("device123")
        .fcmToken("fcm_token_new")
        .deviceType("ANDROID")
        .build();

    FcmToken existingToken = FcmToken.builder()
        .deviceId(request.getDeviceId())
        .fcmToken("fcm_token_old")
        .deviceType(request.getDeviceType())
        .isActive(true)
        .build();

    User userProxy = mock(User.class);

    given(fcmTokenRepository.findByDeviceIdAndUserId(request.getDeviceId(), userId))
        .willReturn(Optional.of(existingToken));
    given(entityManager.getReference(User.class, userId))
        .willReturn(userProxy);
    given(fcmTokenRepository.save(any(FcmToken.class))).will(returnsFirstArg());

    // when
    FcmTokenResponse response = fcmTokenService.saveFcmToken(request, userId);

    // then
    assertEquals(request.getDeviceId(), response.getDeviceId());
    assertEquals(request.getFcmToken(), response.getFcmToken());
    assertEquals(request.getDeviceType(), response.getDeviceType());
    assertTrue(response.isActive());
    assertFalse(existingToken.isActive());
    verify(fcmTokenRepository, times(2)).save(any(FcmToken.class));
  }

  @Test
  void saveFcmToken_WhenExistingTokenIsSame_ThrowFcmTokenException() {
    // given
    UUID userId = UUID.randomUUID();
    FcmTokenRequest request = FcmTokenRequest.builder()
        .deviceId("device123")
        .fcmToken("fcm_token_new")
        .deviceType("ANDROID")
        .build();

    FcmToken existingToken = FcmToken.builder()
        .deviceId(request.getDeviceId())
        .fcmToken(request.getFcmToken())
        .deviceType(request.getDeviceType())
        .isActive(true)
        .build();

    given(fcmTokenRepository.findByDeviceIdAndUserId(request.getDeviceId(), userId))
        .willReturn(Optional.of(existingToken));

    // when & then
    FcmTokenException fcmTokenException = assertThrows(FcmTokenException.class,
        () -> fcmTokenService.saveFcmToken(request, userId));

    assertEquals(ErrorCode.FCM_TOKEN_ALREADY_EXISTS, fcmTokenException.getErrorCode());
  }


}