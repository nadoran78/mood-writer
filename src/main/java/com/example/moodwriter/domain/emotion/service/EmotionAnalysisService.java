package com.example.moodwriter.domain.emotion.service;

import static com.example.moodwriter.global.exception.code.ErrorCode.ALREADY_DELETED_DIARY;
import static com.example.moodwriter.global.exception.code.ErrorCode.FINAL_SAVED_DIARY_REQUIRED_FOR_EMOTION_ANALYSIS;
import static com.example.moodwriter.global.exception.code.ErrorCode.FORBIDDEN_ACCESS_DIARY;
import static com.example.moodwriter.global.exception.code.ErrorCode.JSON_PARSE_ERROR;

import com.example.moodwriter.domain.diary.dao.DiaryRepository;
import com.example.moodwriter.domain.diary.entity.Diary;
import com.example.moodwriter.domain.diary.exception.DiaryException;
import com.example.moodwriter.domain.emotion.dao.EmotionAnalysisRepository;
import com.example.moodwriter.domain.emotion.dto.EmotionAnalysisResponse;
import com.example.moodwriter.domain.emotion.dto.PrimaryEmotionAndScoreRequest;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmotionAnalysisService {

  private final DiaryRepository diaryRepository;
  private final EmotionAnalysisRepository emotionAnalysisRepository;
  private final OpenAIClient openAIClient;
  private final ObjectMapper objectMapper;

  public EmotionAnalysisResponse createPrimaryEmotionAndEmotionScore(
      PrimaryEmotionAndScoreRequest request, UUID userId) {
    Diary diary = diaryRepository.findById(request.getDiaryId())
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

    EmotionAnalysis emotionAnalysis = emotionAnalysisRepository.findByDiary(diary)
        .orElse(EmotionAnalysis.from(diary));

    String openAIRequest =
        diary.getContent() + OpenAIRequestSentence.PRIMARY_EMOTION_AND_SCORE.getSentence();

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

    emotionAnalysis.updateScoreAndPrimaryEmotion(emotionScoreAndPrimaryEmotion.getEmotionScore(),
        emotionScoreAndPrimaryEmotion.getPrimaryEmotion());

    EmotionAnalysis savedEmotionAnalysis = emotionAnalysisRepository.save(emotionAnalysis);

    return EmotionAnalysisResponse.fromEntity(savedEmotionAnalysis);
  }

  @Getter
  @NoArgsConstructor
  public static class EmotionScoreAndPrimaryEmotion {

    private int emotionScore;
    private String primaryEmotion;
  }
}
