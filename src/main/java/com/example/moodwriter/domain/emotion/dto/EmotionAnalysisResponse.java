package com.example.moodwriter.domain.emotion.dto;

import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionAnalysisResponse {

  private UUID emotionAnalysisId;
  private UUID diaryId;
  private LocalDate date;
  private List<String> primaryEmotion;
  private Integer emotionScore;
  private String analysisContent;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static EmotionAnalysisResponse fromEntity(EmotionAnalysis emotionAnalysis) {
    List<String> primaryEmotion;
    if (emotionAnalysis.getPrimaryEmotion() == null) {
      primaryEmotion = null;
    } else {
      primaryEmotion = Arrays.stream(emotionAnalysis.getPrimaryEmotion().split(","))
          .map(String::strip).collect(Collectors.toList());
    }
    return EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysis.getId())
        .diaryId(emotionAnalysis.getDiary().getId())
        .date(emotionAnalysis.getDate())
        .primaryEmotion(primaryEmotion)
        .emotionScore(emotionAnalysis.getEmotionScore())
        .analysisContent(emotionAnalysis.getAnalysisContent())
        .createdAt(emotionAnalysis.getCreatedAt())
        .updatedAt(emotionAnalysis.getUpdatedAt())
        .build();
  }

}
