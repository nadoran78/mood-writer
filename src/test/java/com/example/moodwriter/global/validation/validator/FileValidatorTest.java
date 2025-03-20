package com.example.moodwriter.global.validation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.validation.annotation.ValidFile;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

class FileValidatorTest {

  private final FileValidator fileValidator = new FileValidator();

  private final ValidFile validFileAnnotation = createValidFile();
  @Mock
  private ConstraintValidatorContext context;

  @Test
  void successWithValidFile() {
    MultipartFile imageFile1 = mock(MultipartFile.class);
    MultipartFile imageFile2 = mock(MultipartFile.class);

    when(imageFile1.getContentType()).thenReturn("image/jpeg");
    when(imageFile2.getContentType()).thenReturn("image/png");

    List<MultipartFile> validFiles = List.of(imageFile1, imageFile2);
    assertTrue(fileValidator.isValid(validFiles, context));
  }

  @BeforeEach
  void setUp() {
    fileValidator.initialize(validFileAnnotation);
  }

  @Test
  void failWithInvalidFile() {
    MultipartFile imageFile = mock(MultipartFile.class);
    MultipartFile textFile = mock(MultipartFile.class);

    when(imageFile.getContentType()).thenReturn("image/jpeg");
    when(textFile.getContentType()).thenReturn("text/txt");

    List<MultipartFile> validFiles = List.of(imageFile, textFile);
    assertFalse(fileValidator.isValid(validFiles, context));
  }

  @Test
  void successWithFileListIsEmpty() {
    List<MultipartFile> validFiles = new ArrayList<>();
    assertTrue(fileValidator.isValid(validFiles, context));
  }

  @Test
  void successWithFileListIsNull() {
    assertTrue(fileValidator.isValid(null, context));
  }

  @Test
  void failWithFileContentTypeIsNull() {
    MultipartFile imageFile = mock(MultipartFile.class);
    MultipartFile nullFile = mock(MultipartFile.class);

    when(imageFile.getContentType()).thenReturn("image/jpeg");
    when(nullFile.getContentType()).thenReturn(null);

    List<MultipartFile> validFiles = List.of(imageFile, nullFile);
    assertFalse(fileValidator.isValid(validFiles, context));
  }

  @Test
  void failWithFileIsNull() {
    MultipartFile imageFile = mock(MultipartFile.class);

    when(imageFile.getContentType()).thenReturn("image/jpeg");

    List<MultipartFile> validFiles = new ArrayList<>();
    validFiles.add(imageFile);
    validFiles.add(null);

    assertFalse(fileValidator.isValid(validFiles, context));
  }

  @Test
  void successWithValidFileAndContentTypeIsUpperCase() {
    MultipartFile imageFile1 = mock(MultipartFile.class);
    MultipartFile imageFile2 = mock(MultipartFile.class);

    when(imageFile1.getContentType()).thenReturn("IMAGE/JPEG");
    when(imageFile2.getContentType()).thenReturn("IMAGE/PNG");

    List<MultipartFile> validFiles = List.of(imageFile1, imageFile2);
    assertTrue(fileValidator.isValid(validFiles, context));
  }

  private ValidFile createValidFile() {
    return new ValidFile() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return ValidFile.class;
      }

      @Override
      public String message() {
        return "유효한 파일이 아닙니다.";
      }

      @Override
      public Class<?>[] groups() {
        return new Class[0];
      }

      @Override
      public Class<? extends Payload>[] payload() {
        return new Class[0];
      }

      @Override
      public FileType[] allowFileType() {
        return new FileType[]{FileType.IMAGE};
      }
    };
  }

}