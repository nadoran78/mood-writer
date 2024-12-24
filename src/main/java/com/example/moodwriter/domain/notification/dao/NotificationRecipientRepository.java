package com.example.moodwriter.domain.notification.dao;

import com.example.moodwriter.domain.notification.entity.NotificationRecipient;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRecipientRepository extends
    JpaRepository<NotificationRecipient, UUID> {

}
