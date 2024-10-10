package com.example.moodwriter.diary.dto;

import com.example.moodwriter.global.dto.FileDto;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryImageUploadResponse {

  private List<String> imageUrls;
  private UUID diaryId;
  private String message;

  public static DiaryImageUploadResponse of(List<FileDto> fileDtoList, UUID diaryId) {
    return DiaryImageUploadResponse.builder()
        .imageUrls(fileDtoList.stream().map(FileDto::getUrl).collect(
            Collectors.toList()))
        .diaryId(diaryId)
        .message("이미지가 성공적으로 업로드되었습니다.")
        .build();
  }
}
