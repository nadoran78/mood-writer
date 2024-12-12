package com.example.moodwriter.domain.fcm.service;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.dto.FcmTokenRequest;
import com.example.moodwriter.domain.fcm.dto.FcmTokenResponse;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.fcm.exception.FcmTokenException;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.exception.code.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;
  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  public FcmTokenResponse saveFcmToken(FcmTokenRequest request, UUID userId) {
    FcmToken existingToken = fcmTokenRepository.findByDeviceIdAndUserId(
        request.getDeviceId(), userId).orElse(null);

    if (existingToken != null) {
      if (existingToken.getFcmToken().equals(request.getFcmToken())) {
        throw new FcmTokenException(ErrorCode.FCM_TOKEN_ALREADY_EXISTS);
      }

      existingToken.deactivate();
      fcmTokenRepository.save(existingToken);
    }

    // Todo User를 직접 조회할 때와 비교해서 test
    User userProxy = entityManager.getReference(User.class, userId);

    FcmToken newFcmToken = FcmToken.from(request, userProxy);
    FcmToken savedFcmToken = fcmTokenRepository.save(newFcmToken);

    return FcmTokenResponse.from(savedFcmToken);
  }
}
