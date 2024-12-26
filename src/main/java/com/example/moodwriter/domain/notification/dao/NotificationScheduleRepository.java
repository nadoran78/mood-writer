package com.example.moodwriter.domain.notification.dao;

import com.example.moodwriter.domain.notification.entity.NotificationSchedule;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationScheduleRepository extends
    JpaRepository<NotificationSchedule, UUID> {

}
