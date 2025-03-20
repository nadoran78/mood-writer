package com.example.moodwriter.domain.notification.entity;

import com.example.moodwriter.domain.notification.constant.NotificationTopic;
import com.example.moodwriter.domain.notification.converter.MapToJsonConverter;
import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "topic", nullable = false)
  private NotificationTopic topic;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "body", nullable = false, columnDefinition = "TEXT")
  private String body;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "data", columnDefinition = "TEXT")
  private Map<String, String> data;

  @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
  private List<NotificationRecipient> recipients = new ArrayList<>();

  @Builder
  public Notification(NotificationTopic topic, String title, String body,
      Map<String, String> data) {
    this.topic = topic;
    this.title = title;
    this.body = body;
    this.data = data;
  }

}
