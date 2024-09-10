package com.example.moodwriter.global.validation.validator;

import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.validation.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, List<MultipartFile>> {

  private List<FileType> allowedFileTypes;

  @Override
  public void initialize(ValidFile constraintAnnotation) {
    this.allowedFileTypes = Arrays.asList(constraintAnnotation.allowFileType());
  }

  @Override
  public boolean isValid(List<MultipartFile> multipartFiles, ConstraintValidatorContext context) {
    if (multipartFiles == null || multipartFiles.isEmpty()) {
      return true;
    }

    for (MultipartFile file : multipartFiles) {
      if (file == null || file.getContentType() == null) {
        return false;
      }

      for (FileType fileType : allowedFileTypes) {
        if (!fileType.getExtensions().contains(file.getContentType().toLowerCase())) {
          return false;
        }
      }
    }
    return true;
  }
}
