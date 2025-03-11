package com.example.moodwriter.global.exception;

import com.example.moodwriter.global.exception.code.ErrorCode;

public class LambdaException extends CustomException{

  public LambdaException(ErrorCode errorCode) {
    super(errorCode);
  }
}
