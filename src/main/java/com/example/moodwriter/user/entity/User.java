package com.example.moodwriter.user.entity;

import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.entity.BaseEntity;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import com.example.moodwriter.user.entity.converter.FileDtoStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(nullable = false)
  private String name;

  @Column(name = "profile_picture_url")
  @Convert(converter = FileDtoStringConverter.class)
  private List<FileDto> profilePictureUrl;

  @Enumerated(value = EnumType.STRING)
  private Role role;

  @Builder
  public User(String email, String passwordHash, String name, List<FileDto> profilePictureUrl, Role role) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.profilePictureUrl = profilePictureUrl;
    this.role = role;
  }

  public static User from(UserRegisterRequest request, String passwordHash,
      List<FileDto> profilePictureUrl) {
    return User.builder()
        .email(request.getEmail())
        .passwordHash(passwordHash)
        .name(request.getName())
        .profilePictureUrl(profilePictureUrl)
        .role(Role.ROLE_USER)
        .build();
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateProfileImage(List<FileDto> images) {
    this.profilePictureUrl = images;
  }
}
