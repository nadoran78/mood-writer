package com.example.moodwriter.user.entity;

import com.example.moodwriter.global.entity.BaseEntity;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
  private String profilePictureUrl;

  @Builder
  public User(String email, String passwordHash, String name, String profilePictureUrl) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.profilePictureUrl = profilePictureUrl;
  }

  public static User from(UserRegisterRequest request, String passwordHash) {
    return User.builder()
        .email(request.getEmail())
        .passwordHash(passwordHash)
        .name(request.getName())
        .build();
  }
}
