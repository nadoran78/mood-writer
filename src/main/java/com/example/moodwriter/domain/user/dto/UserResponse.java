package com.example.moodwriter.domain.user.dto;

import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.dto.FileDto;
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
  private LocalDateTime updatedAt;

  @Builder
  public static UserResponse fromEntity(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .profilePictureUrl(user.getProfilePictureUrl())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
