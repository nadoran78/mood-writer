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
        .build();

    UUID diaryId = UUID.randomUUID();
    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
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
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successCreateDiary_whenTitleAndContentIsNull() throws Exception {
    // given
    DiaryCreateRequest request = DiaryCreateRequest.builder()
        .build();

    UUID diaryId = UUID.randomUUID();
    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
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
        .build();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
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
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void successAutoSaveDiary_whenTitleIsNullInRequest() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryAutoSaveRequest request = DiaryAutoSaveRequest.builder()
        .content("자동 저장 내용")
        .build();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
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
        .andExpect(jsonPath("$.temp").value(response.isTemp()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  void autoSaveDiary_shouldReturnBadRequest_whenContentIsNullInRequest()
      throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryAutoSaveRequest request = DiaryAutoSaveRequest.builder()
        .title("자동 저장 제목")
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
        .build();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title(request.getTitle())
        .content(request.getContent())
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
  void successStartEditingDiary() throws Exception {
    // given
    UUID diaryId = UUID.randomUUID();

    DiaryResponse response = DiaryResponse.builder()
        .diaryId(diaryId)
        .title("제목")
        .content("내용")
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
}