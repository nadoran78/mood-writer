package com.example.moodwriter.user.dto;

import com.example.moodwriter.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegisterResponse {

  private UUID id;
  private String email;
  private String name;
  private String profilePictureUrl;
  private LocalDateTime createdAt;

  @Builder
  public static UserRegisterResponse fromEntity(User user) {
    return UserRegisterResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .profilePictureUrl(user.getProfilePictureUrl())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
