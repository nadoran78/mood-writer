package com.example.moodwriter.domain.emotion.dao;

import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionAnalysisRepository extends JpaRepository<EmotionAnalysis, UUID> {

}
