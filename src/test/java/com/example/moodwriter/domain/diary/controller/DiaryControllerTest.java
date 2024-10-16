package com.example.moodwriter.domain.diary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
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
        .andExpect(jsonPath("$.createdAt").value(response.getCreatedAt().toString()))
        .andExpect(jsonPath("$.updatedAt").value(response.getUpdatedAt().toString()));
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
        .andExpect(jsonPath("$.createdAt").value(response.getCreatedAt().toString()))
        .andExpect(jsonPath("$.updatedAt").value(response.getUpdatedAt().toString()));
  }
}