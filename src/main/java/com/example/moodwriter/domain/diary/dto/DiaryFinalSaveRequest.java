package com.example.moodwriter.domain.diary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryFinalSaveRequest {
  @NotBlank(message = "제목을 입력해주세요.")
  private String title;

  @NotBlank(message = "내용을 입력해주세요.")
  private String content;

  @NotNull(message = "일기 최종저장 시에는 작성일자가 필요합니다.")
  @PastOrPresent(message = "일기 작성 날짜는 현재 또는 과거만 가능합니다.")
  private LocalDate date;
}
