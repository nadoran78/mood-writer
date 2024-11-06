package com.example.moodwriter.domain.diary.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class DiaryRepositoryTest {

  @Autowired
  private DiaryRepository diaryRepository;

  @Autowired
  private UserRepository userRepository;

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

    Diary diary1 = Diary.builder()
        .date(LocalDate.of(2024, 10, 1))
        .user(user)
        .isDeleted(false)
        .isTemp(false)
        .build();
    diaryRepository.save(diary1);

    Diary diary2 = Diary.builder()
        .date(LocalDate.of(2024, 10, 5))
        .user(user)
        .isDeleted(false)
        .isTemp(false)
        .build();
    diaryRepository.save(diary2);

    Diary diary3 = Diary.builder()
        .date(LocalDate.of(2024, 10, 10))
        .user(user)
        .isDeleted(true)
        .isTemp(false)
        .build();
    diaryRepository.save(diary3);

    Diary diary4 = Diary.builder()
        .date(LocalDate.of(2024, 10, 15))
        .user(user)
        .isDeleted(false)
        .isTemp(false)
        .build();
    diaryRepository.save(diary4);

    Diary diary5 = Diary.builder()
        .date(LocalDate.of(2024, 10, 5))
        .user(user)
        .isDeleted(false)
        .isTemp(true)
        .build();
    diaryRepository.save(diary5);

    Diary diary6 = Diary.builder()
        .date(LocalDate.of(2024, 10, 5))
        .user(anotherUser)
        .isDeleted(false)
        .isTemp(false)
        .build();
    diaryRepository.save(diary6);
  }

  @Test
  void successFindByDateBetweenAndIsDeletedFalseAndIsTempFalseAndUser() {
    LocalDate startDate = LocalDate.of(2024, 10, 1);
    LocalDate endDate = LocalDate.of(2024, 10, 10);
    Pageable pageable = PageRequest.of(0, 10, Sort.by("date").descending());

    Slice<Diary> diaries = diaryRepository.findByDateBetweenAndIsDeletedFalseAndIsTempFalseAndUser(
        startDate, endDate, user, pageable);

    assertEquals(2, diaries.getContent().size());
    assertTrue(diaries.getContent().stream().noneMatch(Diary::isDeleted));
    assertTrue(diaries.getContent().stream().noneMatch(Diary::isTemp));
    assertTrue(diaries.getContent().stream().allMatch(diary -> diary.getUser().equals(user)));
    assertEquals(LocalDate.of(2024, 10, 5), diaries.getContent().get(0).getDate());
    assertEquals(LocalDate.of(2024, 10, 1), diaries.getContent().get(1).getDate());

  }
}