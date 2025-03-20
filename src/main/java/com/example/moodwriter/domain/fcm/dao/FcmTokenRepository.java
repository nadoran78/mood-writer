package com.example.moodwriter.domain.fcm.dao;

import com.example.moodwriter.domain.fcm.entity.FcmToken;
import com.example.moodwriter.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {

  @Query("SELECT f FROM FcmToken f WHERE f.deviceId = :deviceId AND f.user.id = :userId")
  Optional<FcmToken> findByDeviceIdAndUserId(@Param("deviceId") String deviceId,
      @Param("userId") UUID userId);

  List<FcmToken> findAllByUserAndIsActiveTrue(User user);
}
