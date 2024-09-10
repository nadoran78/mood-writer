package com.example.moodwriter.global.validation.annotation;

import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.validation.validator.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface ValidFile {

  String message() default "유효한 파일이 아닙니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  FileType[] allowFileType() default {};

}
