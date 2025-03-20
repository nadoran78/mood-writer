package com.example.moodwriter.domain.fcm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.fcm.dto.FcmTokenRequest;
import com.example.moodwriter.domain.fcm.dto.FcmTokenResponse;
import com.example.moodwriter.domain.fcm.service.FcmTokenService;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FcmTokenController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class FcmTokenControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private FcmTokenService fcmTokenService;

  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setup() {
    User user = mock(User.class);
    CustomUserDetails userDetails = mock(CustomUserDetails.class);

    when(user.getId()).thenReturn(userId);
    when(userDetails.getId()).thenReturn(userId);

    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(
        new JwtAuthenticationToken(userDetails, "", null));
  }

  @Test
  void successSaveFcmToken() throws Exception {
    // given
    FcmTokenRequest request = FcmTokenRequest.builder()
        .deviceId("deviceId")
        .fcmToken("fcmToken")
        .deviceType("deviceType")
        .build();

    LocalDateTime now = LocalDateTime.now();

    FcmTokenResponse response = FcmTokenResponse.builder()
        .fcmTokenId(UUID.randomUUID())
        .deviceId("deviceId")
        .fcmToken("fcmToken")
        .deviceType("deviceType")
        .isActive(true)
        .createdAt(now)
        .updatedAt(now)
        .build();

    given(fcmTokenService.saveFcmToken(any(FcmTokenRequest.class), eq(userId)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/fcm-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fcmTokenId").value(response.getFcmTokenId().toString()))
        .andExpect(jsonPath("$.deviceId").value(response.getDeviceId()))
        .andExpect(jsonPath("$.fcmToken").value(response.getFcmToken()))
        .andExpect(jsonPath("$.deviceType").value(response.getDeviceType()))
        .andExpect(jsonPath("$.active").value(response.isActive()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void saveFcmToken_shouldThrowValidationError_whenInvalidRequest() throws Exception {
    // given
    FcmTokenRequest request = FcmTokenRequest.builder()
        .build();

    // when & then
    mockMvc.perform(post("/api/fcm-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/fcm-token"))
        .andExpect(jsonPath(
            "$.fieldErrors[?(@.field == 'fcmToken' && @.message == 'fcm token을 입력해주세요.')]").exists())
        .andExpect(jsonPath(
            "$.fieldErrors[?(@.field == 'deviceType' && @.message == 'device type을 입력해주세요.')]").exists())
        .andExpect(jsonPath(
            "$.fieldErrors[?(@.field == 'deviceId' && @.message == 'device id를 입력해주세요.')]").exists());
  }
}