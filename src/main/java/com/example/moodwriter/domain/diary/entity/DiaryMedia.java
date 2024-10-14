package com.example.moodwriter.domain.diary.entity;

import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "diary_media")
public class DiaryMedia extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diary_id", nullable = false)
  private Diary diary;

  @Column(name = "file_url", nullable = false)
  private String fileUrl;

  @Column(name = "file_type", nullable = false)
  private String fileType;

  @Builder
  public DiaryMedia(User user, Diary diary, String fileUrl, String fileType) {
    this.user = user;
    this.diary = diary;
    this.fileUrl = fileUrl;
    this.fileType = fileType;
  }

  public static DiaryMedia of(FileDto fileDto, User user, Diary diary) {
    return DiaryMedia.builder()
        .user(user)
        .diary(diary)
        .fileUrl(fileDto.getUrl())
        .fileType(fileDto.getFileType())
        .build();
  }
}
