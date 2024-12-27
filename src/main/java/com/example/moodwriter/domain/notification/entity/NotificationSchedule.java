package com.example.moodwriter.domain.notification.entity;

import com.example.moodwriter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class NotificationSchedule extends BaseEntity {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_id", nullable = false)
  private NotificationRecipient recipient;

  @Column(name = "scheduled_time", nullable = false)
  private LocalTime scheduledTime;

  @Builder
  public NotificationSchedule(NotificationRecipient recipient, LocalTime scheduledTime) {
    this.recipient = recipient;
    this.scheduledTime = scheduledTime;
  }

}
