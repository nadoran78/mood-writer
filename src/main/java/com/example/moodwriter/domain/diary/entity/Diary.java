package com.example.moodwriter.domain.diary.entity;

import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity
@Table(name = "diaries")
public class Diary extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(name = "is_temp", nullable = false)
  private boolean isTemp = true;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public Diary(User user, String title, String content, boolean isTemp, boolean isDeleted,
      LocalDateTime deletedAt) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.isTemp = isTemp;
    this.isDeleted = isDeleted;
    this.deletedAt = deletedAt;
  }
}
