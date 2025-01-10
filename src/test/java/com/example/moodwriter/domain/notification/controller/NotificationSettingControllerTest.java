package com.example.moodwriter.domain.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.notification.dto.DailyReminderRequest;
import com.example.moodwriter.domain.notification.service.NotificationSettingService;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

@WebMvcTest(controllers = NotificationSettingController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
public class NotificationSettingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private NotificationSettingService notificationSettingService;

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
  void activateDailyReminder_shouldReturnOk() throws Exception {
    // Given
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(true)
        .remindTime(LocalTime.of(8, 0))
        .build();

    String requestBody = objectMapper.writeValueAsString(request);

    // When & Then
    mockMvc.perform(post("/api/notifications/activate/daily-reminder")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk());

    ArgumentCaptor<DailyReminderRequest> argumentCaptor =
        ArgumentCaptor.forClass(DailyReminderRequest.class);
    verify(notificationSettingService).activateDailyReminder(argumentCaptor.capture(),
        eq(userId));

    DailyReminderRequest capturedRequest = argumentCaptor.getValue();
    assertEquals(request.isActivate(), capturedRequest.isActivate());
    assertEquals(request.getRemindTime(), capturedRequest.getRemindTime());
  }

  @Test
  void activateDailyReminder_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
    // Given
    DailyReminderRequest request = DailyReminderRequest.builder()
        .isActivate(true)
        .build(); // Missing remindTime

    String requestBody = objectMapper.writeValueAsString(request);

    // When & Then
    mockMvc.perform(post("/api/notifications/activate/daily-reminder")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/notifications/activate/daily-reminder"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("remindTime"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("must not be null"));
  }
}
