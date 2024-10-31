package com.example.moodwriter.domain.emotion.dto;

import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionAnalysisResponse {

  private UUID emotionAnalysisId;
  private UUID diaryId;
  private LocalDate date;
  private String primaryEmotion;
  private Integer emotionScore;
  private String analysisContent;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static EmotionAnalysisResponse fromEntity(EmotionAnalysis emotionAnalysis) {
    return EmotionAnalysisResponse.builder()
        .diaryId(emotionAnalysis.getDiary().getId())
        .date(emotionAnalysis.getDate())
        .primaryEmotion(emotionAnalysis.getPrimaryEmotion())
        .emotionScore(emotionAnalysis.getEmotionScore())
        .analysisContent(emotionAnalysis.getAnalysisContent())
        .createdAt(emotionAnalysis.getCreatedAt())
        .updatedAt(emotionAnalysis.getUpdatedAt())
        .build();
  }

}
