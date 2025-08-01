package com.example.moodwriter.domain.diary.service;

import com.example.moodwriter.domain.diary.dao.DiaryMediaRepository;
import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryImageDeleteRequest;
import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.entity.DiaryMedia;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.s3.dto.FileDto;
import com.example.moodwriter.global.s3.service.S3FileService;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DiaryMediaService {

  private final S3FileService s3FileService;
  private final DiaryMediaRepository diaryMediaRepository;
  private final DiaryRepository diaryRepository;
  private final EntityManager entityManager;

  @Transactional
  public DiaryImageUploadResponse uploadDiaryImages(UUID diaryId, UUID userId,
      List<MultipartFile> diaryImages) {

    User userProxy = entityManager.getReference(User.class, userId);

    Diary diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new DiaryException(ErrorCode.NOT_FOUND_DIARY));

    List<FileDto> fileDtoList = s3FileService.uploadManyFiles(diaryImages,
        FilePath.DIARY);

    for (FileDto fileDto : fileDtoList) {
      DiaryMedia diaryMedia = DiaryMedia.of(fileDto, userProxy, diary);
      diaryMediaRepository.save(diaryMedia);
    }

    return DiaryImageUploadResponse.of(fileDtoList);
  }

  @Transactional
  public void deleteDiaryImage(UUID diaryId, UUID userId, DiaryImageDeleteRequest request) {

    for (String imageUrl : request.getImageUrls()) {
      String filename = "DIARY/" + StringUtils.getFilename(imageUrl);

      DiaryMedia diaryMedia = diaryMediaRepository.findByFileName(filename)
          .orElseThrow(() -> new DiaryException(ErrorCode.NOT_FOUND_DIARY_MEDIA));

      if (!diaryMedia.getUser().getId().equals(userId)) {
        throw new DiaryException(ErrorCode.FORBIDDEN_DELETE_MEDIA);
      }

      if (!diaryMedia.getDiary().getId().equals(diaryId)) {
        throw new DiaryException(ErrorCode.CONFLICT_DIARY_MEDIA);
      }

      s3FileService.deleteFile(filename);
      diaryMediaRepository.delete(diaryMedia);
    }
  }

}
