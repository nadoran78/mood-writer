package com.example.moodwriter.domain.diary.dao;

import com.example.moodwriter.domain.diary.entity.Diary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {

}
