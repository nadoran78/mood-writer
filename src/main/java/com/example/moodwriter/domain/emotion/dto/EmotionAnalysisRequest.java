package com.example.moodwriter.domain.emotion.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisRequest {

  @NotNull
  private UUID diaryId;

}
