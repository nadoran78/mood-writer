package com.example.moodwriter.domain.emotion.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.emotion.dao.EmotionAnalysisRepository;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
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
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class EmotionAnalysisServiceTest {

  @Mock
  private DiaryRepository diaryRepository;
  @Mock
  private EmotionAnalysisRepository emotionAnalysisRepository;
  @Mock
  private EntityManager entityManager;
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
    assertEquals(Arrays.stream(
        emotionScoreAndPrimaryEmotion.getPrimaryEmotion().split(","))
            .map(String::strip).collect(Collectors.toList()),
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
    assertEquals(Arrays.stream(
                emotionScoreAndPrimaryEmotion.getPrimaryEmotion().split(","))
            .map(String::strip).collect(Collectors.toList()),
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
    assertEquals(Arrays.stream(
                emotionScoreAndPrimaryEmotion.getPrimaryEmotion().split(","))
            .map(String::strip).collect(Collectors.toList()),
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

  @Test
  void successCreateEmotionAnalysis_whenItIsFirstTimeForEmotionAnalysis() {
    // given
    String analysisContent = "일기를 보니 행복하십니다.";

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(analysisContent)
                        .build())
                .build()))
        .build();

    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(Optional.empty());
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(emotionAnalysisRepository.save(any(EmotionAnalysis.class)))
        .will(returnsFirstArg());

    // when
    EmotionAnalysisResponse response = emotionAnalysisService.createEmotionAnalysis(
        request, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getDate(), response.getDate());
    assertNull(response.getPrimaryEmotion());
    assertNull(response.getEmotionScore());
    assertEquals(analysisContent, response.getAnalysisContent());
  }

  @Test
  void successCreateEmotionAnalysis_whenItIsNotFirstTimeForEmotionAnalysis() {
    // given
    String analysisContent = "일기를 보니 행복하십니다.";

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(analysisContent)
                        .build())
                .build()))
        .build();

    EmotionAnalysis emotionAnalysis = EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .build();

    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary))
        .willReturn(Optional.of(emotionAnalysis));
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(emotionAnalysisRepository.save(any(EmotionAnalysis.class)))
        .will(returnsFirstArg());

    // when
    EmotionAnalysisResponse response = emotionAnalysisService.createEmotionAnalysis(
        request, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(emotionAnalysis.getDate(), response.getDate());
    assertEquals(Arrays.stream(
            emotionAnalysis.getPrimaryEmotion().split(","))
        .map(String::strip).collect(Collectors.toList()), response.getPrimaryEmotion());
    assertEquals(emotionAnalysis.getEmotionScore(), response.getEmotionScore());
    assertEquals(analysisContent, response.getAnalysisContent());
  }

  @Test
  void successCreateEmotionAnalysis_whenEmotionAnalysisIsDeleted() {
    // given
    String analysisContent = "일기를 보니 행복하십니다.";

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    OpenAIResponse openAIResponse = OpenAIResponse.builder()
        .choices(Collections.singletonList(
            Choice.builder()
                .message(
                    Message.builder()
                        .content(analysisContent)
                        .build())
                .build()))
        .build();

    EmotionAnalysis emotionAnalysis = EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔, 불안, 초조")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .isDeleted(true)
        .deletedAt(LocalDateTime.now())
        .build();

    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary))
        .willReturn(Optional.of(emotionAnalysis));
    given(openAIClient.callOpenAI(anyString(), any(OpenAIModel.class)))
        .willReturn(openAIResponse);
    given(emotionAnalysisRepository.save(any(EmotionAnalysis.class)))
        .will(returnsFirstArg());

    // when
    EmotionAnalysisResponse response = emotionAnalysisService.createEmotionAnalysis(
        request, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getDate(), response.getDate());
    assertNull(emotionAnalysis.getPrimaryEmotion());
    assertNull(emotionAnalysis.getEmotionScore());
    assertEquals(analysisContent, response.getAnalysisContent());
    assertFalse(emotionAnalysis.isDeleted());
    assertNull(emotionAnalysis.getDeletedAt());
  }

  @Test
  void createEmotionAnalysis_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.createEmotionAnalysis(request, userId));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void createEmotionAnalysis_shouldReturnDiaryException_whenDiaryWriterIsNotMatched() {
    // given
    UUID anotherUserId = UUID.randomUUID();
    given(user.getId()).willReturn(anotherUserId);

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.createEmotionAnalysis(request, userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, diaryException.getErrorCode());
  }

  @Test
  void createEmotionAnalysis_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    given(user.getId()).willReturn(userId);
    diary.deactivate();

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.createEmotionAnalysis(request, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void createEmotionAnalysis_shouldReturnEmotionAnalysisException_whenDiaryIsTemp() {
    // given
    given(user.getId()).willReturn(userId);
    diary.startEditing();

    EmotionAnalysisRequest request = new EmotionAnalysisRequest(diaryId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.createEmotionAnalysis(request, userId));

    assertEquals(ErrorCode.FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void successGetEmotionAnalysis() {
    // given
    UUID emotionAnalysisId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    EmotionAnalysis emotionAnalysis = spy(EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .isDeleted(false)
        .build());

    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);
    given(emotionAnalysis.getId()).willReturn(emotionAnalysisId);
    given(emotionAnalysis.getCreatedAt()).willReturn(now);
    given(emotionAnalysis.getUpdatedAt()).willReturn(now);
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary))
        .willReturn(Optional.of(emotionAnalysis));

    // when
    EmotionAnalysisResponse response = emotionAnalysisService.getEmotionAnalysis(
        diaryId, userId);

    // then
    assertEquals(emotionAnalysisId, response.getEmotionAnalysisId());
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getDate(), response.getDate());
    assertEquals(Arrays.stream(
            emotionAnalysis.getPrimaryEmotion().split(","))
        .map(String::strip).collect(Collectors.toList()), response.getPrimaryEmotion());
    assertEquals(emotionAnalysis.getEmotionScore(), response.getEmotionScore());
    assertEquals(emotionAnalysis.getAnalysisContent(), response.getAnalysisContent());
    assertEquals(now, response.getCreatedAt());
    assertEquals(now, response.getUpdatedAt());
  }

  @Test
  void getEmotionAnalysis_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.getEmotionAnalysis(diaryId, userId));

    // then
    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void getEmotionAnalysis_shouldReturnDiaryException_whenDiaryWriterIsNotMatched() {
    // given
    UUID anotherUserId = UUID.randomUUID();
    given(user.getId()).willReturn(anotherUserId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.getEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, diaryException.getErrorCode());
  }

  @Test
  void getEmotionAnalysis_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    given(user.getId()).willReturn(userId);
    diary.deactivate();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.getEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void getEmotionAnalysis_shouldReturnEmotionAnalysisException_whenDiaryIsTemp() {
    // given
    given(user.getId()).willReturn(userId);
    diary.startEditing();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.getEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void getEmotionAnalysis_shouldReturnEmotionAnalysisException_whenEmotionAnalysisIsNotExist() {
    // given
    given(user.getId()).willReturn(userId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(Optional.empty());

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.getEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.NOT_FOUND_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void getEmotionAnalysis_shouldReturnEmotionAnalysisException_whenEmotionAnalysisIsDeleted() {
    // given
    EmotionAnalysis emotionAnalysis = spy(EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .isDeleted(true)
        .deletedAt(LocalDateTime.now())
        .build());

    given(user.getId()).willReturn(userId);
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(
        Optional.of(emotionAnalysis));

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.getEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void successDeleteDiary() {
    // given
    EmotionAnalysis emotionAnalysis = EmotionAnalysis.builder()
        .diary(diary)
        .emotionScore(0)
        .primaryEmotion("슬픔")
        .analysisContent("행복하십니다.")
        .date(diary.getDate())
        .isDeleted(false)
        .build();

    given(user.getId()).willReturn(userId);
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary))
        .willReturn(Optional.of(emotionAnalysis));

    // when & then
    emotionAnalysisService.deleteEmotionAnalysis(diaryId, userId);

    verify(emotionAnalysisRepository).save(emotionAnalysis);

    assertTrue(emotionAnalysis.isDeleted());
    assertNotNull(emotionAnalysis.getDeletedAt());
  }

  @Test
  void deleteEmotionAnalysis_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.deleteEmotionAnalysis(diaryId, userId));

    // then
    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void deleteEmotionAnalysis_shouldReturnDiaryException_whenDiaryWriterIsNotMatched() {
    // given
    UUID anotherUserId = UUID.randomUUID();
    given(user.getId()).willReturn(anotherUserId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.deleteEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, diaryException.getErrorCode());
  }

  @Test
  void deleteEmotionAnalysis_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    given(user.getId()).willReturn(userId);
    diary.deactivate();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> emotionAnalysisService.deleteEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void deleteEmotionAnalysis_shouldReturnEmotionAnalysisException_whenDiaryIsTemp() {
    // given
    given(user.getId()).willReturn(userId);
    diary.startEditing();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.deleteEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void deleteEmotionAnalysis_shouldReturnEmotionAnalysisException_whenEmotionAnalysisIsNotExist() {
    // given
    given(user.getId()).willReturn(userId);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(Optional.empty());

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.deleteEmotionAnalysis(diaryId, userId));

    assertEquals(ErrorCode.NOT_FOUND_EMOTION_ANALYSIS,
        emotionAnalysisException.getErrorCode());
  }

  @Test
  void successGetEmotionAnalysisByDateRange() {
    // given
    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 10);

    UUID userId = UUID.randomUUID();
    User user = mock(User.class);

    UUID diaryId = UUID.randomUUID();
    Diary diary = mock(Diary.class);

    LocalDateTime now = LocalDateTime.now();
    UUID emotionAnalysisId1 = UUID.randomUUID();
    UUID emotionAnalysisId2 = UUID.randomUUID();

    EmotionAnalysis emotionAnalysis1 = spy(EmotionAnalysis.builder()
        .diary(diary)
        .date(LocalDate.of(2024, 10, 1))
        .primaryEmotion("행복, 여유, 만족")
        .emotionScore(9)
        .analysisContent("행복해보이십니다.")
        .build());

    EmotionAnalysis emotionAnalysis2 = spy(EmotionAnalysis.builder()
        .diary(diary)
        .date(LocalDate.of(2024, 10, 10))
        .primaryEmotion("불안, 초조, 걱정")
        .emotionScore(2)
        .analysisContent("불안해보이십니다.")
        .build());

    Pageable pageable = PageRequest.of(0, 10);

    given(diary.getId()).willReturn(diaryId);
    given(emotionAnalysis1.getId()).willReturn(emotionAnalysisId1);
    given(emotionAnalysis2.getId()).willReturn(emotionAnalysisId2);
    given(emotionAnalysis1.getCreatedAt()).willReturn(now);
    given(emotionAnalysis1.getUpdatedAt()).willReturn(now);
    given(emotionAnalysis2.getCreatedAt()).willReturn(now);
    given(emotionAnalysis2.getUpdatedAt()).willReturn(now);
    given(entityManager.getReference(User.class, userId)).willReturn(user);
    given(
        emotionAnalysisRepository.findByDateBetweenAndIsDeletedFalseAndUser(startDate,
            endDate, user, pageable))
        .willReturn(new SliceImpl<>(Arrays.asList(emotionAnalysis1, emotionAnalysis2)));

    // when
    Slice<EmotionAnalysisResponse> responses = emotionAnalysisService.getEmotionAnalysisByDateRange(
        startDate,
        endDate, userId, pageable);

    // then
    assertEquals(2, responses.getContent().size());
    assertEquals(emotionAnalysisId1,
        responses.getContent().get(0).getEmotionAnalysisId());
    assertEquals(diaryId, responses.getContent().get(0).getDiaryId());
    assertEquals(emotionAnalysis1.getDate(), responses.getContent().get(0).getDate());
    assertEquals(Arrays.stream(
                emotionAnalysis1.getPrimaryEmotion().split(","))
            .map(String::strip).collect(Collectors.toList()),
        responses.getContent().get(0).getPrimaryEmotion());
    assertEquals(emotionAnalysis1.getEmotionScore(),
        responses.getContent().get(0).getEmotionScore());
    assertEquals(emotionAnalysis1.getAnalysisContent(),
        responses.getContent().get(0).getAnalysisContent());
    assertEquals(now, responses.getContent().get(0).getCreatedAt());
    assertEquals(now, responses.getContent().get(0).getUpdatedAt());
    assertEquals(emotionAnalysisId2,
        responses.getContent().get(1).getEmotionAnalysisId());
    assertEquals(diaryId, responses.getContent().get(1).getDiaryId());
    assertEquals(emotionAnalysis2.getDate(), responses.getContent().get(1).getDate());
    assertEquals(Arrays.stream(
                emotionAnalysis2.getPrimaryEmotion().split(","))
            .map(String::strip).collect(Collectors.toList()),
        responses.getContent().get(1).getPrimaryEmotion());
    assertEquals(emotionAnalysis2.getEmotionScore(),
        responses.getContent().get(1).getEmotionScore());
    assertEquals(emotionAnalysis2.getAnalysisContent(),
        responses.getContent().get(1).getAnalysisContent());
    assertEquals(now, responses.getContent().get(1).getCreatedAt());
    assertEquals(now, responses.getContent().get(1).getUpdatedAt());
  }

  @Test
  void getEmotionAnalysisByDateRange_shouldReturnDiaryException_whenStartDateIsBeforeEndDate() {
    // given
    LocalDate startDate = LocalDate.of(2024, 10, 10);
    LocalDate endDate = LocalDate.of(2024, 10, 1);

    Pageable pageable = mock(Pageable.class);
    UUID userId = mock(UUID.class);

    // when & then
    EmotionAnalysisException emotionAnalysisException = assertThrows(
        EmotionAnalysisException.class,
        () -> emotionAnalysisService.getEmotionAnalysisByDateRange(startDate, endDate,
            userId, pageable));

    assertEquals(ErrorCode.START_DATE_MUST_BE_BEFORE_END_DATE,
        emotionAnalysisException.getErrorCode());
  }

}