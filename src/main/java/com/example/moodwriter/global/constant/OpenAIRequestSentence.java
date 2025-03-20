package com.example.moodwriter.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenAIRequestSentence {

  PRIMARY_EMOTION_AND_SCORE(
      "\n\n위 일기를 보고 감정점수와 대표 감정을 작성해주는데, "
          + "감정 점수는 매우 긍정적일 때 100점, 매우 부정적일 때 0점을 기준으로 해서 대표점수 하나만 알려줘. "
          + "그리고 대표 감정은 한 단어로 세가지를 아래와 같이 json 문자열 예시와 같은 형식으로 대답해줘\n\n"
          + "{\n"
          + "    \"emotionScore\": ?,\n"
          + "    \"primaryEmotion\": \"??, ??, ??\""
          + "\n}"
  ),
  EMOTION_ANALYSIS(
      "\n\n위 일기를 보고 감정분석가가 되어 감정분석을 상세하게 작성해주고, "
          + "감정분석에 따른 적절한 조언을 상세히 작성해줘."
  )

  ;

  private final String sentence;
}
