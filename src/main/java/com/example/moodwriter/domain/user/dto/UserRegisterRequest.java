package com.example.moodwriter.domain.user.dto;

import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.constant.RegexPattern;
import com.example.moodwriter.global.validation.annotation.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class UserRegisterRequest {

  @NotBlank(message = "이메일은 반드시 입력해야 합니다.")
  @Pattern(regexp = RegexPattern.EMAIL, message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호는 반드시 입력해야 합니다.")
  @Pattern(regexp = RegexPattern.PASSWORD, message = "암호는 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하여야 합니다.")
  private String password;

  @NotBlank(message = "이름을 빈칸으로 입력할 수 없습니다.")
  @Size(max = 10, message = "이름은 10자 이하여야 합니다.")
  private String name;

  @ValidFile(allowFileType = FileType.IMAGE)
  @Size(max = 1, message = "프로필 이미지는 1장만 업데이트 가능합니다.")
  private List<MultipartFile> profileImages;
}
