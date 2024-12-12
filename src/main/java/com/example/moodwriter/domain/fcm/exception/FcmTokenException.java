package com.example.moodwriter.domain.fcm.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;

public class FcmTokenException extends CustomException {

  public FcmTokenException(
      ErrorCode errorCode) {
    super(errorCode);
  }
}
