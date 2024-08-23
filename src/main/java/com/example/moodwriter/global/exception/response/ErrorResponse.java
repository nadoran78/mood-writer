package com.example.moodwriter.global.exception.response;

import com.example.moodwriter.global.exception.code.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {

  private ErrorCode errorCode;
  private String message;
  private String path;
  private List<FieldValidationError> fieldErrors;
  private List<ParameterValidationError> parameterErrors;

  public static ErrorResponse of(ErrorCode errorCode, String path) {
    return ErrorResponse.builder()
        .errorCode(errorCode)
        .message(errorCode.getMessage())
        .path(path)
        .build();
  }

  public static ErrorResponse of(String message, String path) {
    return ErrorResponse.builder()
        .message(message)
        .path(path)
        .build();
  }

  public void addFieldErrors(List<FieldError> fieldErrors) {
    this.fieldErrors = new ArrayList<>();
    for (FieldError fieldError : fieldErrors) {
      this.fieldErrors.add(new FieldValidationError(
          fieldError.getField(),
          fieldError.getRejectedValue(),
          fieldError.getDefaultMessage()
      ));
    }
  }

  public void addParameterValidationErrors(
      List<ParameterValidationResult> parameterErrors) {
    this.parameterErrors = new ArrayList<>();
    for (ParameterValidationResult error : parameterErrors) {
      this.parameterErrors.add(new ParameterValidationError(
          error.getMethodParameter().getParameterName(),
          error.getArgument(),
          error.getResolvableErrors().stream()
              .map(MessageSourceResolvable::getDefaultMessage).collect(Collectors.toList())
      ));
    }
  }

  @JsonInclude(Include.NON_NULL)
  @RequiredArgsConstructor
  @Getter
  private static class ParameterValidationError {

    private final String parameter;
    private final Object value;
    private final List<String> messages;
  }

  @JsonInclude(Include.NON_NULL)
  @RequiredArgsConstructor
  @Getter
  private static class FieldValidationError {

    private final String field;
    private final Object rejectedValue;
    private final String message;
  }


}
