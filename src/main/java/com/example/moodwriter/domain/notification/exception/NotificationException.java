package com.example.moodwriter.domain.notification.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class NotificationException extends CustomException {

  public NotificationException(
      ErrorCode errorCode) {
    super(errorCode);
  }
}
