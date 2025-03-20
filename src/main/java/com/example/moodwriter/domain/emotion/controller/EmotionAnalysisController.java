package com.example.moodwriter.domain.emotion.controller;

import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.service.EmotionAnalysisService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotion-analysis")
public class EmotionAnalysisController {

  private final EmotionAnalysisService emotionAnalysisService;

  @PostMapping("/score")
  public ResponseEntity<EmotionAnalysisResponse> createPrimaryEmotionAndEmotionScore(
      @RequestBody @Valid EmotionAnalysisRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    EmotionAnalysisResponse response = emotionAnalysisService.createPrimaryEmotionAndEmotionScore(
        request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/detail")
  public ResponseEntity<EmotionAnalysisResponse> createEmotionAnalysis(
      @RequestBody @Valid EmotionAnalysisRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    EmotionAnalysisResponse response = emotionAnalysisService.createEmotionAnalysis(
        request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{diaryId}")
  public ResponseEntity<EmotionAnalysisResponse> getEmotionAnalysis(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    EmotionAnalysisResponse response = emotionAnalysisService.getEmotionAnalysis(
        diaryId, userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<Slice<EmotionAnalysisResponse>> getEmotionAnalysisByDateRange(
      @RequestParam @PastOrPresent(message = "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다.") LocalDate startDate,
      @RequestParam @PastOrPresent(message = "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다.") LocalDate endDate,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "desc") SortOrder sortOrder,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Sort sort = sortOrder == SortOrder.DESC ? Sort.by("date").descending()
        : Sort.by("date").ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Slice<EmotionAnalysisResponse> responses = emotionAnalysisService.getEmotionAnalysisByDateRange(
        startDate, endDate, userDetails.getId(), pageable);
    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("/{diaryId}")
  public ResponseEntity<Void> deleteEmotionAnalysis(
      @PathVariable UUID diaryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    emotionAnalysisService.deleteEmotionAnalysis(diaryId, userDetails.getId());
    return ResponseEntity.noContent().build();
  }

}
