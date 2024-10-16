package com.example.moodwriter.domain.diary.controller;

import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.service.DiaryService;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
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

}
