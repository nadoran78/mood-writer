package com.example.moodwriter.global.openAI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class OpenAIResponse {

  private String id;
  private String object;
  private Long created;
  private String model;

  @JsonProperty("system_fingerprint")
  private String systemFingerprint;

  private List<Choice> choices;
  private Usage usage;


  @Getter
  @Builder
  public static class Choice {
    private int index;
    private Message message;
    private Boolean logprobs;

    @JsonProperty("finish_reason")
    private String finishReason;
  }

  @Getter
  @Builder
  public static class Message {
    private String role;
    private String content;
  }

  @Getter
  @Builder
  public static class Usage {
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @JsonProperty("completion_tokens_details")
    private CompletionTokensDetails completionTokensDetails;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CompletionTokensDetails {
    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;
  }
}
