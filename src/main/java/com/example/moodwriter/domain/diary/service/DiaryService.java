package com.example.moodwriter.domain.diary.service;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryAutoSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.util.Optional;
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
  public DiaryResponse autoSaveDiary(UUID diaryId, UUID userId, DiaryAutoSaveRequest request) {
    Diary diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new DiaryException(ErrorCode.NOT_FOUND_DIARY));

    if (!diary.getUser().getId().equals(userId)) {
      throw new DiaryException(ErrorCode.FORBIDDEN_AUTO_SAVE_DIARY);
    }

    if (diary.isDeleted()) {
      throw new DiaryException(ErrorCode.ALREADY_DELETED_DIARY);
    }

    if (!diary.isTemp()) {
      throw new DiaryException(ErrorCode.CONFLICT_DIARY_STATE);
    }

    diary.autoSave(request);

    Diary savedDiary = diaryRepository.save(diary);

    return DiaryResponse.fromEntity(savedDiary);
  }

}
