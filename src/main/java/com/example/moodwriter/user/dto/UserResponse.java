package com.example.moodwriter.user.dto;

import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

  private UUID id;
  private String email;
  private String name;
  private List<FileDto> profilePictureUrl;
  private LocalDateTime createdAt;

  @Builder
  public static UserResponse fromEntity(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .profilePictureUrl(user.getProfilePictureUrl())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
