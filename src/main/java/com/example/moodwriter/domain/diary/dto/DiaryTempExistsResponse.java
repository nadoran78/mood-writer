package com.example.moodwriter.domain.diary.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryTempExistsResponse {

  private boolean tempExists;
  private UUID diaryId;

  public static DiaryTempExistsResponse makeTrueResponse(UUID diaryId) {
    return DiaryTempExistsResponse.builder()
        .tempExists(true)
        .diaryId(diaryId)
        .build();
  }

  public static DiaryTempExistsResponse makeFalseResponse() {
    return DiaryTempExistsResponse.builder()
        .tempExists(false)
        .build();
  }

}
