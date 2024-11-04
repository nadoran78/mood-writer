package com.example.moodwriter.domain.emotion.controller;

import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.service.EmotionAnalysisService;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
      @PathVariable UUID diaryId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    EmotionAnalysisResponse response = emotionAnalysisService.getEmotionAnalysis(
        diaryId, userDetails.getId());
    return ResponseEntity.ok(response);
  }
}
