package com.example.moodwriter.domain.diary.service;

import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.service.S3FileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DiaryImageService {

  private final S3FileService s3FileService;

  public DiaryImageUploadResponse uploadDiaryImages(List<MultipartFile> diaryImages) {
    List<FileDto> fileDtoList = s3FileService.uploadManyFiles(diaryImages, FilePath.DIARY);
    return DiaryImageUploadResponse.of(fileDtoList);
  }

}
