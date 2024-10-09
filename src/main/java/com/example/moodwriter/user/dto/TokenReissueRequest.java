package com.example.moodwriter.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenReissueRequest {

  @NotBlank(message = "리프레쉬 토큰을 필수값입니다.")
  private String refreshToken;
}
