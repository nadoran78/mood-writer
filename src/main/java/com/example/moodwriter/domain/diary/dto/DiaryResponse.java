package com.example.moodwriter.domain.diary.dto;

import com.example.moodwriter.domain.diary.entity.Diary;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryResponse {

  private UUID diaryId;
  private String title;
  private String content;
  private LocalDate date;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean isTemp;

  public static DiaryResponse fromEntity(Diary diary) {
    return DiaryResponse.builder()
        .diaryId(diary.getId())
        .title(diary.getTitle())
        .content(diary.getContent())
        .date(diary.getDate())
        .createdAt(diary.getCreatedAt())
        .updatedAt(diary.getUpdatedAt())
        .isTemp(diary.isTemp())
        .build();
  }
}
