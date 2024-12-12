package com.example.moodwriter.domain.fcm.dto;

import com.example.moodwriter.domain.fcm.entity.FcmToken;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTokenResponse {

  private UUID fcmTokenId;
  private String deviceId;
  private String fcmToken;
  private String deviceType;
  private boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static FcmTokenResponse from(FcmToken fcmToken) {
    return FcmTokenResponse.builder()
        .fcmTokenId(fcmToken.getId())
        .deviceId(fcmToken.getDeviceId())
        .fcmToken(fcmToken.getFcmToken())
        .deviceType(fcmToken.getDeviceType())
        .isActive(fcmToken.isActive())
        .createdAt(fcmToken.getCreatedAt())
        .updatedAt(fcmToken.getUpdatedAt())
        .build();
  }
}
