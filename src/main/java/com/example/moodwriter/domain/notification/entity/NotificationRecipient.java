package com.example.moodwriter.domain.notification.entity;

import com.example.moodwriter.domain.user.entity.User;
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
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Table(name = "notification_recipient")
public class NotificationRecipient {

  @Id
  @UuidGenerator
  @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "notification_id", nullable = false)
  private Notification notification;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "is_read", nullable = false)
  private boolean isRead = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  public void markAsRead() {
    this.isRead = true;
    this.readAt = LocalDateTime.now();
  }

  @Builder
  public NotificationRecipient(Notification notification, User user, boolean isActive) {
    this.notification = notification;
    this.user = user;
    this.isActive = isActive;
  }

  public static NotificationRecipient from(Notification notification, User user) {
    return NotificationRecipient.builder()
        .notification(notification)
        .user(user)
        .isActive(true)
        .build();
  }

  public void activate() {
    this.isActive = true;
  }

  public void deactivate() {
    this.isActive = false;
  }

}
