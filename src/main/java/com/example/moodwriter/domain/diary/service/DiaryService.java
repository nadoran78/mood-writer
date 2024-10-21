package com.example.moodwriter.domain.diary.service;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryService {

  private final UserRepository userRepository;
  private final DiaryRepository diaryRepository;

  @Transactional
  public DiaryResponse createDiary(UUID userId, DiaryCreateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

    Diary diary = Diary.from(user, request);

    Diary savedDiary = diaryRepository.save(diary);

    return DiaryResponse.fromEntity(savedDiary);
  }

  @Transactional
  public DiaryResponse autoSaveDiary(UUID diaryId, UUID userId,
      DiaryAutoSaveRequest request) {
    Diary diary = checkValidAndTempDiary(diaryId, userId);

    diary.autoSave(request);

    Diary savedDiary = diaryRepository.save(diary);

    return DiaryResponse.fromEntity(savedDiary);
  }

  @Transactional
  public DiaryResponse finalSaveDiary(UUID diaryId, UUID userId,
      DiaryFinalSaveRequest request) {
    Diary diary = checkValidAndTempDiary(diaryId, userId);

    diary.finalSave(request);

    Diary savedDiary = diaryRepository.save(diary);

    return DiaryResponse.fromEntity(savedDiary);
  }

  @Transactional
  public DiaryResponse startEditingDiary(UUID diaryId, UUID userId) {
    Diary diary = getCheckedValidDiary(diaryId, userId);

    if (diary.isTemp()) {
      throw new DiaryException(ErrorCode.CONFLICT_DIARY_STATE);
    }

    diary.startEditing();

    Diary savedDiary = diaryRepository.save(diary);

    return DiaryResponse.fromEntity(savedDiary);
  }

  @Transactional(readOnly = true)
  public DiaryResponse getDiary(UUID diaryId, UUID userId) {
    Diary diary = getCheckedValidDiary(diaryId, userId);

    return DiaryResponse.fromEntity(diary);
  }

  @Transactional
  public void deleteDiary(UUID diaryId, UUID userId) {
    Diary diary = getCheckedValidDiary(diaryId, userId);

    diary.deactivate();

    diaryRepository.save(diary);
  }

  private Diary checkValidAndTempDiary(UUID diaryId, UUID userId) {
    Diary diary = getCheckedValidDiary(diaryId, userId);

    if (!diary.isTemp()) {
      throw new DiaryException(ErrorCode.CONFLICT_DIARY_STATE);
    }

    return diary;
  }

  private Diary getCheckedValidDiary(UUID diaryId, UUID userId) {
    Diary diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new DiaryException(ErrorCode.NOT_FOUND_DIARY));

    if (!diary.getUser().getId().equals(userId)) {
      throw new UserException(ErrorCode.FORBIDDEN_ACCESS_DIARY);
    }

    if (diary.isDeleted()) {
      throw new DiaryException(ErrorCode.ALREADY_DELETED_DIARY);
    }

    return diary;
  }

}
