package com.example.moodwriter.domain.diary.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryCreateRequest {
  private String content;
  private LocalDate date;
}
