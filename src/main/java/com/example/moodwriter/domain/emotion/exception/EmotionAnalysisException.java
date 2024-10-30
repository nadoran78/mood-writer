package com.example.moodwriter.domain.emotion.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class EmotionAnalysisException extends CustomException {

  public EmotionAnalysisException(
      ErrorCode errorCode) {
    super(errorCode);
  }
}
