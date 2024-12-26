package com.example.moodwriter.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class NotificationSchedule {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_id", nullable = false)
  private NotificationRecipient recipient;

  @Column(name = "scheduled_at", nullable = false)
  private LocalDateTime scheduledAt;

  @Builder
  public NotificationSchedule(NotificationRecipient recipient, LocalDateTime scheduledAt) {
    this.recipient = recipient;
    this.scheduledAt = scheduledAt;
  }

}
