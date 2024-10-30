package com.example.moodwriter.domain.emotion.entity;

import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Table(name = "emotion_analysis")
public class EmotionAnalysis extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diary_id", nullable = false)
  private Diary diary;

  @Column(name = "primary_emotion", columnDefinition = "VARCHAR(50)")
  private String primaryEmotion;

  @Column(name = "emotion_score", columnDefinition = "TINYINT")
  private Integer emotionScore;

  @Column(name = "analysis_content", columnDefinition = "TEXT")
  private String analysisContent;

  @Column(columnDefinition = "DATE")
  private LocalDate date;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public EmotionAnalysis(User user, Diary diary, String primaryEmotion, int emotionScore,
      String analysisContent, LocalDate date, boolean isDeleted, LocalDateTime deletedAt) {
    this.user = user;
    this.diary = diary;
    this.primaryEmotion = primaryEmotion;
    this.emotionScore = emotionScore;
    this.analysisContent = analysisContent;
    this.date = date;
    this.isDeleted = isDeleted;
    this.deletedAt = deletedAt;
  }

  public static EmotionAnalysis from(Diary diary) {
    return EmotionAnalysis.builder()
        .user(diary.getUser())
        .diary(diary)
        .date(diary.getDate())
        .isDeleted(false)
        .build();
  }

  public void clear(Diary diary) {
    this.user = diary.getUser();
    this.diary = diary;
    this.date = diary.getDate();
    this.isDeleted = false;
    this.deletedAt = null;
    this.primaryEmotion = null;
    this.emotionScore = null;
    this.analysisContent = null;
  }

  public void updateScoreAndPrimaryEmotion(int score, String primaryEmotion) {
    this.emotionScore = score;
    this.primaryEmotion = primaryEmotion;
  }

  public void deactivate() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
  }
}
