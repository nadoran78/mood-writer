package com.example.moodwriter.domain.diary.entity;

import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.global.s3.dto.FileDto;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Entity
@Table(name = "diary_media", indexes = {
    @Index(name = "idx_file_name_prefix", columnList = "file_name(16)")
})
public class DiaryMedia extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Builder
  public DiaryMedia(User user, Diary diary, String fileUrl, String fileType, String fileName) {
    this.user = user;
    this.diary = diary;
    this.fileUrl = fileUrl;
    this.fileType = fileType;
    this.fileName = fileName;
  }

  public static DiaryMedia of(FileDto fileDto, User user, Diary diary) {
    return DiaryMedia.builder()
        .user(user)
        .diary(diary)
        .fileUrl(fileDto.getUrl())
        .fileType(fileDto.getFileType())
        .fileName(fileDto.getFilename())
        .build();
  }
}
