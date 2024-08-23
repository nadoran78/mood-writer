package com.example.moodwriter.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  JSON_EOF_ERROR(HttpStatus.BAD_REQUEST, "잘못된 JSON 데이터입니다."),
  HTTP_MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "HTTP 메시지를 읽을 수 없습니다."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "처리되지 않은 에러가 발생하였습니다."),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다.")

  ;

  private final HttpStatus httpStatus;
  private final String message;
}
