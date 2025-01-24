package com.example.moodwriter.domain.fcm.service;

import com.example.moodwriter.domain.fcm.dao.FcmTokenRepository;
import com.example.moodwriter.domain.fcm.dto.FcmTokenRequest;
import com.example.moodwriter.domain.fcm.dto.FcmTokenResponse;
import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;

  private final EntityManager entityManager;

  @Transactional
  public FcmTokenResponse saveFcmToken(FcmTokenRequest request, UUID userId) {
    FcmToken existingToken = fcmTokenRepository.findByDeviceIdAndUserId(
        request.getDeviceId(), userId).orElse(null);

    if (existingToken != null) {
      if (existingToken.getFcmToken().equals(request.getFcmToken())) {
        return FcmTokenResponse.from(existingToken);
      }

      existingToken.update(request);
      FcmToken updatedToken = fcmTokenRepository.save(existingToken);
      return FcmTokenResponse.from(updatedToken);
    }

    User userProxy = entityManager.getReference(User.class, userId);

    FcmToken newFcmToken = FcmToken.from(request, userProxy);
    FcmToken savedFcmToken = fcmTokenRepository.save(newFcmToken);

    return FcmTokenResponse.from(savedFcmToken);
  }
}
