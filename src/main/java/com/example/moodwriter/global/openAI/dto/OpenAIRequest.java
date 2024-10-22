package com.example.moodwriter.global.openAI.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenAIRequest {

  private String model;
  private List<Message> messages;

  @Getter
  @Builder
  public static class Message {
    private String role;
    private String content;
  }
}
