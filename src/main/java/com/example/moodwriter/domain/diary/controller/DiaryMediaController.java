package com.example.moodwriter.domain.diary.controller;

import com.example.moodwriter.domain.diary.dto.DiaryImageDeleteRequest;
import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.domain.diary.service.DiaryMediaService;
import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.validation.annotation.ValidFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryMediaController {

  private final DiaryMediaService diaryMediaService;

  @PostMapping("/{diaryId}/images")
  public ResponseEntity<DiaryImageUploadResponse> uploadImages(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam("images") @ValidFile(allowFileType = FileType.IMAGE)
      @Size(max = 5, message = "한 번에 업로드할 수 있는 파일의 수는 최대 5개입니다.")
      List<MultipartFile> imageFiles) {
    DiaryImageUploadResponse response = diaryMediaService.uploadDiaryImages(diaryId,
        userDetails.getId(), imageFiles);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{diaryId}/images")
  public ResponseEntity<Void> deleteImage(@PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid DiaryImageDeleteRequest request) {
    diaryMediaService.deleteDiaryImage(diaryId, userDetails.getId(), request);
    return ResponseEntity.noContent().build();
  }


}
