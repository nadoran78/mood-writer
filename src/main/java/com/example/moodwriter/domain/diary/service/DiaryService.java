package com.example.moodwriter.domain.diary.service;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.dto.UserResponse;
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

}
