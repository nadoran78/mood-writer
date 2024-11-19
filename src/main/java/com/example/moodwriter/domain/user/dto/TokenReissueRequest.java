package com.example.moodwriter.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueRequest {

  @NotBlank(message = "액세스 토큰을 입력해주세요.")
  private String accessToken;

  @NotBlank(message = "리프레쉬 토큰을 입력해주세요.")
  private String refreshToken;
}
