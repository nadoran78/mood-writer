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
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당하는 회원이 존재하지 않습니다."),
  INCORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
  ALREADY_DEACTIVATED_USER(HttpStatus.FORBIDDEN, "이미 탈퇴한 회원입니다."),

  FAIL_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
  JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 데이터 변환에 실패했습니다."),

  INVALID_TOKEN(HttpStatus.FORBIDDEN, "토큰 정보가 유효하지 않습니다."),
  NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "저장된 리프레쉬 토큰이 없습니다."),
  UNMATCHED_SAVED_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "저장된 리프레쉬 토큰과 일치하지 않습니다."),
  NEED_TO_SIGN_IN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

  NOT_FOUND_DIARY(HttpStatus.NOT_FOUND, "해당하는 일기가 존재하지 않습니다."),
  NOT_FOUND_DIARY_MEDIA(HttpStatus.NOT_FOUND, "해당하는 파일이 존재하지 않습니다."),
  FORBIDDEN_DELETE_MEDIA(HttpStatus.FORBIDDEN, "해당 미디어 파일을 삭제할 권한이 없습니다."),
  FORBIDDEN_ACCESS_DIARY(HttpStatus.FORBIDDEN, "일기에 접근할 권한이 없습니다."),
  CONFLICT_DIARY_MEDIA(HttpStatus.CONFLICT, "삭제하려는 파일이 연결된 일기와 현재 작업 중인 일기가 일치하지 않습니다."),
  ALREADY_DELETED_DIARY(HttpStatus.FORBIDDEN, "삭제된 일기입니다."),
  CONFLICT_DIARY_STATE(HttpStatus.CONFLICT, "일기가 유효한 상태가 아닙니다."),

  OPEN_AI_RETURN_UNEXPECTED_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "Open AI API 호출 결과 예상치 못 한 응답코드가 반환되었습니다.")

  ;

  private final HttpStatus httpStatus;
  private final String message;
}
