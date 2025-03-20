package com.example.moodwriter.domain.emotion.dao;

import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import com.example.moodwriter.domain.user.entity.User;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionAnalysisRepository extends JpaRepository<EmotionAnalysis, UUID> {

  Optional<EmotionAnalysis> findByDiary(Diary diary);

  Slice<EmotionAnalysis> findByDateBetweenAndIsDeletedFalseAndUser(LocalDate startDate,
      LocalDate endDate, User user, Pageable pageable);
}
