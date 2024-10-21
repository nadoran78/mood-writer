package com.example.moodwriter.domain.diary.service;

import static org.junit.jupiter.api.Assertions.*;
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
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private DiaryRepository diaryRepository;

  @InjectMocks
  private DiaryService diaryService;

  @Test
  void successCreateDiary() {
    // given
    UUID userId = UUID.randomUUID();

    DiaryCreateRequest request = DiaryCreateRequest.builder()
        .title("임시 제목")
        .content("임시 내용")
        .build();

    User user = mock(User.class);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(diaryRepository.save(any(Diary.class))).will(returnsFirstArg());

    // when
    DiaryResponse response = diaryService.createDiary(userId, request);

    // then
    ArgumentCaptor<Diary> argumentCaptor = ArgumentCaptor.forClass(Diary.class);
    verify(diaryRepository).save(argumentCaptor.capture());
    assertEquals(user, argumentCaptor.getValue().getUser());
    assertFalse(argumentCaptor.getValue().isDeleted());

    assertEquals(request.getTitle(), response.getTitle());
    assertEquals(request.getContent(), response.getContent());
    assertTrue(response.isTemp());
  }

  @Test
  void createDiary_shouldReturnUserException_whenUserIsNotExist() {
    // given
    UUID userId = UUID.randomUUID();

    DiaryCreateRequest request = DiaryCreateRequest.builder()
        .title("임시 제목")
        .content("임시 내용")
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
        .title("임시 저장 제목")
        .content("임시 저장 내용")
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

    // when
    DiaryResponse response = diaryService.autoSaveDiary(diaryId, userId, request);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(request.getTitle(), response.getTitle());
    assertEquals(request.getContent(), response.getContent());
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
        .title("최종 저장 제목")
        .content("최종 저장 내용")
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

    // when
    DiaryResponse response = diaryService.finalSaveDiary(diaryId, userId, request);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(request.getTitle(), response.getTitle());
    assertEquals(request.getContent(), response.getContent());
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
        .title("제목")
        .content("내용")
        .isTemp(false)
        .isDeleted(false)
        .build());
    given(diary.getId()).willReturn(diaryId);
    given(diary.getCreatedAt()).willReturn(now);
    given(diary.getUpdatedAt()).willReturn(now);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(diaryRepository.save(diary)).will(returnsFirstArg());

    // when
    DiaryResponse response = diaryService.startEditingDiary(diaryId, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getTitle(), response.getTitle());
    assertEquals(diary.getContent(), response.getContent());
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
        .title("제목")
        .content("내용")
        .isTemp(false)
        .isDeleted(false)
        .build());
    given(diary.getId()).willReturn(diaryId);
    given(diary.getCreatedAt()).willReturn(now);
    given(diary.getUpdatedAt()).willReturn(now);

    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));

    // when
    DiaryResponse response = diaryService.getDiary(diaryId, userId);

    // then
    assertEquals(diaryId, response.getDiaryId());
    assertEquals(diary.getTitle(), response.getTitle());
    assertEquals(diary.getContent(), response.getContent());
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
        .title("제목")
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
}