package com.example.moodwriter.domain.diary.dao;

import com.example.moodwriter.domain.diary.entity.DiaryMedia;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryMediaRepository extends JpaRepository<DiaryMedia, UUID> {

}
