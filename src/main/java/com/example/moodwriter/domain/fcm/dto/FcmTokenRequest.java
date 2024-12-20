package com.example.moodwriter.domain.fcm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmTokenRequest {

  @NotBlank(message = "device id를 입력해주세요.")
  private String deviceId;

  @NotBlank(message = "fcm token을 입력해주세요.")
  private String fcmToken;

  @NotBlank(message = "device type을 입력해주세요.")
  private String deviceType;

}
