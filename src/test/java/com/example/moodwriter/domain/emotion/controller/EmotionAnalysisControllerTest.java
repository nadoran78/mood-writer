package com.example.moodwriter.domain.emotion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.service.EmotionAnalysisService;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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

@WebMvcTest(controllers = EmotionAnalysisController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class EmotionAnalysisControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private EmotionAnalysisService emotionAnalysisService;

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
  void successCreatePrimaryEmotionAndEmotionScore() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();
    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    UUID emotionAnalysisId = UUID.randomUUID();
    EmotionAnalysisResponse response = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysisId)
        .diaryId(diaryId)
        .date(LocalDate.of(2024, 10, 1))
        .primaryEmotion("행복, 감사, 만족")
        .emotionScore(8)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(emotionAnalysisService.createPrimaryEmotionAndEmotionScore(
        any(EmotionAnalysisRequest.class), eq(userId)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/emotion-analysis/score")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.emotionAnalysisId").value(emotionAnalysisId.toString()))
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.primaryEmotion").value(response.getPrimaryEmotion()))
        .andExpect(jsonPath("$.emotionScore").value(response.getEmotionScore()))
        .andExpect(jsonPath("$.analysisContent").isEmpty())
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }


}