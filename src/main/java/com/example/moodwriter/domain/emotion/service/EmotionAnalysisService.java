package com.example.moodwriter.domain.emotion.service;

import static com.example.moodwriter.global.exception.code.ErrorCode.ALREADY_DELETED_DIARY;
import static com.example.moodwriter.global.exception.code.ErrorCode.ALREADY_DELETED_EMOTION_ANALYSIS;
import static com.example.moodwriter.global.exception.code.ErrorCode.FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS;
import static com.example.moodwriter.global.exception.code.ErrorCode.FORBIDDEN_ACCESS_DIARY;
import static com.example.moodwriter.global.exception.code.ErrorCode.JSON_PARSE_ERROR;
import static com.example.moodwriter.global.exception.code.ErrorCode.NOT_FOUND_EMOTION_ANALYSIS;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.emotion.dao.EmotionAnalysisRepository;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisRequest;
import com.example.moodwriter.domain.emotion.entity.EmotionAnalysis;
import com.example.moodwriter.domain.emotion.exception.EmotionAnalysisException;
import com.example.moodwriter.global.constant.OpenAIModel;
import com.example.moodwriter.global.constant.OpenAIRequestSentence;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse;
import com.example.moodwriter.global.openAI.service.OpenAIClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

  private final DiaryRepository diaryRepository;
  private final EmotionAnalysisRepository emotionAnalysisRepository;
  private final OpenAIClient openAIClient;
  private final ObjectMapper objectMapper;

  @Transactional
  public EmotionAnalysisResponse createPrimaryEmotionAndEmotionScore(
      EmotionAnalysisRequest request, UUID userId) {
    Diary diary = getValidDiary(request.getDiaryId(), userId);

    EmotionAnalysis emotionAnalysis = getOrCreateEmotionAnalysis(diary);

    String openAIRequest =
        diary.getContent()
            + OpenAIRequestSentence.PRIMARY_EMOTION_AND_SCORE.getSentence();

    OpenAIResponse openAIResponse = openAIClient.callOpenAI(openAIRequest,
        OpenAIModel.GPT_3_5_TURBO);

    String content = openAIResponse.getChoices().get(0).getMessage().getContent();

    EmotionScoreAndPrimaryEmotion emotionScoreAndPrimaryEmotion;
    try {
      emotionScoreAndPrimaryEmotion = objectMapper.readValue(content,
          EmotionScoreAndPrimaryEmotion.class);
    } catch (JsonProcessingException e) {
      throw new CustomException(JSON_PARSE_ERROR);
    }

    emotionAnalysis.updateScoreAndPrimaryEmotion(
        emotionScoreAndPrimaryEmotion.getEmotionScore(),
        emotionScoreAndPrimaryEmotion.getPrimaryEmotion());

    EmotionAnalysis savedEmotionAnalysis = emotionAnalysisRepository.save(
        emotionAnalysis);

    return EmotionAnalysisResponse.fromEntity(savedEmotionAnalysis);
  }

  private Diary getValidDiary(UUID diaryId, UUID userId) {
    Diary diary = diaryRepository.findById(diaryId)
        .orElseThrow(() -> new DiaryException(ErrorCode.NOT_FOUND_DIARY));

    if (!diary.getUser().getId().equals(userId)) {
      throw new DiaryException(FORBIDDEN_ACCESS_DIARY);
    }

    if (diary.isDeleted()) {
      throw new DiaryException(ALREADY_DELETED_DIARY);
    }

    if (diary.isTemp()) {
      throw new EmotionAnalysisException(FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS);
    }

    return diary;
  }

  private EmotionAnalysis getOrCreateEmotionAnalysis(Diary diary) {
    EmotionAnalysis emotionAnalysis = emotionAnalysisRepository.findByDiary(diary)
        .orElse(EmotionAnalysis.from(diary));

    if (emotionAnalysis.isDeleted()) {
      emotionAnalysis.clear(diary);
    }

    return emotionAnalysis;
  }

  @Transactional
  public EmotionAnalysisResponse createEmotionAnalysis(EmotionAnalysisRequest request,
      UUID userId) {
    Diary diary = getValidDiary(request.getDiaryId(), userId);

    EmotionAnalysis emotionAnalysis = getOrCreateEmotionAnalysis(diary);

    String openAIRequest =
        diary.getContent() + OpenAIRequestSentence.EMOTION_ANALYSIS.getSentence();

    OpenAIResponse openAIResponse = openAIClient.callOpenAI(openAIRequest,
        OpenAIModel.GPT_4O_MINI);

    String analysisContent = openAIResponse.getChoices().get(0).getMessage().getContent();

    emotionAnalysis.updateEmotionAnalysisContent(analysisContent);

    EmotionAnalysis savedEmotionAnalysis = emotionAnalysisRepository.save(
        emotionAnalysis);

    return EmotionAnalysisResponse.fromEntity(savedEmotionAnalysis);
  }

  @Transactional(readOnly = true)
  public EmotionAnalysisResponse getEmotionAnalysis(UUID diaryId, UUID userId) {
    Diary diary = getValidDiary(diaryId, userId);

    EmotionAnalysis emotionAnalysis = emotionAnalysisRepository.findByDiary(diary)
        .orElseThrow(() -> new EmotionAnalysisException(NOT_FOUND_EMOTION_ANALYSIS));

    if (emotionAnalysis.isDeleted()) {
      throw new EmotionAnalysisException(ALREADY_DELETED_EMOTION_ANALYSIS);
    }

    return EmotionAnalysisResponse.fromEntity(emotionAnalysis);
  }

  @Getter
  @NoArgsConstructor
  @Builder
  @AllArgsConstructor
  public static class EmotionScoreAndPrimaryEmotion {

    private int emotionScore;
    private String primaryEmotion;
  }
}
