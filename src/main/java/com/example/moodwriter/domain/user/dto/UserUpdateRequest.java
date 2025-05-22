package com.example.moodwriter.domain.user.dto;

import com.example.moodwriter.global.constant.FileType;
import com.example.moodwriter.global.constant.RegexPattern;
import com.example.moodwriter.global.validation.annotation.ValidFile;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

  @Pattern(regexp = RegexPattern.NAME, message = "이름의 시작, 끝, 전체를 공백으로 입력할 수 없습니다.")
  @Size(max = 10, message = "이름은 10자 이하여야 합니다.")
  private String name;

}
