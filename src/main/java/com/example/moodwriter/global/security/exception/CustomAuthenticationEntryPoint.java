package com.example.moodwriter.global.security.exception;

import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.exception.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    ErrorCode errorCode;
    if (Boolean.TRUE.equals(request.getAttribute("deactivatedUser"))) {
      errorCode = ErrorCode.ALREADY_DEACTIVATED_USER;
    } else {
      errorCode = ErrorCode.NEED_TO_SIGN_IN;
    }

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setStatus(errorCode.getHttpStatus().value());
    response.getWriter().write(objectMapper.writeValueAsString(
        ErrorResponse.of(errorCode, request.getRequestURI())));
  }

}
