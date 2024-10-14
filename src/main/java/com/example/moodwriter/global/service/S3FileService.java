package com.example.moodwriter.global.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3FileService {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public FileDto uploadFile(MultipartFile multipartFile, FilePath path) {
    if (multipartFile == null || multipartFile.isEmpty()) {
      return null;
    }

    try {
      ObjectMetadata objectMetadata = new ObjectMetadata();
      objectMetadata.setContentType(multipartFile.getContentType());
      objectMetadata.setContentLength(multipartFile.getSize());

      String extension = StringUtils.getFilenameExtension(
          multipartFile.getOriginalFilename());
      String filename = path + "/" + UUID.randomUUID() + "." + extension;

      PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename,
          multipartFile.getInputStream(), objectMetadata);

      amazonS3Client.putObject(putObjectRequest);

      String objectUrl = amazonS3Client.getUrl(bucketName, filename).toString();

      return new FileDto(objectUrl, filename, multipartFile.getContentType());

    } catch (IOException e) {
      throw new CustomException(ErrorCode.FAIL_TO_UPLOAD_FILE);
    }
  }

  @Transactional
  public List<FileDto> uploadManyFiles(List<MultipartFile> multipartFiles, FilePath path) {
    return multipartFiles.stream()
        .map(multipartFile -> uploadFile(multipartFile, path))
        .toList();
  }

  public void deleteFile(String filename) {
    amazonS3Client.deleteObject(bucketName, filename);
  }

  public void deleteManyFile(List<FileDto> savedImages) {
    for (FileDto image : savedImages) {
      deleteFile(image.getFilename());
    }
  }
}
