package com.example.moodwriter.domain.emotion.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@EnableJpaAuditing
class EmotionAnalysisRepositoryTest {

  @Autowired
  private EmotionAnalysisRepository emotionAnalysisRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private DiaryRepository diaryRepository;

  @MockBean
  private ObjectMapper objectMapper;
  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .email("test@email.com")
        .passwordHash("Password12!@")
        .name("이름")
        .build();
    user = userRepository.save(user);

    User anotherUser = User.builder()
        .email("test2@email.com")
        .passwordHash("Password12!@")
        .name("이름")
        .build();
    userRepository.save(anotherUser);

    List<Diary> diaries = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Diary diary = Diary.builder()
          .date(LocalDate.of(2024, 10, 5))
          .user(user)
          .isDeleted(false)
          .isTemp(false)
          .build();
      Diary savedDiary = diaryRepository.save(diary);
      diaries.add(savedDiary);
    }

    EmotionAnalysis emotionAnalysis1 = EmotionAnalysis.builder()
        .user(user)
        .diary(diaries.get(0))
        .date(LocalDate.of(2024, 10, 1))
        .isDeleted(false)
        .build();
    emotionAnalysisRepository.save(emotionAnalysis1);

    EmotionAnalysis emotionAnalysis2 = EmotionAnalysis.builder()
        .user(user)
        .diary(diaries.get(1))
        .date(LocalDate.of(2024, 10, 5))
        .user(user)
        .isDeleted(false)
        .build();
    emotionAnalysisRepository.save(emotionAnalysis2);

    EmotionAnalysis emotionAnalysis3 = EmotionAnalysis.builder()
        .user(user)
        .diary(diaries.get(2))
        .date(LocalDate.of(2024, 10, 10))
        .isDeleted(true)
        .build();
    emotionAnalysisRepository.save(emotionAnalysis3);

    EmotionAnalysis emotionAnalysis4 = EmotionAnalysis.builder()
        .user(user)
        .diary(diaries.get(3))
        .date(LocalDate.of(2024, 10, 15))
        .isDeleted(false)
        .build();
    emotionAnalysisRepository.save(emotionAnalysis4);

    EmotionAnalysis emotionAnalysis5 = EmotionAnalysis.builder()
        .user(anotherUser)
        .diary(diaries.get(4))
        .date(LocalDate.of(2024, 10, 5))
        .isDeleted(false)
        .build();
    emotionAnalysisRepository.save(emotionAnalysis5);
  }

  @Test
  void successFindByDateBetweenAndIsDeletedFalseAndUser() {
    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 10);
    Pageable pageable = PageRequest.of(0, 10, Sort.by("date").descending());

    Slice<EmotionAnalysis> emotionAnalyses = emotionAnalysisRepository.findByDateBetweenAndIsDeletedFalseAndUser(
        startDate, endDate, user, pageable);

    assertEquals(2, emotionAnalyses.getContent().size());
    assertTrue(
        emotionAnalyses.getContent().stream().noneMatch(EmotionAnalysis::isDeleted));
    assertTrue(emotionAnalyses.getContent().stream()
        .allMatch(emotionAnalysis -> emotionAnalysis.getUser().equals(user)));
    assertEquals(LocalDate.of(2024, 10, 5),
        emotionAnalyses.getContent().get(0).getDate());
    assertEquals(LocalDate.of(2024, 10, 1),
        emotionAnalyses.getContent().get(1).getDate());

  }
}