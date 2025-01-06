package com.example.moodwriter.domain.notification.dao;

import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRecipientRepository extends
    JpaRepository<NotificationRecipient, UUID> {

  @Query("SELECT nr FROM NotificationRecipient nr WHERE nr.user.id = :userId AND nr.notification.id = :notificationId")
  Optional<NotificationRecipient> findByUserIdAndNotificationId(@Param("userId") UUID userId,
      @Param("notificationId") UUID notificationId);
}
