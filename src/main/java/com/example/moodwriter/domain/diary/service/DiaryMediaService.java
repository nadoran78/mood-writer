package com.example.moodwriter.domain.diary.service;

import com.example.moodwriter.domain.diary.dao.DiaryMediaRepository;
import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.entity.DiaryMedia;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.service.S3FileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DiaryMediaService {

  private final S3FileService s3FileService;
  private final UserRepository userRepository;
  private final DiaryMediaRepository diaryMediaRepository;
  private final DiaryRepository diaryRepository;

  @Transactional
  public DiaryImageUploadResponse uploadDiaryImages(UUID diaryId, UUID userId,
      List<MultipartFile> diaryImages) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

    Diary diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new DiaryException(ErrorCode.NOT_FOUND_DIARY));

    List<FileDto> fileDtoList = s3FileService.uploadManyFiles(diaryImages,
        FilePath.DIARY);

    for (FileDto fileDto : fileDtoList) {
      DiaryMedia diaryMedia = DiaryMedia.of(fileDto, user, diary);
      diaryMediaRepository.save(diaryMedia);
    }

    return DiaryImageUploadResponse.of(fileDtoList);
  }

}
