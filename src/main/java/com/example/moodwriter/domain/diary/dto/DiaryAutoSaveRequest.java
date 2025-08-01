package com.example.moodwriter.domain.diary.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryAutoSaveRequest {

  @NotBlank(message = "임시 저장 일기 내용은 반드시 입력해야 합니다.")
  private String content;

  private LocalDate date;
}
