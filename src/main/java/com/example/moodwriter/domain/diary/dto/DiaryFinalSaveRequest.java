package com.example.moodwriter.domain.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryFinalSaveRequest {
  @NotBlank
  private String title;

  @NotBlank
  private String content;
}
