package com.example.moodwriter.user.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends CustomException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }
}
