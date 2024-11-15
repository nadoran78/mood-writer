package com.example.moodwriter.domain.diary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.diary.dto.DiaryAutoSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryFinalSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.service.DiaryService;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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

@WebMvcTest(controllers = DiaryController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class DiaryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private DiaryService diaryService;

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
  void successCreateDiary() throws Exception {
    // given
    DiaryCreateRequest request = DiaryCreateRequest.builder()
        .title("임시 제목")
        .content("임시 내용")
        .date(LocalDate.now().minusDays(1))
        .build();

    UUID diaryId = UUID.randomUUID();
    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
        .date(request.getDate())
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.createDiary(eq(userId), any(DiaryCreateRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/diaries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successCreateDiary_whenRequestIsNull() throws Exception {
    // given
    DiaryCreateRequest request = null;

    UUID diaryId = UUID.randomUUID();
    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.createDiary(userId, null))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/diaries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successCreateDiary_whenTitleAndContentAndDateIsNull() throws Exception {
    // given
    DiaryCreateRequest request = DiaryCreateRequest.builder().build();

    UUID diaryId = UUID.randomUUID();
    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
        .date(request.getDate())
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.createDiary(eq(userId), any(DiaryCreateRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/diaries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successAutoSaveDiary() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryAutoSaveRequest request = DiaryAutoSaveRequest.builder()
        .title("자동 저장 제목")
        .content("자동 저장 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
        .date(request.getDate())
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.autoSaveDiary(eq(diaryId), eq(userId),
        any(DiaryAutoSaveRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(put("/api/diaries/auto-save/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successAutoSaveDiary_whenTitleAndDateIsNullInRequest() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryAutoSaveRequest request = DiaryAutoSaveRequest.builder()
        .content("자동 저장 내용")
        .build();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
        .date(request.getDate())
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.autoSaveDiary(eq(diaryId), eq(userId),
        any(DiaryAutoSaveRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(put("/api/diaries/auto-save/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void autoSaveDiary_shouldReturnBadRequest_whenRequestIsNull()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryAutoSaveRequest request = null;

    // when & then
    mockMvc.perform(put("/api/diaries/auto-save/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("HTTP_MESSAGE_NOT_READABLE"))
        .andExpect(jsonPath("$.message").value("HTTP 메시지를 읽을 수 없습니다."))
        .andExpect(jsonPath("$.path").value("/api/diaries/auto-save/" + diaryId));
  }

  @Test
  void autoSaveDiary_shouldReturnBadRequest_whenContentIsNullInRequest()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryAutoSaveRequest request = DiaryAutoSaveRequest.builder()
        .title("자동 저장 제목")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    // when & then
    mockMvc.perform(put("/api/diaries/auto-save/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("content"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("임시 저장 일기 내용은 반드시 입력해야 합니다."));
  }

  @Test
  void successFinalSaveDiary() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryFinalSaveRequest request = DiaryFinalSaveRequest.builder()
        .title("최종 저장 제목")
        .content("최종 저장 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
        .date(request.getDate())
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.finalSaveDiary(eq(diaryId), eq(userId),
        any(DiaryFinalSaveRequest.class))).willReturn(response);

    // when & then
    mockMvc.perform(put("/api/diaries/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void finalSaveDiary_shouldReturnBadRequest_whenContentIsNullInRequest()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryFinalSaveRequest request = DiaryFinalSaveRequest.builder()
        .title("최종 저장 제목")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    // when & then
    mockMvc.perform(put("/api/diaries/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("content"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("내용을 입력해주세요."));
  }

  @Test
  void finalSaveDiary_shouldReturnBadRequest_whenTitleIsNullInRequest()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryFinalSaveRequest request = DiaryFinalSaveRequest.builder()
        .content("최종 저장 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    // when & then
    mockMvc.perform(put("/api/diaries/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("title"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("제목을 입력해주세요."));
  }

  @Test
  void finalSaveDiary_shouldReturnBadRequest_whenDateIsNullInRequest()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryFinalSaveRequest request = DiaryFinalSaveRequest.builder()
        .title("최종 저장 제목")
        .content("최종 저장 내용")
        .build();

    // when & then
    mockMvc.perform(put("/api/diaries/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("date"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("일기 최종저장 시에는 작성일자가 필요합니다."));
  }

  @Test
  void finalSaveDiary_shouldReturnBadRequest_whenDateIsFuture()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryFinalSaveRequest request = DiaryFinalSaveRequest.builder()
        .title("최종 저장 제목")
        .content("최종 저장 내용")
        .date(LocalDate.now().plusDays(1))
        .build();

    // when & then
    mockMvc.perform(put("/api/diaries/" + diaryId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("date"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("일기 작성 날짜는 현재 또는 과거만 가능합니다."));
  }

  @Test
  void successStartEditingDiary() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title("제목")
        .content("내용")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.startEditingDiary(diaryId, userId)).willReturn(response);

    // when & then
    mockMvc.perform(patch("/api/diaries/" + diaryId))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successGetDiary() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title("제목")
        .content("내용")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    given(diaryService.getDiary(diaryId, userId)).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/diaries/" + diaryId))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.diaryId").value(diaryId.toString()))
        .andExpect(jsonPath("$.title").value(response.getTitle()))
        .andExpect(jsonPath("$.content").value(response.getContent()))
        .andExpect(jsonPath("$.date").value(response.getDate().toString()))
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successDeleteDiary() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/diaries/" + diaryId))
        .andExpect(status().isNoContent());

    verify(diaryService).deleteDiary(diaryId, userId);
  }

  @Test
  void successGetDiariesByDateRange_whenParameterIsDefault() throws Exception {
    // given
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 31);

    DiaryResponse response1 = DiaryResponse.builder()
        .diaryId(diaryId1)
        .title("제목1")
        .content("내용1")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    DiaryResponse response2 = DiaryResponse.builder()
        .diaryId(diaryId2)
        .title("제목2")
        .content("내용2")
        .date(LocalDate.of(2024, 10, 3))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    SliceImpl<DiaryResponse> responses = new SliceImpl<>(
        Arrays.asList(response1, response2),
        PageRequest.of(0, 10, Sort.by("date").descending()),
        false);

    given(diaryService.getDiariesByDateRange(eq(startDate), eq(endDate),
        any(Pageable.class), eq(userId))).willReturn(responses);

    // when & then
    mockMvc.perform(get("/api/diaries")
            .param("startDate", "2024-10-01")
            .param("endDate", "2024-10-31"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].diaryId").value(diaryId1.toString()))
        .andExpect(jsonPath("$.content[0].title").value(response1.getTitle()))
        .andExpect(jsonPath("$.content[0].content").value(response1.getContent()))
        .andExpect(jsonPath("$.content[0].date").value(response1.getDate().toString()))
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[1].diaryId").value(diaryId2.toString()))
        .andExpect(jsonPath("$.content[1].title").value(response2.getTitle()))
        .andExpect(jsonPath("$.content[1].content").value(response2.getContent()))
        .andExpect(jsonPath("$.content[1].date").value(response2.getDate().toString()))
        .andExpect(jsonPath("$.content[1].createdAt").exists())
        .andExpect(jsonPath("$.content[1].updatedAt").exists());
  }

  @Test
  void successGetDiariesByDateRange_whenInputParameter() throws Exception {
    // given
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 31);

    DiaryResponse response1 = DiaryResponse.builder()
        .diaryId(diaryId1)
        .title("제목1")
        .content("내용1")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    DiaryResponse response2 = DiaryResponse.builder()
        .diaryId(diaryId2)
        .title("제목2")
        .content("내용2")
        .date(LocalDate.of(2024, 10, 3))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    SliceImpl<DiaryResponse> responses = new SliceImpl<>(
        Arrays.asList(response1, response2),
        PageRequest.of(0, 10, Sort.by("date").descending()),
        false);

    given(diaryService.getDiariesByDateRange(eq(startDate), eq(endDate),
        any(Pageable.class), eq(userId))).willReturn(responses);

    // when & then
    mockMvc.perform(get("/api/diaries")
            .param("startDate", "2024-10-01")
            .param("endDate", "2024-10-31")
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "asc"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].diaryId").value(diaryId1.toString()))
        .andExpect(jsonPath("$.content[0].title").value(response1.getTitle()))
        .andExpect(jsonPath("$.content[0].content").value(response1.getContent()))
        .andExpect(jsonPath("$.content[0].date").value(response1.getDate().toString()))
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[1].diaryId").value(diaryId2.toString()))
        .andExpect(jsonPath("$.content[1].title").value(response2.getTitle()))
        .andExpect(jsonPath("$.content[1].content").value(response2.getContent()))
        .andExpect(jsonPath("$.content[1].date").value(response2.getDate().toString()))
        .andExpect(jsonPath("$.content[1].createdAt").exists())
        .andExpect(jsonPath("$.content[1].updatedAt").exists());
  }

  @Test
  void getDiariesByDateRange_shouldThrowValidationError_whenInputDateIsFuture()
      throws Exception {
    // given
    String startDate = LocalDate.now().plusDays(2).toString();
    String endDate = LocalDate.now().plusMonths(1).toString();

    // when & then
    mockMvc.perform(get("/api/diaries")
            .param("startDate", startDate)
            .param("endDate", endDate)
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "asc"))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/diaries"))
        .andExpect(jsonPath("$.parameterErrors[0].messages[0]").value(
            "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다."))
        .andExpect(jsonPath("$.parameterErrors[1].messages[0]").value(
            "조회하는 날짜는 오늘을 포함한 이전 날짜만 가능합니다."));
  }

  @Test
  void getDiariesByDateRange_shouldThrowError_whenInputInvalidSortOrder()
      throws Exception {
    // given
    String startDate = LocalDate.now().minusMonths(2).toString();
    String endDate = LocalDate.now().minusMonths(1).toString();

    // when & then
    mockMvc.perform(get("/api/diaries")
            .param("startDate", startDate)
            .param("endDate", endDate)
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "ascending"))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("METHOD_ARGUMENT_TYPE_MISMATCHED"))
        .andExpect(jsonPath("$.message").value("함수의 argument의 타입이 일치하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/diaries"));
  }

  @Test
  void successGetAllMyDiaries_whenInputParameter() throws Exception {
    // given
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    DiaryResponse response1 = DiaryResponse.builder()
        .diaryId(diaryId1)
        .title("제목1")
        .content("내용1")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    DiaryResponse response2 = DiaryResponse.builder()
        .diaryId(diaryId2)
        .title("제목2")
        .content("내용2")
        .date(LocalDate.of(2024, 10, 3))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    SliceImpl<DiaryResponse> responses = new SliceImpl<>(
        Arrays.asList(response1, response2),
        PageRequest.of(0, 10, Sort.by("date").descending()),
        false);

    given(diaryService.getAllMyDiaries(any(Pageable.class), eq(userId)))
        .willReturn(responses);

    // when & then
    mockMvc.perform(get("/api/diaries/all")
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "asc"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].diaryId").value(diaryId1.toString()))
        .andExpect(jsonPath("$.content[0].title").value(response1.getTitle()))
        .andExpect(jsonPath("$.content[0].content").value(response1.getContent()))
        .andExpect(jsonPath("$.content[0].date").value(response1.getDate().toString()))
        .andExpect(jsonPath("$.content[0].temp").value(response1.isTemp()))
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[1].diaryId").value(diaryId2.toString()))
        .andExpect(jsonPath("$.content[1].title").value(response2.getTitle()))
        .andExpect(jsonPath("$.content[1].content").value(response2.getContent()))
        .andExpect(jsonPath("$.content[1].date").value(response2.getDate().toString()))
        .andExpect(jsonPath("$.content[1].temp").value(response1.isTemp()))
        .andExpect(jsonPath("$.content[1].createdAt").exists())
        .andExpect(jsonPath("$.content[1].updatedAt").exists());
  }

  @Test
  void successGetAllMyDiaries_whenDoNotInputParameter() throws Exception {
    // given
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    DiaryResponse response1 = DiaryResponse.builder()
        .diaryId(diaryId1)
        .title("제목1")
        .content("내용1")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    DiaryResponse response2 = DiaryResponse.builder()
        .diaryId(diaryId2)
        .title("제목2")
        .content("내용2")
        .date(LocalDate.of(2024, 10, 3))
        .isTemp(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    SliceImpl<DiaryResponse> responses = new SliceImpl<>(
        Arrays.asList(response1, response2),
        PageRequest.of(0, 10, Sort.by("date").descending()),
        false);

    given(diaryService.getAllMyDiaries(any(Pageable.class), eq(userId)))
        .willReturn(responses);

    // when & then
    mockMvc.perform(get("/api/diaries/all"))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].diaryId").value(diaryId1.toString()))
        .andExpect(jsonPath("$.content[0].title").value(response1.getTitle()))
        .andExpect(jsonPath("$.content[0].content").value(response1.getContent()))
        .andExpect(jsonPath("$.content[0].date").value(response1.getDate().toString()))
        .andExpect(jsonPath("$.content[0].temp").value(response1.isTemp()))
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[1].diaryId").value(diaryId2.toString()))
        .andExpect(jsonPath("$.content[1].title").value(response2.getTitle()))
        .andExpect(jsonPath("$.content[1].content").value(response2.getContent()))
        .andExpect(jsonPath("$.content[1].date").value(response2.getDate().toString()))
        .andExpect(jsonPath("$.content[1].temp").value(response1.isTemp()))
        .andExpect(jsonPath("$.content[1].createdAt").exists())
        .andExpect(jsonPath("$.content[1].updatedAt").exists());
  }

  @Test
  void getAllMyDiaries_shouldThrowError_whenInputInvalidSortOrder()
      throws Exception {
    // when & then
    mockMvc.perform(get("/api/diaries/all")
            .param("page", "1")
            .param("size", "10")
            .param("sortOrder", "ascending"))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("METHOD_ARGUMENT_TYPE_MISMATCHED"))
        .andExpect(jsonPath("$.message").value("함수의 argument의 타입이 일치하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/diaries/all"));
  }
}