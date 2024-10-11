package com.example.moodwriter.diary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.domain.diary.service.DiaryImageService;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.service.S3FileService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DiaryImageServiceTest {

  @Mock
  private S3FileService s3FileService;

  @InjectMocks
  private DiaryImageService diaryImageService;

  @Test
  void successUploadDiaryImages() {
    // given
    MockMultipartFile mockImage1 = mock(MockMultipartFile.class);
    MockMultipartFile mockImage2 = mock(MockMultipartFile.class);

    List<FileDto> fileDtoList = List.of(
        new FileDto("https://example.com/image1", "image1.jpg"),
        new FileDto("https://example.com/image2", "image2.jpg"));

    given(s3FileService.uploadManyFiles(List.of(mockImage1, mockImage2), FilePath.DIARY))
        .willReturn(fileDtoList);

    // when
    DiaryImageUploadResponse response = diaryImageService.uploadDiaryImages(
        List.of(mockImage1, mockImage2));

    // then
    assertEquals(fileDtoList.get(0).getUrl(), response.getImageUrls().get(0));
    assertEquals(fileDtoList.get(1).getUrl(), response.getImageUrls().get(1));
    assertEquals("이미지가 성공적으로 업로드되었습니다.", response.getMessage());
  }
}