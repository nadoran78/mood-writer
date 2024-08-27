package com.example.moodwriter.global.exception;

import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

  private final ErrorCode errorCode;
  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
