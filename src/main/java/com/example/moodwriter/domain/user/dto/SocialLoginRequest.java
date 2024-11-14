package com.example.moodwriter.domain.user.dto;

import com.example.moodwriter.global.constant.RegexPattern;
import com.example.moodwriter.global.constant.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginRequest {

  @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
  @Pattern(regexp = RegexPattern.EMAIL, message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotNull(message = "이름을 입력하여 주세요.")
  private String name;

  @NotNull(message = "어떤 소셜네트워크서비스를 통해 로그인하였는지 입력하여 주세요.")
  private SocialProvider socialProvider;

}
