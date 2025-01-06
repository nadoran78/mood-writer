package com.example.moodwriter.domain.notification.controller;

import com.example.moodwriter.domain.notification.constant.NotificationTopic;
import com.example.moodwriter.domain.notification.dto.DailyReminderRequest;
import com.example.moodwriter.domain.notification.service.NotificationSettingService;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationSettingController {

  private final NotificationSettingService notificationSettingService;

  @PostMapping("/activate/daily-reminder")
  public ResponseEntity<Void> activateDailyReminder(DailyReminderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    return ResponseEntity.ok().build();
  }

}
