package com.example.moodwriter.user.dto;

import com.example.moodwriter.global.constant.RegexPattern;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegisterRequest {

  @Pattern(regexp = RegexPattern.EMAIL, message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @Pattern(regexp = RegexPattern.PASSWORD, message = "암호는 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하여야 합니다.")
  private String password;
  private String name;
}
