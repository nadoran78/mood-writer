package com.example.moodwriter.global.openAI.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

import com.example.moodwriter.global.constant.OpenAIModel;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.openAI.dto.OpenAIRequest;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse;
import com.example.moodwriter.global.openAI.exception.OpenAIException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpenAIClientTest {

  private OpenAIClient openAIClient;
  private MockWebServer mockWebServer;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    objectMapper = spy(new ObjectMapper().registerModule(
        new ParameterNamesModule()));

    openAIClient = new OpenAIClient(new OkHttpClient(), objectMapper);
    ReflectionTestUtils.setField(openAIClient, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(openAIClient, "apiUrl",
        mockWebServer.url("/v1/chat/completions").toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void successCallOpenAI() throws IOException {
    // given
    MockResponse mockResponse = new MockResponse()
        .setResponseCode(200)
        .setBody(
            """
                {
                  "id": "chatcmpl-123",
                  "object": "chat.completion",
                  "created": 1677652288,
                  "model": "gpt-4o-mini",
                  "system_fingerprint": "fp_44709d6fcb",
                  "choices": [{
                    "index": 0,
                    "message": {
                      "role": "assistant",
                      "content": "감정 분석 완료"
                    },
                    "logprobs": null,
                    "finish_reason": "stop"
                  }],
                  "usage": {
                    "prompt_tokens": 9,
                    "completion_tokens": 12,
                    "total_tokens": 21,
                    "completion_tokens_details": {
                      "reasoning_tokens": 0
                    }
                  }
                }""")
        .setHeader("Content-Type", "application/json");
    mockWebServer.enqueue(mockResponse);

    String diaryContent = "오늘은 기분이 좋다.";

    // when
    OpenAIResponse openAIResponse = openAIClient.callOpenAI(diaryContent, OpenAIModel.GPT_4O);

    // then
    assertNotNull(openAIResponse);
    assertEquals("감정 분석 완료",
        openAIResponse.getChoices().get(0).getMessage().getContent());
  }

  @Test
  void callOpenAI_shouldReturnOpenAIException_whenResponseCodeIsNot200() {
    // given
    MockResponse mockResponse = new MockResponse()
        .setResponseCode(500)
        .setBody("{ \"error\": \"Internal Server Error\" }");
    mockWebServer.enqueue(mockResponse);

    String diaryContent = "오늘은 기분이 좋다.";

    // when
    OpenAIException openAIException = assertThrows(OpenAIException.class,
        () -> openAIClient.callOpenAI(diaryContent, OpenAIModel.GPT_3_5_TURBO));

    // then
    assertEquals(ErrorCode.OPEN_AI_RETURN_UNEXPECTED_RESPONSE,
        openAIException.getErrorCode());
  }

  @Test
  void callOpenAI_shouldReturnCustomException_whenJsonProcessingIsFailed() throws IOException {
    // given
    String diaryContent = "오늘은 기분이 좋다.";

    given(objectMapper.writeValueAsString(any(OpenAIRequest.class))).willThrow(
        JsonProcessingException.class);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> openAIClient.callOpenAI(diaryContent, OpenAIModel.GPT_3_5_TURBO));

    // then
    assertEquals(ErrorCode.JSON_PARSE_ERROR, customException.getErrorCode());
  }

  @Test
  void callOpenAI_shouldReturnCustomException_whenIOExceptionFromOkHttpClient() throws IOException {
    // given
    String diaryContent = "오늘은 기분이 좋다.";

    mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> openAIClient.callOpenAI(diaryContent, OpenAIModel.GPT_3_5_TURBO));

    // then
    assertEquals(ErrorCode.FAIL_TO_CONNECT_WITH_OPEN_AI, customException.getErrorCode());
  }
}