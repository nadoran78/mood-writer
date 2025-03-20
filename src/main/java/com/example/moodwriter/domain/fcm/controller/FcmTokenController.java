package com.example.moodwriter.domain.fcm.controller;

import com.example.moodwriter.domain.fcm.dto.FcmTokenRequest;
import com.example.moodwriter.domain.fcm.dto.FcmTokenResponse;
import com.example.moodwriter.domain.fcm.service.FcmTokenService;
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
@RequestMapping("/api/fcm-token")
@RequiredArgsConstructor
public class FcmTokenController {

  private final FcmTokenService fcmTokenService;

  @PostMapping
  public ResponseEntity<FcmTokenResponse> saveFcmToken(@Valid @RequestBody FcmTokenRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    FcmTokenResponse response = fcmTokenService.saveFcmToken(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
