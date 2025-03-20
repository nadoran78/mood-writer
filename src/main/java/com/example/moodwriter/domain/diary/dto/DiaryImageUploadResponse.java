package com.example.moodwriter.domain.diary.dto;

import com.example.moodwriter.global.s3.dto.FileDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryImageUploadResponse {

  private List<String> imageUrls;
  private String message;

  public static DiaryImageUploadResponse of(List<FileDto> fileDtoList) {
    return DiaryImageUploadResponse.builder()
        .imageUrls(fileDtoList.stream().map(FileDto::getUrl).collect(
            Collectors.toList()))
        .message("이미지가 성공적으로 업로드되었습니다.")
        .build();
  }
}
