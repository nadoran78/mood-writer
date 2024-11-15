package com.example.moodwriter.domain.diary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryAutoSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryFinalSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.emotion.dao.EmotionAnalysisRepository;
import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private DiaryRepository diaryRepository;

  @Mock
  private EmotionAnalysisRepository emotionAnalysisRepository;

  @InjectMocks
  private DiaryService diaryService;

  @Test
  void successCreateDiary() {
    // given
    UUID userId = UUID.randomUUID();

    DiaryCreateRequest request = DiaryCreateRequest.builder()
        .content("임시 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    User user = mock(User.class);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(diaryRepository.save(any(Diary.class))).will(returnsFirstArg());
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    DiaryResponse response = diaryService.createDiary(userId, request);

    // then
    ArgumentCaptor<Diary> argumentCaptor = ArgumentCaptor.forClass(Diary.class);
    verify(diaryRepository).save(argumentCaptor.capture());
    assertEquals(user, argumentCaptor.getValue().getUser());
    assertFalse(argumentCaptor.getValue().isDeleted());

    assertFalse(response.isHaveEmotionAnalysis());
    assertEquals(request.getContent(), response.getContent());
    assertEquals(request.getDate(), response.getDate());
    assertTrue(response.isTemp());
  }

  @Test
  void successCreateDiary_whenRequestIsNull() {
    // given
    UUID userId = UUID.randomUUID();

    DiaryCreateRequest request = null;

    User user = mock(User.class);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(diaryRepository.save(any(Diary.class))).will(returnsFirstArg());
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    DiaryResponse response = diaryService.createDiary(userId, request);

    // then
    ArgumentCaptor<Diary> argumentCaptor = ArgumentCaptor.forClass(Diary.class);
    verify(diaryRepository).save(argumentCaptor.capture());
    assertEquals(user, argumentCaptor.getValue().getUser());
    assertFalse(argumentCaptor.getValue().isDeleted());

    assertFalse(response.isHaveEmotionAnalysis());
    assertNull(response.getContent());
    assertNull(response.getDate());
    assertTrue(response.isTemp());
  }

  @Test
  void createDiary_shouldReturnUserException_whenUserIsNotExist() {
    // given
    UUID userId = UUID.randomUUID();

    DiaryCreateRequest request = DiaryCreateRequest.builder()
        .content("임시 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.createDiary(userId, request));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void successAutoSaveDiary() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    DiaryAutoSaveRequest request = DiaryAutoSaveRequest.builder()
        .content("임시 저장 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    LocalDateTime now = LocalDateTime.now();
    Diary diary = spy(Diary.builder()
        .user(user)
        .isTemp(true)
        .isDeleted(false)
        .build());
    given(diary.getId()).willReturn(diaryId);
    given(diary.getCreatedAt()).willReturn(now);
    given(diary.getUpdatedAt()).willReturn(now);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(diaryRepository.save(diary)).will(returnsFirstArg());
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    DiaryResponse response = diaryService.autoSaveDiary(diaryId, userId, request);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertFalse(response.isHaveEmotionAnalysis());
    assertEquals(request.getContent(), response.getContent());
    assertEquals(request.getDate(), response.getDate());
    assertTrue(response.isTemp());
    assertEquals(now, response.getCreatedAt());
    assertEquals(now, response.getUpdatedAt());
  }

  @Test
  void autoSaveDiary_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = mock(UUID.class);
    DiaryAutoSaveRequest request = mock(DiaryAutoSaveRequest.class);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.autoSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void autoSaveDiary_shouldReturnUserException_whenUserIsNotMatch() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();
    DiaryAutoSaveRequest request = mock(DiaryAutoSaveRequest.class);

    User user = mock(User.class);
    UUID invalidUserId = UUID.randomUUID();
    given(user.getId()).willReturn(invalidUserId);

    Diary diary = Diary.builder()
        .user(user)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.autoSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, userException.getErrorCode());
  }

  @Test
  void autoSaveDiary_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();
    DiaryAutoSaveRequest request = mock(DiaryAutoSaveRequest.class);

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .isDeleted(true)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.autoSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void autoSaveDiary_shouldReturnDiaryException_whenDiaryIsNotTemp() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();
    DiaryAutoSaveRequest request = mock(DiaryAutoSaveRequest.class);

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .isDeleted(false)
        .isTemp(false)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.autoSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.CONFLICT_DIARY_STATE, diaryException.getErrorCode());
  }

  @Test
  void successFinalSaveDiary() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    DiaryFinalSaveRequest request = DiaryFinalSaveRequest.builder()
        .content("최종 저장 내용")
        .date(LocalDate.of(2024, 10, 1))
        .build();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    LocalDateTime now = LocalDateTime.now();
    Diary diary = spy(Diary.builder()
        .user(user)
        .isTemp(true)
        .isDeleted(false)
        .build());
    given(diary.getId()).willReturn(diaryId);
    given(diary.getCreatedAt()).willReturn(now);
    given(diary.getUpdatedAt()).willReturn(now);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(diaryRepository.save(diary)).will(returnsFirstArg());
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    DiaryResponse response = diaryService.finalSaveDiary(diaryId, userId, request);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertFalse(response.isHaveEmotionAnalysis());
    assertEquals(request.getContent(), response.getContent());
    assertEquals(request.getDate(), response.getDate());
    assertFalse(response.isTemp());
    assertEquals(now, response.getCreatedAt());
    assertEquals(now, response.getUpdatedAt());
  }

  @Test
  void finalSaveDiary_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = mock(UUID.class);
    DiaryFinalSaveRequest request = mock(DiaryFinalSaveRequest.class);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.finalSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void finalSaveDiary_shouldReturnUserException_whenUserIsNotMatch() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();
    DiaryFinalSaveRequest request = mock(DiaryFinalSaveRequest.class);

    User user = mock(User.class);
    UUID invalidUserId = UUID.randomUUID();
    given(user.getId()).willReturn(invalidUserId);

    Diary diary = Diary.builder()
        .user(user)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.finalSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, userException.getErrorCode());
  }

  @Test
  void finalSaveDiary_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();
    DiaryFinalSaveRequest request = mock(DiaryFinalSaveRequest.class);

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .isDeleted(true)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.finalSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void finalSaveDiary_shouldReturnDiaryException_whenDiaryIsNotTemp() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();
    DiaryFinalSaveRequest request = mock(DiaryFinalSaveRequest.class);

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .isDeleted(false)
        .isTemp(false)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.finalSaveDiary(diaryId, userId, request));

    assertEquals(ErrorCode.CONFLICT_DIARY_STATE, diaryException.getErrorCode());
  }

  @Test
  void successStartEditingDiary() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    LocalDateTime now = LocalDateTime.now();
    Diary diary = spy(Diary.builder()
        .user(user)
        .content("내용")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .isDeleted(false)
        .build());
    given(diary.getId()).willReturn(diaryId);
    given(diary.getCreatedAt()).willReturn(now);
    given(diary.getUpdatedAt()).willReturn(now);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(diaryRepository.save(diary)).will(returnsFirstArg());
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    DiaryResponse response = diaryService.startEditingDiary(diaryId, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertFalse(response.isHaveEmotionAnalysis());
    assertEquals(diary.getContent(), response.getContent());
    assertEquals(diary.getDate(), response.getDate());
    assertTrue(response.isTemp());
    assertEquals(now, response.getCreatedAt());
    assertEquals(now, response.getUpdatedAt());
  }

  @Test
  void startEditingDiary_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.startEditingDiary(diaryId, userId));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void startEditingDiary_shouldReturnUserException_whenUserIsNotMatch() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    UUID invalidUserId = UUID.randomUUID();
    given(user.getId()).willReturn(invalidUserId);

    Diary diary = Diary.builder()
        .user(user)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.startEditingDiary(diaryId, userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, userException.getErrorCode());
  }

  @Test
  void startEditingDiary_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .isDeleted(true)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.startEditingDiary(diaryId, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void startEditingDiary_shouldReturnDiaryException_whenDiaryIsTemp() {
    // given
    UUID diaryId = mock(UUID.class);
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .isDeleted(false)
        .isTemp(true)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.startEditingDiary(diaryId, userId));

    assertEquals(ErrorCode.CONFLICT_DIARY_STATE, diaryException.getErrorCode());
  }

  @Test
  void successGetDiary() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    LocalDateTime now = LocalDateTime.now();
    Diary diary = spy(Diary.builder()
        .user(user)
        .content("내용")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .isDeleted(false)
        .build());
    given(diary.getId()).willReturn(diaryId);
    given(diary.getCreatedAt()).willReturn(now);
    given(diary.getUpdatedAt()).willReturn(now);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    DiaryResponse response = diaryService.getDiary(diaryId, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertFalse(response.isHaveEmotionAnalysis());
    assertEquals(diary.getContent(), response.getContent());
    assertEquals(diary.getDate(), response.getDate());
    assertEquals(diary.isTemp(), response.isTemp());
    assertEquals(now, response.getCreatedAt());
    assertEquals(now, response.getUpdatedAt());
  }

  @Test
  void getDiary_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.getDiary(diaryId, userId));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void getDiary_shouldReturnUserException_UserIsNotMatched() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    UUID anotherUserId = UUID.randomUUID();
    given(user.getId()).willReturn(anotherUserId);

    Diary diary = Diary.builder()
        .user(user)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.getDiary(diaryId, userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, userException.getErrorCode());
  }

  @Test
  void getDiary_shouldReturnDiaryException_whenDiaryIsDeleted() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = spy(Diary.builder()
        .user(user)
        .content("내용")
        .isTemp(false)
        .isDeleted(true)
        .build());

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.getDiary(diaryId, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void successDeleteDiary_whenEmotionAnalysisIsNotExist() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .content("내용")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .isDeleted(false)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(Optional.empty());

    // when
    diaryService.deleteDiary(diaryId, userId);

    // then
    assertTrue(diary.isDeleted());
    assertNotNull(diary.getDeletedAt());

    verify(diaryRepository).save(diary);
  }

  @Test
  void successDeleteDiary_whenEmotionAnalysisIsExist() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .content("내용")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .isDeleted(false)
        .build();

    EmotionAnalysis emotionAnalysis = EmotionAnalysis.builder()
        .isDeleted(false)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(emotionAnalysisRepository.findByDiary(diary)).willReturn(
        Optional.of(emotionAnalysis));

    // when
    diaryService.deleteDiary(diaryId, userId);

    // then
    assertTrue(diary.isDeleted());
    assertNotNull(diary.getDeletedAt());

    verify(diaryRepository).save(diary);

    assertTrue(emotionAnalysis.isDeleted());
    assertNotNull(emotionAnalysis.getDeletedAt());
  }

  @Test
  void deleteDiary_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.deleteDiary(diaryId, userId));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void deleteDiary_shouldReturnUserException_whenUserIsNotMatched() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    UUID anotherUserId = UUID.randomUUID();
    given(user.getId()).willReturn(anotherUserId);

    Diary diary = Diary.builder()
        .user(user)
        .content("내용")
        .isTemp(false)
        .isDeleted(false)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.deleteDiary(diaryId, userId));

    assertEquals(ErrorCode.FORBIDDEN_ACCESS_DIARY, userException.getErrorCode());
  }

  @Test
  void deleteDiary_shouldReturnDiaryException_whenDiaryIsAlreadyDeleted() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = spy(Diary.builder()
        .user(user)
        .content("내용")
        .isTemp(false)
        .isDeleted(true)
        .build());

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.deleteDiary(diaryId, userId));

    assertEquals(ErrorCode.ALREADY_DELETED_DIARY, diaryException.getErrorCode());
  }

  @Test
  void deleteDiary_shouldReturnDiaryException_whenDiaryIsTemp() {
    // given
    UUID diaryId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    Diary diary = Diary.builder()
        .user(user)
        .content("내용")
        .isTemp(true)
        .isDeleted(false)
        .build();

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.deleteDiary(diaryId, userId));

    assertEquals(ErrorCode.CONFLICT_DIARY_STATE, diaryException.getErrorCode());
  }

  @Test
  void successGetDiariesByDateRange() {
    // given
    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 10);

    UUID userId = UUID.randomUUID();

    User user = mock(User.class);

    LocalDateTime now = LocalDateTime.now();
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    Diary diary1 = spy(Diary.builder()
        .content("content")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .build());

    Diary diary2 = spy(Diary.builder()
        .content("content2")
        .date(LocalDate.of(2024, 10, 10))
        .isTemp(false)
        .build());

    Pageable pageable = PageRequest.of(0, 10);

    given(diary1.getId()).willReturn(diaryId1);
    given(diary2.getId()).willReturn(diaryId2);
    given(diary1.getCreatedAt()).willReturn(now);
    given(diary1.getUpdatedAt()).willReturn(now);
    given(diary2.getCreatedAt()).willReturn(now);
    given(diary2.getUpdatedAt()).willReturn(now);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(
        diaryRepository.findByDateBetweenAndIsDeletedFalseAndIsTempFalseAndUser(startDate,
            endDate, user, pageable))
        .willReturn(new SliceImpl<>(Arrays.asList(diary1, diary2)));
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    Slice<DiaryResponse> responses = diaryService.getDiariesByDateRange(startDate,
        endDate, pageable, userId);

    // then
    assertEquals(2, responses.getContent().size());
    assertEquals(diaryId1, responses.getContent().get(0).getDiaryId());
    assertFalse(responses.getContent().get(0).isHaveEmotionAnalysis());
    assertEquals(diary1.getContent(), responses.getContent().get(0).getContent());
    assertEquals(diary1.getDate(), responses.getContent().get(0).getDate());
    assertEquals(diary1.isTemp(), responses.getContent().get(0).isTemp());
    assertEquals(now, responses.getContent().get(0).getCreatedAt());
    assertEquals(now, responses.getContent().get(0).getUpdatedAt());
    assertEquals(diaryId2, responses.getContent().get(1).getDiaryId());
    assertFalse(responses.getContent().get(1).isHaveEmotionAnalysis());
    assertEquals(diary2.getContent(), responses.getContent().get(1).getContent());
    assertEquals(diary2.getDate(), responses.getContent().get(1).getDate());
    assertEquals(diary2.isTemp(), responses.getContent().get(1).isTemp());
    assertEquals(now, responses.getContent().get(0).getCreatedAt());
    assertEquals(now, responses.getContent().get(0).getUpdatedAt());
  }

  @Test
  void getDiariesByDateRange_shouldReturnDiaryException_whenStartDateIsBeforeEndDate() {
    // given
    LocalDate startDate = LocalDate.of(2024, 10, 10);
    LocalDate endDate = LocalDate.of(2024, 10, 1);

    Pageable pageable = mock(Pageable.class);
    UUID userId = mock(UUID.class);

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryService.getDiariesByDateRange(startDate, endDate, pageable, userId));

    assertEquals(ErrorCode.START_DATE_MUST_BE_BEFORE_END_DATE, diaryException.getErrorCode());
  }

  @Test
  void getDiariesByDateRange_shouldReturnUserException_whenUserIsNotExist() {
    // given
    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 10);

    Pageable pageable = mock(Pageable.class);
    UUID userId = mock(UUID.class);

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.getDiariesByDateRange(startDate, endDate, pageable, userId));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void successGetAllMyDiaries() {
    // given
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);

    LocalDateTime now = LocalDateTime.now();
    UUID diaryId1 = UUID.randomUUID();
    UUID diaryId2 = UUID.randomUUID();

    Diary diary1 = spy(Diary.builder()
        .content("content")
        .date(LocalDate.of(2024, 10, 1))
        .isTemp(false)
        .build());

    Diary diary2 = spy(Diary.builder()
        .content("content2")
        .date(LocalDate.of(2024, 10, 10))
        .isTemp(false)
        .build());

    Pageable pageable = PageRequest.of(0, 10);

    given(diary1.getId()).willReturn(diaryId1);
    given(diary2.getId()).willReturn(diaryId2);
    given(diary1.getCreatedAt()).willReturn(now);
    given(diary1.getUpdatedAt()).willReturn(now);
    given(diary2.getCreatedAt()).willReturn(now);
    given(diary2.getUpdatedAt()).willReturn(now);
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(
        diaryRepository.findAllByUserAndIsDeletedFalseAndIsTempFalse(user, pageable))
        .willReturn(new SliceImpl<>(Arrays.asList(diary1, diary2)));
    given(emotionAnalysisRepository.findByDiary(any(Diary.class)))
        .willReturn(Optional.empty());

    // when
    Slice<DiaryResponse> responses = diaryService.getAllMyDiaries(pageable, userId);

    // then
    assertEquals(2, responses.getContent().size());
    assertEquals(diaryId1, responses.getContent().get(0).getDiaryId());
    assertFalse(responses.getContent().get(0).isHaveEmotionAnalysis());
    assertEquals(diary1.getContent(), responses.getContent().get(0).getContent());
    assertEquals(diary1.getDate(), responses.getContent().get(0).getDate());
    assertEquals(diary1.isTemp(), responses.getContent().get(0).isTemp());
    assertEquals(now, responses.getContent().get(0).getCreatedAt());
    assertEquals(now, responses.getContent().get(0).getUpdatedAt());
    assertEquals(diaryId2, responses.getContent().get(1).getDiaryId());
    assertFalse(responses.getContent().get(1).isHaveEmotionAnalysis());
    assertEquals(diary2.getContent(), responses.getContent().get(1).getContent());
    assertEquals(diary2.getDate(), responses.getContent().get(1).getDate());
    assertEquals(diary2.isTemp(), responses.getContent().get(1).isTemp());
    assertEquals(now, responses.getContent().get(0).getCreatedAt());
    assertEquals(now, responses.getContent().get(0).getUpdatedAt());
  }

  @Test
  void getAllMyDiaries_shouldReturnUserException_whenUserIsNotExist() {
    // given
    Pageable pageable = mock(Pageable.class);
    UUID userId = mock(UUID.class);

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryService.getAllMyDiaries(pageable, userId));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }
}