package com.example.moodwriter.domain.user.dao;

import com.example.moodwriter.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByEmail(String email);
  User getByEmail(String email);

  Optional<User> findByEmail(String email);
}
