package com.example.moodwriter.global.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.dto.FileDto;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class S3FileServiceTest {

  @Mock
  private AmazonS3Client amazonS3Client;

  @InjectMocks
  private S3FileService s3FileService;

  private final String bucketName = "test-bucket";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(s3FileService, "bucketName", bucketName);
  }

  @Test
  void successUploadFile() throws MalformedURLException {
    // given
    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "test-image.png",
        "image/png",
        "dummy image content".getBytes()
    );

    FilePath path = FilePath.PROFILE;

    String expectedUrl =
        "https://s3.amazonaws.com" + bucketName + "/" + path + "/" + UUID.randomUUID()
            + ".png";
    given(amazonS3Client.getUrl(anyString(), anyString())).willReturn(
        new URL(expectedUrl));

    // when
    FileDto result = s3FileService.uploadFile(mockMultipartFile, path);

    // then
    assertNotNull(result);
    assertEquals(expectedUrl, result.getUrl());
    assertTrue(result.getFilename().contains(".png"));

    ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor.forClass(
        PutObjectRequest.class);
    verify(amazonS3Client).putObject(putObjectRequestArgumentCaptor.capture());

    PutObjectRequest captureRequest = putObjectRequestArgumentCaptor.getValue();
    assertEquals(bucketName, captureRequest.getBucketName());
    assertTrue(captureRequest.getKey().contains(".png"));
  }

  @Test
  void successUploadManyFiles() throws MalformedURLException {
    // given
    MockMultipartFile mockMultipartFile1 = new MockMultipartFile(
        "file1", "test-image1.png", "image/png",
        "dummy image content 1".getBytes());

    MockMultipartFile mockMultipartFile2 = new MockMultipartFile(
        "file2", "test-image2.png", "image/png",
        "dummy image content 2".getBytes());

    FilePath path = FilePath.PROFILE;

    given(amazonS3Client.getUrl(anyString(), anyString()))
        .willReturn(new URL("http://test-url"));

    // when
    List<FileDto> result = s3FileService.uploadManyFiles(
        List.of(mockMultipartFile1, mockMultipartFile2), path);

    // then
    assertEquals(2, result.size());
    verify(amazonS3Client, times(2)).putObject(any(PutObjectRequest.class));
  }

  @Test
  void FailUploadFileWhenFileIsEmpty() {
    // given
    MockMultipartFile emptyMultipartFile = new MockMultipartFile(
        "file", "test", "image/png", new byte[0]);

    FilePath path = FilePath.PROFILE;

    // when
    FileDto result = s3FileService.uploadFile(emptyMultipartFile, path);

    // then
    assertNull(result);
    verify(amazonS3Client, never()).putObject(any(PutObjectRequest.class));
  }

  @Test
  void FailUploadFileWhenFileIsNull() {
    // given
    MockMultipartFile nullMultipartFile = null;

    FilePath path = FilePath.PROFILE;

    // when
    FileDto result = s3FileService.uploadFile(nullMultipartFile, path);

    // then
    assertNull(result);
    verify(amazonS3Client, never()).putObject(any(PutObjectRequest.class));
  }

  @Test
  void SuccessUploadManyFileWhenFileListIncludesEmptyAndNullFile()
      throws MalformedURLException {
    // given
    MockMultipartFile mockMultipartFile = new MockMultipartFile(
        "file",
        "test-image.png",
        "image/png",
        "dummy image content".getBytes()
    );
    MockMultipartFile emptyMultipartFile = new MockMultipartFile(
        "file", "test", "image/png", new byte[0]);
    MockMultipartFile nullMultipartFile = null;

    List<MultipartFile> mockMultipartFileList = new ArrayList<>();
    mockMultipartFileList.add(mockMultipartFile);
    mockMultipartFileList.add(emptyMultipartFile);
    mockMultipartFileList.add(nullMultipartFile);

    FilePath path = FilePath.PROFILE;

    String expectedUrl = "http://test-url";

    given(amazonS3Client.getUrl(anyString(), anyString()))
        .willReturn(new URL(expectedUrl));

    // when
    List<FileDto> result = s3FileService.uploadManyFiles(mockMultipartFileList, path);

    // then
    assertEquals(3, result.size());
    assertEquals(expectedUrl, result.get(0).getUrl());
    assertTrue(result.get(0).getFilename().contains(".png"));
    assertNull(result.get(1));
    assertNull(result.get(2));
  }

  @Test
  void successDeleteFile() {
    // given
    String filename = "test-image.png";

    // when
    s3FileService.deleteFile(filename);

    // then
    verify(amazonS3Client).deleteObject(bucketName, filename);
  }

  @Test
  void successDeleteManyFile() {
    // given
    FileDto file1 = FileDto.builder()
        .filename("filename1")
        .build();

    FileDto file2 = FileDto.builder()
        .filename("filename2")
        .build();

    List<FileDto> savedFile = List.of(file1, file2);

    // when
    s3FileService.deleteManyFile(savedFile);

    // then
    verify(amazonS3Client).deleteObject(bucketName, file1.getFilename());
    verify(amazonS3Client).deleteObject(bucketName, file2.getFilename());
  }
}