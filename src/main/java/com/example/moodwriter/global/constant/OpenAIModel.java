package com.example.moodwriter.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenAIModel {

  GPT_4O_MINI("gpt-4o-mini"),
  GPT_3_5_TURBO("gpt-3.5-turbo-0125"),
  GPT_4O("gpt-4o")

  ;


  private final String model;
}
