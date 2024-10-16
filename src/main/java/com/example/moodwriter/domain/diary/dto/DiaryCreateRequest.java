package com.example.moodwriter.domain.diary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryCreateRequest {
  private String title;
  private String content;
}
