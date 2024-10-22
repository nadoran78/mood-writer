package com.example.moodwriter.global.openAI.exception;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class OpenAIException extends CustomException {

  public OpenAIException(ErrorCode errorCode) {
    super(errorCode);
  }
}
