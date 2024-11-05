package com.example.moodwriter.domain.diary.controller;

import com.example.moodwriter.domain.diary.dto.DiaryAutoSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryFinalSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.service.DiaryService;
import com.example.moodwriter.global.constant.SortOrder;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

  private final DiaryService diaryService;

  @PostMapping
  public ResponseEntity<DiaryResponse> createDiary(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody(required = false) DiaryCreateRequest request) {
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

  @GetMapping("/{diaryId}")
  public ResponseEntity<DiaryResponse> getDiary(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    DiaryResponse response = diaryService.getDiary(diaryId, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<Slice<DiaryResponse>> getDiariesByDateRange(
      @RequestParam @PastOrPresent(message = "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다.") LocalDate startDate,
      @RequestParam @PastOrPresent(message = "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다.") LocalDate endDate,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "desc") SortOrder sortOrder,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Sort sort = sortOrder == SortOrder.DESC ? Sort.by("date").descending()
        : Sort.by("date").ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Slice<DiaryResponse> responses = diaryService.getDiariesByDateRange(
        startDate, endDate, pageable, userDetails.getId());
    return ResponseEntity.ok(responses);
  }


  @DeleteMapping("/{diaryId}")
  public ResponseEntity<Void> deleteDiary(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    diaryService.deleteDiary(diaryId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

}
