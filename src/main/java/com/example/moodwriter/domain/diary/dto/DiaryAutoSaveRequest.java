package com.example.moodwriter.domain.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryAutoSaveRequest {

  private String title;

  @NotBlank(message = "임시 저장 일기 내용은 반드시 입력해야 합니다.")
  private String content;
}
