package com.example.moodwriter.domain.diary.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryImageDeleteRequest {

  @NotEmpty(message = "이미지 URL 목록은 비어 있을 수 없습니다.")
  private List<String> imageUrls;
}
