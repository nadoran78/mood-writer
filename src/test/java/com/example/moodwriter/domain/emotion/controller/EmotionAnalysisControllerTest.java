package com.example.moodwriter.domain.emotion.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.service.EmotionAnalysisService;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
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
        .primaryEmotion(List.of("행복", "감사", "만족"))
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
        .andExpect(jsonPath("$.primaryEmotion", hasSize(3)))
        .andExpect(jsonPath("$.primaryEmotion", hasItem(response.getPrimaryEmotion().get(0))))
        .andExpect(jsonPath("$.primaryEmotion", hasItem(response.getPrimaryEmotion().get(1))))
        .andExpect(jsonPath("$.primaryEmotion", hasItem(response.getPrimaryEmotion().get(2))))
        .andExpect(jsonPath("$.emotionScore").value(response.getEmotionScore()))
        .andExpect(jsonPath("$.analysisContent").isEmpty())
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successCreateEmotionAnalysis() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();
    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    UUID emotionAnalysisId = UUID.randomUUID();
    EmotionAnalysisResponse response = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysisId)
        .diaryId(diaryId)
        .date(LocalDate.of(2024, 10, 1))
        .analysisContent("행복하게 잘 살고 있으십니다.")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(emotionAnalysisService.createEmotionAnalysis(
        any(EmotionAnalysisRequest.class), eq(userId)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/emotion-analysis/detail")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.emotionAnalysisId").value(emotionAnalysisId.toString()))
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.primaryEmotion").isEmpty())
        .andExpect(jsonPath("$.emotionScore").isEmpty())
        .andExpect(jsonPath("$.analysisContent").value(response.getAnalysisContent()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successGetEmotionAnalysis() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    EmotionAnalysisResponse response = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(UUID.randomUUID())
        .diaryId(diaryId)
        .date(LocalDate.now())
        .primaryEmotion(List.of("행복", "감사", "만족"))
        .emotionScore(7)
        .analysisContent("행복하신 하루였습니다.")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(emotionAnalysisService.getEmotionAnalysis(diaryId, userId))
        .willReturn(response);

    // when & then
    mockMvc.perform(get("/api/emotion-analysis/" + diaryId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.emotionAnalysisId").value(
            response.getEmotionAnalysisId().toString()))
        .andExpect(jsonPath("$.diaryId").value(response.getDiaryId().toString()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.primaryEmotion", hasSize(3)))
        .andExpect(jsonPath("$.primaryEmotion", hasItem(response.getPrimaryEmotion().get(0))))
        .andExpect(jsonPath("$.primaryEmotion", hasItem(response.getPrimaryEmotion().get(1))))
        .andExpect(jsonPath("$.primaryEmotion", hasItem(response.getPrimaryEmotion().get(2))))
        .andExpect(jsonPath("$.emotionScore").value(response.getEmotionScore()))
        .andExpect(jsonPath("$.analysisContent").value(response.getAnalysisContent()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successDeleteEmotionAnalysis() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/emotion-analysis/" + diaryId))
        .andExpect(status().isNoContent());

    verify(emotionAnalysisService).deleteEmotionAnalysis(diaryId, userId);
  }

  @Test
  void successGetEmotionAnalysisByDateRange_whenParameterIsDefault() throws Exception {
    // given
    UUID emotionAnalysisId1 = UUID.randomUUID();
    UUID emotionAnalysisId2 = UUID.randomUUID();
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 31);

    EmotionAnalysisResponse response1 = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysisId1)
        .diaryId(diaryId1)
        .date(LocalDate.of(2024, 10, 1))
        .primaryEmotion(List.of("행복", "감사", "만족"))
        .emotionScore(9)
        .analysisContent("행복해보이십니다.")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    EmotionAnalysisResponse response2 = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysisId2)
        .diaryId(diaryId2)
        .date(LocalDate.of(2024, 10, 10))
        .primaryEmotion(List.of("행복", "감사", "만족"))
        .emotionScore(2)
        .analysisContent("불안해보이십니다.")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    SliceImpl<EmotionAnalysisResponse> responses = new SliceImpl<>(
        Arrays.asList(response1, response2),
        PageRequest.of(0, 10, Sort.by("date").descending()),
        false);

    given(emotionAnalysisService.getEmotionAnalysisByDateRange(eq(startDate), eq(endDate),
        eq(userId), any(Pageable.class))).willReturn(responses);

    // when & then
    mockMvc.perform(get("/api/emotion-analysis")
            .param("startDate", "2024-10-01")
            .param("endDate", "2024-10-31"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].emotionAnalysisId").value(
            emotionAnalysisId1.toString()))
        .andExpect(jsonPath("$.content[0].diaryId").value(
            diaryId1.toString()))
        .andExpect(jsonPath("$.content[0].date").value(response1.getDate().toString()))
        .andExpect(jsonPath("$.content[0].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(0))))
        .andExpect(jsonPath("$.content[0].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(1))))
        .andExpect(jsonPath("$.content[0].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(2))))
        .andExpect(jsonPath("$.content[0].emotionScore").value(response1.getEmotionScore()))
        .andExpect(jsonPath("$.content[0].analysisContent").value(response1.getAnalysisContent()))
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[1].emotionAnalysisId").value(
            emotionAnalysisId2.toString()))
        .andExpect(jsonPath("$.content[1].diaryId").value(
            diaryId2.toString()))
        .andExpect(jsonPath("$.content[1].date").value(response2.getDate().toString()))
        .andExpect(jsonPath("$.content[1].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(0))))
        .andExpect(jsonPath("$.content[1].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(1))))
        .andExpect(jsonPath("$.content[1].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(2))))
        .andExpect(jsonPath("$.content[1].emotionScore").value(response2.getEmotionScore()))
        .andExpect(jsonPath("$.content[1].analysisContent").value(response2.getAnalysisContent()))
        .andExpect(jsonPath("$.content[1].createdAt").exists())
        .andExpect(jsonPath("$.content[1].updatedAt").exists());
  }

  @Test
  void successGetEmotionAnalysisByDateRange_whenInputParameter() throws Exception {
    // given
    UUID emotionAnalysisId1 = UUID.randomUUID();
    UUID emotionAnalysisId2 = UUID.randomUUID();
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 31);

    EmotionAnalysisResponse response1 = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysisId1)
        .diaryId(diaryId1)
        .date(LocalDate.of(2024, 10, 1))
        .primaryEmotion(List.of("행복", "감사", "만족"))
        .emotionScore(9)
        .analysisContent("행복해보이십니다.")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    EmotionAnalysisResponse response2 = EmotionAnalysisResponse.builder()
        .emotionAnalysisId(emotionAnalysisId2)
        .diaryId(diaryId2)
        .date(LocalDate.of(2024, 10, 10))
        .primaryEmotion(List.of("행복", "감사", "만족"))
        .emotionScore(2)
        .analysisContent("불안해보이십니다.")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    SliceImpl<EmotionAnalysisResponse> responses = new SliceImpl<>(
        Arrays.asList(response1, response2),
        PageRequest.of(0, 10, Sort.by("date").descending()),
        false);

    given(emotionAnalysisService.getEmotionAnalysisByDateRange(eq(startDate), eq(endDate),
        eq(userId), any(Pageable.class))).willReturn(responses);

    // when & then
    mockMvc.perform(get("/api/emotion-analysis")
            .param("startDate", "2024-10-01")
            .param("endDate", "2024-10-31")
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "asc"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].emotionAnalysisId").value(
            emotionAnalysisId1.toString()))
        .andExpect(jsonPath("$.content[0].diaryId").value(
            diaryId1.toString()))
        .andExpect(jsonPath("$.content[0].date").value(response1.getDate().toString()))
        .andExpect(jsonPath("$.content[0].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(0))))
        .andExpect(jsonPath("$.content[0].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(1))))
        .andExpect(jsonPath("$.content[0].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(2))))
        .andExpect(jsonPath("$.content[0].emotionScore").value(response1.getEmotionScore()))
        .andExpect(jsonPath("$.content[0].analysisContent").value(response1.getAnalysisContent()))
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[1].emotionAnalysisId").value(
            emotionAnalysisId2.toString()))
        .andExpect(jsonPath("$.content[1].diaryId").value(
            diaryId2.toString()))
        .andExpect(jsonPath("$.content[1].date").value(response2.getDate().toString()))
        .andExpect(jsonPath("$.content[1].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(0))))
        .andExpect(jsonPath("$.content[1].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(1))))
        .andExpect(jsonPath("$.content[1].primaryEmotion",
            hasItem(response1.getPrimaryEmotion().get(2))))
        .andExpect(jsonPath("$.content[1].emotionScore").value(response2.getEmotionScore()))
        .andExpect(jsonPath("$.content[1].analysisContent").value(response2.getAnalysisContent()))
        .andExpect(jsonPath("$.content[1].createdAt").exists())
        .andExpect(jsonPath("$.content[1].updatedAt").exists());
  }

  @Test
  void getEmotionAnalysisByDateRange_shouldThrowValidationError_whenInputDateIsFuture()
      throws Exception {
    // given
    String startDate = LocalDate.now().plusDays(2).toString();
    String endDate = LocalDate.now().plusMonths(1).toString();

    // when & then
    mockMvc.perform(get("/api/emotion-analysis")
            .param("startDate", startDate)
            .param("endDate", endDate)
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "asc"))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/emotion-analysis"))
        .andExpect(jsonPath("$.parameterErrors[0].messages[0]").value(
            "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다."))
        .andExpect(jsonPath("$.parameterErrors[1].messages[0]").value(
            "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다."));
  }

  @Test
  void getEmotionAnalysisByDateRange_shouldThrowError_whenInputInvalidSortOrder()
      throws Exception {
    // given
    String startDate = LocalDate.now().minusMonths(2).toString();
    String endDate = LocalDate.now().minusMonths(1).toString();

    // when & then
    mockMvc.perform(get("/api/emotion-analysis")
            .param("startDate", startDate)
            .param("endDate", endDate)
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "ascending"))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("METHOD_ARGUMENT_TYPE_MISMATCHED"))
        .andExpect(jsonPath("$.message").value("함수의 argument의 타입이 일치하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/emotion-analysis"));
  }


}