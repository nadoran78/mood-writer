package com.example.moodwriter.domain.emotion.service;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.emotion.dao.EmotionAnalysisRepository;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import com.example.moodwriter.domain.emotion.exception.EmotionAnalysisException;
import com.example.moodwriter.domain.emotion.service.EmotionAnalysisService.EmotionScoreAndPrimaryEmotion;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.constant.OpenAIModel;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse.Choice;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse.Message;
import com.example.moodwriter.global.openAI.service.OpenAIClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmotionAnalysisServiceTest {

  @Mock
  private DiaryRepository diaryRepository;
  @Mock
  private EmotionAnalysisRepository emotionAnalysisRepository;
  @Mock
  private OpenAIClient openAIClient;
  @Mock
  private ObjectMapper objectMapper;
  @InjectMocks
  private EmotionAnalysisService emotionAnalysisService;

  private final UUID userId = UUID.randomUUID();
  private final UUID diaryId = UUID.randomUUID();
  private Diary diary;
  private User user;

  @BeforeEach
  void setUp() {
    user = mock(User.class);

    diary = spy(Diary.builder()
        .user(user)
        .isDeleted(false)
        .isTemp(false)
        .date(LocalDate.of(2024, 10, 1))
        .build());
  }

  @Test
  void successCreatePrimaryEmotionAndEmotionScore_whenItIsFirstTimeForEmotionAnalysis()
      throws JsonProcessingException {
    // given
    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    String jsonResponse = """
        {
            "emotionScore": 8,
            "primaryEmotion": "행복, 만족, 감사"
        }""";
    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(jsonResponse)
                        .build())
                .build()))
        .build();

    EmotionScoreAndPrimaryEmotion emotionScoreAndPrimaryEmotion =
        EmotionScoreAndPrimaryEmotion.builder()
            .emotionScore(8)
            .primaryEmotion("행복, 만족, 감사")
            .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(Optional.empty());
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(objectMapper.readValue(jsonResponse, EmotionScoreAndPrimaryEmotion.class))
        .willReturn(emotionScoreAndPrimaryEmotion);
    given(emotionAnalysisRepository.save(any(EmotionAnalysis.class)))
        .will(returnsFirstArg());

    // when
    EmotionAnalysisResponse response =
        emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getDate(), response.getDate());
    assertEquals(emotionScoreAndPrimaryEmotion.getPrimaryEmotion(),
        response.getPrimaryEmotion());
    assertEquals(emotionScoreAndPrimaryEmotion.getEmotionScore(),
        response.getEmotionScore());
    assertNull(response.getAnalysisContent());
  }

  @Test
  void successCreatePrimaryEmotionAndEmotionScore_whenItIsNotFirstTimeForEmotionAnalysis()
      throws JsonProcessingException {
    // given
    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    EmotionAnalysis emotionAnalysis = EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .build();

    String jsonResponse = """
        {
            "emotionScore": 8,
            "primaryEmotion": "행복, 만족, 감사"
        }""";
    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(jsonResponse)
                        .build())
                .build()))
        .build();

    EmotionScoreAndPrimaryEmotion emotionScoreAndPrimaryEmotion =
        EmotionScoreAndPrimaryEmotion.builder()
            .emotionScore(8)
            .primaryEmotion("행복, 만족, 감사")
            .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(
        Optional.of(emotionAnalysis));
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(objectMapper.readValue(jsonResponse, EmotionScoreAndPrimaryEmotion.class))
        .willReturn(emotionScoreAndPrimaryEmotion);
    given(emotionAnalysisRepository.save(any(EmotionAnalysis.class)))
        .will(returnsFirstArg());

    // when
    EmotionAnalysisResponse response =
        emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getDate(), response.getDate());
    assertEquals(emotionScoreAndPrimaryEmotion.getPrimaryEmotion(),
        response.getPrimaryEmotion());
    assertEquals(emotionScoreAndPrimaryEmotion.getEmotionScore(),
        response.getEmotionScore());
    assertEquals(emotionAnalysis.getAnalysisContent(), response.getAnalysisContent());
  }

  @Test
  void successCreatePrimaryEmotionAndEmotionScore_whenEmotionAnalysisIsDeleted()
      throws JsonProcessingException {
    // given
    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    EmotionAnalysis emotionAnalysis = EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .isDeleted(true)
        .build();

    String jsonResponse = """
        {
            "emotionScore": 8,
            "primaryEmotion": "행복, 만족, 감사"
        }""";
    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(jsonResponse)
                        .build())
                .build()))
        .build();

    EmotionScoreAndPrimaryEmotion emotionScoreAndPrimaryEmotion =
        EmotionScoreAndPrimaryEmotion.builder()
            .emotionScore(8)
            .primaryEmotion("행복, 만족, 감사")
            .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(
        Optional.of(emotionAnalysis));
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(objectMapper.readValue(jsonResponse, EmotionScoreAndPrimaryEmotion.class))
        .willReturn(emotionScoreAndPrimaryEmotion);
    given(emotionAnalysisRepository.save(any(EmotionAnalysis.class)))
        .will(returnsFirstArg());

    // when
    EmotionAnalysisResponse response =
        emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request, userId);

    // then
    assertFalse(emotionAnalysis.isDeleted());
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getDate(), response.getDate());
    assertEquals(emotionScoreAndPrimaryEmotion.getPrimaryEmotion(),
        response.getPrimaryEmotion());
    assertEquals(emotionScoreAndPrimaryEmotion.getEmotionScore(),
        response.getEmotionScore());
    assertNull(response.getAnalysisContent());
  }

  @Test
  void createPrimaryEmotionAndEmotionScore_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request,
            userId));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void createPrimaryEmotionAndEmotionScore_shouldReturnDiaryException_whenDiaryWriterIsNotMatched() {
    // given
    UUID anotherUserId = UUID.randomUUID();
    given(user.getId()).willReturn(anotherUserId);

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request,
            userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, diaryException.getErrorCode());
  }

  @Test
  void createPrimaryEmotionAndEmotionScore_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    given(user.getId()).willReturn(userId);
    diary.deactivate();

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request,
            userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void createPrimaryEmotionAndEmotionScore_shouldReturnEmotionAnalysisException_whenDiaryIsTemp() {
    // given
    given(user.getId()).willReturn(userId);
    diary.startEditing();

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request,
            userId));

    assertEquals(ErrorCode.FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void createPrimaryEmotionAndEmotionScore_shouldReturnCustomException_whenJsonParsingErrorIsOccurred()
      throws JsonProcessingException {
    // given
    given(user.getId()).willReturn(userId);

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    String jsonResponse = """
        {
            "emotionScore": 8,
            "primaryEmotion": "행복, 만족, 감사"
        }""";
    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(jsonResponse)
                        .build())
                .build()))
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(Optional.empty());
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(objectMapper.readValue(jsonResponse, EmotionScoreAndPrimaryEmotion.class))
        .willThrow(JsonProcessingException.class);

    // when & then
    CustomException customException = assertThrows(
        CustomException.class,
        () -> emotionAnalysisService.createPrimaryEmotionAndEmotionScore(request,
            userId));

    assertEquals(ErrorCode.JSON_PARSE_ERROR, customException.getErrorCode());
  }

}