package com.example.moodwriter.domain.diary.dao;

import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.user.entity.User;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {

  Slice<Diary> findByDateBetweenAndIsDeletedFalseAndIsTempFalseAndUser(LocalDate startDate,
      LocalDate endDate, User user, Pageable pageable);
  Slice<Diary> findAllByUser(User user, Pageable pageable);
}
