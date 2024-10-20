package com.example.moodwriter.domain.diary.controller;

import com.example.moodwriter.domain.diary.dto.DiaryAutoSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryFinalSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.service.DiaryService;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

  private final DiaryService diaryService;

  @PostMapping
  public ResponseEntity<DiaryResponse> createDiary(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody DiaryCreateRequest request) {
    DiaryResponse response = diaryService.createDiary(userDetails.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/auto-save/{diaryId}")
  public ResponseEntity<DiaryResponse> autoSaveDiary(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid DiaryAutoSaveRequest request) {
    DiaryResponse response = diaryService.autoSaveDiary(diaryId, userDetails.getId(),
        request);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{diaryId}")
  public ResponseEntity<DiaryResponse> finalSaveDiary(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid DiaryFinalSaveRequest request) {
    DiaryResponse response = diaryService.finalSaveDiary(diaryId, userDetails.getId(),
        request);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{diaryId}")
  public ResponseEntity<DiaryResponse> startEditingDiary(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    DiaryResponse response = diaryService.startEditingDiary(diaryId, userDetails.getId());
    return ResponseEntity.ok(response);
  }

}
