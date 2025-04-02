package com.example.moodwriter.global.exception;

import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class TokenException extends AuthenticationException {
  private final ErrorCode errorCode;

  public TokenException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
