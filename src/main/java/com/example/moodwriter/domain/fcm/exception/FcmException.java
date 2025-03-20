package com.example.moodwriter.domain.fcm.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;

public class FcmException extends CustomException {

  public FcmException(
      ErrorCode errorCode) {
    super(errorCode);
  }
}
