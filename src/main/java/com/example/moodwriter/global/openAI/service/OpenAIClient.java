package com.example.moodwriter.global.openAI.service;

import static com.example.moodwriter.global.exception.code.ErrorCode.FAIL_TO_CONNECT_WITH_OPEN_AI;
import static com.example.moodwriter.global.exception.code.ErrorCode.JSON_PARSE_ERROR;
import static com.example.moodwriter.global.exception.code.ErrorCode.OPEN_AI_RETURN_UNEXPECTED_RESPONSE;

import com.example.moodwriter.global.constant.OpenAIModel;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.openAI.dto.OpenAIRequest;
import com.example.moodwriter.global.openAI.dto.OpenAIRequest.Message;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse;
import com.example.moodwriter.global.openAI.exception.OpenAIException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAIClient {

  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;

  @Value("${cloud.open-ai.secret-key}")
  private String apiKey;

  @Value("${cloud.open-ai.url}")
  private String apiUrl;

  private final String role = "user";

  public OpenAIResponse callOpenAI(String diaryContent, OpenAIModel model) {

    OpenAIRequest.Message message = Message.builder()
        .role(role)
        .content(diaryContent)
        .build();

    OpenAIRequest request = OpenAIRequest.builder()
        .model(model.getModel())
        .messages(Collections.singletonList(message))
        .build();

    String jsonBody = null;
    try {
      jsonBody = objectMapper.writeValueAsString(request);
    } catch (JsonProcessingException e) {
      throw new CustomException(JSON_PARSE_ERROR);
    }

    RequestBody requestBody = RequestBody.create(jsonBody,
        MediaType.parse("application/json"));

    Request httpRequest = new Request.Builder()
        .url(apiUrl)
        .addHeader("Authorization", "Bearer " + apiKey)
        .post(requestBody)
        .build();

    try (Response response = okHttpClient.newCall(httpRequest).execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        throw new OpenAIException(OPEN_AI_RETURN_UNEXPECTED_RESPONSE);
      }

      String responseBody = response.body().string();

      return objectMapper.readValue(responseBody, OpenAIResponse.class);
    } catch (IOException e) {
      throw new OpenAIException(FAIL_TO_CONNECT_WITH_OPEN_AI);
    }

  }
}
