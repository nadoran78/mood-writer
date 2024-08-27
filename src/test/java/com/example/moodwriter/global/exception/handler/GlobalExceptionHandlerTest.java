package com.example.moodwriter.global.exception.handler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.exception.handler.GlobalExceptionHandlerTest.ExceptionHandlerTestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Import({ExceptionHandlerTestController.class})
@WebMvcTest(controllers = {ExceptionHandlerTestController.class,
    GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void handleCustomException() throws Exception {
    // given
    String uri = "/custom-exception";

    // when & then
    mockMvc.perform(get(uri))
        .andExpect(status().is(ErrorCode.INTERNAL_ERROR.getHttpStatus().value()))
        .andExpect(
            jsonPath("$.message").value(ErrorCode.INTERNAL_ERROR.getMessage()))
        .andExpect(jsonPath("$.path").value(uri));
  }

  @Test
  void handleMethodArgumentNotValidException() throws Exception {
    // given
    String uri = "/method-argument-not-valid";
    SampleRequest requestBody = new SampleRequest();

    // when & then
    mockMvc.perform(post(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_ERROR.getMessage()))
        .andExpect(jsonPath("$.path").value(uri))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("must not be blank"));
  }

  @Test
  void handleHandlerMethodValidationException() throws Exception {
    // given
    String uri = "/trigger-handler-method-validation-exception";

    // when & then
    mockMvc.perform(get(uri)
            .param("age", "5"))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_ERROR.getMessage()))
        .andExpect(jsonPath("$.path").value(uri))
        .andExpect(jsonPath("$.parameterErrors[0].parameter").value("age"))
        .andExpect(jsonPath("$.parameterErrors[0].messages[0]").value(
            "must be greater than or equal to 10"));
  }

  @Test
  void handleJsonEOFException() throws Exception {
    // given
    String uri = "/method-argument-not-valid";
    String requestBody = "{\"name\": 12";

    // when & then
    mockMvc.perform(post(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.message").value(ErrorCode.JSON_EOF_ERROR.getMessage()))
        .andExpect(jsonPath("$.path").value(uri));
  }

  @Test
  void handleHttpNotReadableException() throws Exception {
    // given
    String uri = "/method-argument-not-valid";
    String requestBody = "";

    // when & then
    mockMvc.perform(post(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(
            jsonPath("$.message").value(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getMessage()))
        .andExpect(jsonPath("$.path").value(uri));
  }

  @Test
  void handleRuntimeException() throws Exception {
    // given
    String uri = "/runtime-exception";

    // when & then
    mockMvc.perform(get(uri))
        .andExpect(status().isInternalServerError())
        .andDo(print())
        .andExpect(
            jsonPath("$.message").value(ErrorCode.INTERNAL_ERROR.getMessage()))
        .andExpect(jsonPath("$.path").value(uri));
  }


  @RestController
  static class ExceptionHandlerTestController {

    @GetMapping("/custom-exception")
    public void throwCustomException() {
      throw new CustomException(ErrorCode.INTERNAL_ERROR);
    }

    @PostMapping("/method-argument-not-valid")
    public void handleMethodArgumentNotValid(@Valid @RequestBody SampleRequest request) {

    }

    @GetMapping("/trigger-handler-method-validation-exception")
    public void triggerHandlerMethodValidationException(
        @Min(10) @RequestParam int age) {

    }

    @GetMapping("/runtime-exception")
    public void throwRuntimeException() {
      throw new RuntimeException("Runtime Exception");
    }
  }

  static class SampleRequest {

    @NotBlank
    private String name;

    public String getName() {
      return this.name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}