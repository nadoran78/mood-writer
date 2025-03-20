package com.example.moodwriter.domain.diary.dao;

import com.example.moodwriter.domain.diary.entity.DiaryMedia;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryMediaRepository extends JpaRepository<DiaryMedia, Long> {
  Optional<DiaryMedia> findByFileName(String filename);
}
