package com.example.moodwriter.domain.diary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.diary.dao.DiaryMediaRepository;
import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.dto.DiaryImageDeleteRequest;
import com.example.moodwriter.domain.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.entity.DiaryMedia;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.diary.service.DiaryMediaService;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.s3.dto.FileDto;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.s3.service.S3FileService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DiaryMediaServiceTest {

  @Mock
  private S3FileService s3FileService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private DiaryMediaRepository diaryMediaRepository;

  @Mock
  private DiaryRepository diaryRepository;

  @InjectMocks
  private DiaryMediaService diaryMediaService;

  @Test
  void successUploadDiaryImages() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    User user = mock(User.class);
    Diary diary = mock(Diary.class);

    MockMultipartFile mockImage1 = mock(MockMultipartFile.class);
    MockMultipartFile mockImage2 = mock(MockMultipartFile.class);

    List<FileDto> fileDtoList = List.of(
        new FileDto("https://example.com/image1", "image1.jpg", "image/jpeg"),
        new FileDto("https://example.com/image2", "image2.jpg", "image/jpeg"));

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(diaryRepository.findById(diaryId)).willReturn(Optional.of(diary));
    given(s3FileService.uploadManyFiles(List.of(mockImage1, mockImage2), FilePath.DIARY))
        .willReturn(fileDtoList);

    // when
    DiaryImageUploadResponse response = diaryMediaService.uploadDiaryImages(
        diaryId, userId, List.of(mockImage1, mockImage2));

    // then
    assertEquals(fileDtoList.get(0).getUrl(), response.getImageUrls().get(0));
    assertEquals(fileDtoList.get(1).getUrl(), response.getImageUrls().get(1));
    assertEquals("이미지가 성공적으로 업로드되었습니다.", response.getMessage());

    ArgumentCaptor<DiaryMedia> argumentCaptor = ArgumentCaptor.forClass(DiaryMedia.class);
    verify(diaryMediaRepository, times(2)).save(argumentCaptor.capture());

    DiaryMedia diaryMedia1 = argumentCaptor.getAllValues().get(0);
    DiaryMedia diaryMedia2 = argumentCaptor.getAllValues().get(1);

    assertEquals(user, diaryMedia1.getUser());
    assertEquals(diary, diaryMedia1.getDiary());
    assertEquals("https://example.com/image1", diaryMedia1.getFileUrl());
    assertEquals("image/jpeg", diaryMedia1.getFileType());
    assertEquals(user, diaryMedia2.getUser());
    assertEquals(diary, diaryMedia2.getDiary());
    assertEquals("https://example.com/image2", diaryMedia2.getFileUrl());
    assertEquals("image/jpeg", diaryMedia2.getFileType());
  }

  @Test
  void uploadDiaryImages_shouldReturnUserException_whenUserIsNotExist() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    MockMultipartFile mockImage1 = mock(MockMultipartFile.class);
    MockMultipartFile mockImage2 = mock(MockMultipartFile.class);

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> diaryMediaService.uploadDiaryImages(
            diaryId, userId, List.of(mockImage1, mockImage2)));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void uploadDiaryImages_shouldReturnDiaryException_whenDiaryIsNotExist() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    User user = mock(User.class);

    MockMultipartFile mockImage1 = mock(MockMultipartFile.class);
    MockMultipartFile mockImage2 = mock(MockMultipartFile.class);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(diaryRepository.findById(diaryId)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryMediaService.uploadDiaryImages(
            diaryId, userId, List.of(mockImage1, mockImage2)));

    assertEquals(ErrorCode.NOT_FOUND_DIARY, diaryException.getErrorCode());
  }

  @Test
  void successDeleteDiaryImage() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    DiaryImageDeleteRequest request = new DiaryImageDeleteRequest(
        List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

    User user = mock(User.class);
    Diary diary = mock(Diary.class);
    DiaryMedia diaryMedia = DiaryMedia.builder()
        .user(user)
        .diary(diary)
        .build();

    String filename1 = "DIARY/image1.jpg";
    String filename2 = "DIARY/image2.jpg";

    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(diaryId);
    given(diaryMediaRepository.findByFileName(filename1)).willReturn(
        Optional.of(diaryMedia));
    given(diaryMediaRepository.findByFileName(filename2)).willReturn(
        Optional.of(diaryMedia));

    // when
    diaryMediaService.deleteDiaryImage(diaryId, userId, request);

    // then
    verify(s3FileService, times(1)).deleteFile(filename1);
    verify(s3FileService, times(1)).deleteFile(filename2);
    verify(diaryMediaRepository, times(2)).delete(diaryMedia);
  }

  @Test
  void deleteDiaryImage_shouldReturnDiaryException_whenDiaryMediaIsNotExist() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    DiaryImageDeleteRequest request = new DiaryImageDeleteRequest(
        List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

    String filename1 = "DIARY/image1.jpg";

    given(diaryMediaRepository.findByFileName(filename1)).willReturn(Optional.empty());

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryMediaService.deleteDiaryImage(diaryId, userId, request));

    assertEquals(ErrorCode.NOT_FOUND_DIARY_MEDIA, diaryException.getErrorCode());
  }

  @Test
  void deleteDiaryImage_shouldReturnDiaryException_whenUserIsNotMatched() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    DiaryImageDeleteRequest request = new DiaryImageDeleteRequest(
        List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

    User user = mock(User.class);
    Diary diary = mock(Diary.class);
    DiaryMedia diaryMedia = DiaryMedia.builder()
        .user(user)
        .diary(diary)
        .build();

    String filename1 = "DIARY/image1.jpg";
    UUID unmatchedUserId = UUID.randomUUID();

    given(user.getId()).willReturn(unmatchedUserId);
    given(diaryMediaRepository.findByFileName(filename1)).willReturn(
        Optional.of(diaryMedia));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryMediaService.deleteDiaryImage(diaryId, userId, request));

    assertEquals(ErrorCode.FORBIDDEN_DELETE_MEDIA, diaryException.getErrorCode());
  }

  @Test
  void deleteDiaryImage_shouldReturnDiaryException_whenDiaryIsNotMatched() {
    // given
    UUID userId = UUID.randomUUID();
    UUID diaryId = UUID.randomUUID();

    DiaryImageDeleteRequest request = new DiaryImageDeleteRequest(
        List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"));

    User user = mock(User.class);
    Diary diary = mock(Diary.class);
    DiaryMedia diaryMedia = DiaryMedia.builder()
        .user(user)
        .diary(diary)
        .build();

    String filename1 = "DIARY/image1.jpg";
    UUID unmatchedDiaryId = UUID.randomUUID();

    given(user.getId()).willReturn(userId);
    given(diary.getId()).willReturn(unmatchedDiaryId);
    given(diaryMediaRepository.findByFileName(filename1)).willReturn(
        Optional.of(diaryMedia));

    // when & then
    DiaryException diaryException = assertThrows(DiaryException.class,
        () -> diaryMediaService.deleteDiaryImage(diaryId, userId, request));

    assertEquals(ErrorCode.CONFLICT_DIARY_MEDIA, diaryException.getErrorCode());
  }
}