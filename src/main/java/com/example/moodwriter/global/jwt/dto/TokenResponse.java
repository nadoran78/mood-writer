package com.example.moodwriter.global.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TokenResponse {
  private String email;
  private String accessToken;
  private String refreshToken;
}
