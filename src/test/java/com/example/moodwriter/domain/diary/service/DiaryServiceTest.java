package com.example.moodwriter.domain.diary.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.global.exception.code.ErrorCode;
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

}