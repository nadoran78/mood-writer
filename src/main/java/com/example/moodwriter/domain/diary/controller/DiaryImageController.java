package com.example.moodwriter.domain.diary.controller;

import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.domain.diary.service.DiaryImageService;
import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.validation.annotation.ValidFile;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryImageController {

  private final DiaryImageService diaryImageService;

  @PostMapping("/images")
  public ResponseEntity<DiaryImageUploadResponse> uploadImages(
      @RequestParam("images") @ValidFile(allowFileType = FileType.IMAGE)
      @Size(max = 5, message = "한 번에 업로드할 수 있는 파일의 수는 최대 5개입니다.")
      List<MultipartFile> imageFiles) {
    DiaryImageUploadResponse response = diaryImageService.uploadDiaryImages(imageFiles);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
