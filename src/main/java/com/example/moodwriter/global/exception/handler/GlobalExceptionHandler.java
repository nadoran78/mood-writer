package com.example.moodwriter.global.exception.handler;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.exception.response.ErrorResponse;
import com.fasterxml.jackson.core.io.JsonEOFException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e,
      HttpServletRequest request) {
    log.error("[CustomException] {} is occurred. uri : {}", e.getErrorCode(),
        request.getRequestURI());

    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ErrorResponse.of(e.getErrorCode(), request.getRequestURI()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.error("MethodArgumentNotValidException is occurred. uri : {}",
        request.getRequestURI());

    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_ERROR,
        request.getRequestURI());
    errorResponse.addFieldErrors(e.getFieldErrors());

    return ResponseEntity
        .status(e.getStatusCode())
        .body(errorResponse);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException e, HttpServletRequest request) {
    log.error("HandlerMethodValidationException is occurred. uri : {}",
        request.getRequestURI());

    List<ParameterValidationResult> parameterValidationResults = e.getAllValidationResults();

    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_ERROR,
        request.getRequestURI());
    errorResponse.addParameterValidationErrors(parameterValidationResults);

    return ResponseEntity
        .status(e.getStatusCode())
        .body(errorResponse);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException e, HttpServletRequest request) {
    log.error("ConstraintViolationException[{}] is occurred. uri : {}",
        e.getMessage(), request.getRequestURI());

    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_ERROR,
        request.getRequestURI());
    errorResponse.addConstraintViolations(e.getConstraintViolations());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errorResponse);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e, HttpServletRequest request) {
    log.error("HttpMessageNotReadableException is occurred. uri : {}",
        request.getRequestURI());

    if (e.getCause() instanceof JsonEOFException) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.of(ErrorCode.JSON_EOF_ERROR, request.getRequestURI()));
    }

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(ErrorCode.HTTP_MESSAGE_NOT_READABLE,
            request.getRequestURI()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e, HttpServletRequest request) {
    log.error("MethodArgumentTypeMismatchException[{}] is occurred. uri : {}",
        e.getMessage(), request.getRequestURI());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCHED,
            request.getRequestURI()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(
      RuntimeException e, HttpServletRequest request) {
    log.error("RuntimeException[{}] is occurred. uri : {}", e.getMessage(),
        request.getRequestURI());

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, request.getRequestURI()));
  }

}
