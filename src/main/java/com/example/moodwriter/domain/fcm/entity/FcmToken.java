package com.example.moodwriter.domain.fcm.entity;

import com.example.moodwriter.domain.fcm.dto.FcmTokenRequest;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Table(name = "fcm_token")
public class FcmToken extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "device_id", nullable = false, length = 255)
  private String deviceId;

  @Column(name = "fcm_token", nullable = false, columnDefinition = "TEXT")
  private String fcmToken;

  @Column(name = "device_type", nullable = false, length = 50)
  private String deviceType;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "last_used_at", columnDefinition = "DATETIME")
  private LocalDateTime lastUsedAt;

  @Builder
  public FcmToken(User user, String deviceId, String fcmToken, String deviceType,
      boolean isActive, LocalDateTime lastUsedAt) {
    this.user = user;
    this.deviceId = deviceId;
    this.fcmToken = fcmToken;
    this.deviceType = deviceType;
    this.isActive = isActive;
    this.lastUsedAt = lastUsedAt;
  }

  public void deactivate() {
    this.isActive = false;
  }

  public static FcmToken from(FcmTokenRequest request, User user) {
    return FcmToken.builder()
        .user(user)
        .deviceId(request.getDeviceId())
        .fcmToken(request.getFcmToken())
        .deviceType(request.getDeviceType())
        .isActive(true)
        .build();
  }

  public void update(FcmTokenRequest request) {
    this.isActive = true;
    this.deviceId = request.getDeviceId();
    this.fcmToken = request.getFcmToken();
    this.deviceType = request.getDeviceType();
  }
}
