package com.example.moodwriter.domain.user.entity;

import com.example.moodwriter.domain.user.dto.UserRegisterRequest;
import com.example.moodwriter.domain.user.entity.converter.FileDtoStringConverter;
import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

  @Column(name = "profile_picture_url", columnDefinition = "TEXT")
  @Convert(converter = FileDtoStringConverter.class)
  private List<FileDto> profilePictureUrl;

  @Enumerated(value = EnumType.STRING)
  private Role role;

  @Column(name = "is_deleted")
  private boolean isDeleted;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public User(String email, String passwordHash, String name,
      List<FileDto> profilePictureUrl, Role role, boolean isDeleted,
      LocalDateTime deletedAt) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.profilePictureUrl = profilePictureUrl;
    this.role = role;
    this.isDeleted = isDeleted;
    this.deletedAt = deletedAt;
  }

  public static User from(UserRegisterRequest request, String passwordHash,
      List<FileDto> profilePictureUrl) {
    return User.builder()
        .email(request.getEmail())
        .passwordHash(passwordHash)
        .name(request.getName())
        .profilePictureUrl(profilePictureUrl)
        .role(Role.ROLE_USER)
        .isDeleted(false)
        .build();
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateProfileImage(List<FileDto> images) {
    this.profilePictureUrl = images;
  }

  public void deactivateUser(LocalDateTime deletedAt) {
    this.isDeleted = true;
    this.deletedAt = deletedAt;
  }

  public void reactivate(UserRegisterRequest request, String encryptedPassword,
      List<FileDto> profilePictureUrl) {
    this.passwordHash = encryptedPassword;
    this.name = request.getName();
    this.profilePictureUrl = profilePictureUrl;
    this.role = Role.ROLE_USER;
    this.isDeleted = false;
  }
}
