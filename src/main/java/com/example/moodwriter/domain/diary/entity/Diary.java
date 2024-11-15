package com.example.moodwriter.domain.diary.entity;

import com.example.moodwriter.domain.diary.dto.DiaryAutoSaveRequest;
import com.example.moodwriter.domain.diary.dto.DiaryCreateRequest;
import com.example.moodwriter.domain.diary.dto.DiaryFinalSaveRequest;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
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

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDate date;

  @Column(name = "is_temp", nullable = false)
  private boolean isTemp = true;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public Diary(User user, String title, String content, LocalDate date, boolean isTemp,
      boolean isDeleted, LocalDateTime deletedAt) {
    this.user = user;
    this.content = content;
    this.date = date;
    this.isTemp = isTemp;
    this.isDeleted = isDeleted;
    this.deletedAt = deletedAt;
  }

  public static Diary from(User user, DiaryCreateRequest request) {
    if (request == null) {
      return Diary.builder()
          .user(user)
          .isTemp(true)
          .isDeleted(false)
          .build();
    }

    return Diary.builder()
        .user(user)
        .content(request.getContent())
        .date(request.getDate())
        .isTemp(true)
        .isDeleted(false)
        .build();
  }

  public void autoSave(DiaryAutoSaveRequest request) {
    this.content = request.getContent();
    this.date = request.getDate();
  }

  public void finalSave(DiaryFinalSaveRequest request) {
    this.content = request.getContent();
    this.date = request.getDate();
    this.isTemp = false;
  }

  public void startEditing() {
    this.isTemp = true;
  }

  public void deactivate() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
  }
}
