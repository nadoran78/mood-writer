package com.example.moodwriter.domain.diary.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class DiaryException extends CustomException {

  public DiaryException(ErrorCode errorCode) {
    super(errorCode);
  }
}
