package com.example.moodwriter.domain.emotion.controller;

import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.PrimaryEmotionAndScoreRequest;
import com.example.moodwriter.domain.emotion.service.EmotionAnalysisService;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotion-analysis")
public class EmotionAnalysisController {

  private final EmotionAnalysisService emotionAnalysisService;

  @PostMapping
  public ResponseEntity<EmotionAnalysisResponse> createPrimaryEmotionAndEmotionScore(
      @RequestBody @Valid PrimaryEmotionAndScoreRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    EmotionAnalysisResponse response = emotionAnalysisService.createPrimaryEmotionAndEmotionScore(
        request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
