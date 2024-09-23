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
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
  ALREADY_REGISTERED_USER(HttpStatus.BAD_REQUEST, "이미 가입한 회원입니다."),
  FAIL_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
  JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 데이터 변환에 실패했습니다."),
  INVALID_TOKEN(HttpStatus.FORBIDDEN, "토큰 정보가 유효하지 않습니다."),
  NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "저장된 리프레쉬 토큰이 없습니다."),
  UNMATCHED_SAVED_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "저장된 리프레쉬 토큰과 일치하지 않습니다."),

  ;

  private final HttpStatus httpStatus;
  private final String message;
}
