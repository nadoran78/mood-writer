package com.example.moodwriter.global.openAI;

import com.example.moodwriter.global.constant.OpenAIModel;
import com.example.moodwriter.global.openAI.dto.OpenAIResponse;
import com.example.moodwriter.global.openAI.service.OpenAIClient;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/open-ai/test")
public class Test {

  private final OpenAIClient openAIClient;

  @PostMapping
  public String test(@RequestBody String diary) {
    OpenAIResponse openAIResponse = null;
    try {
      openAIResponse = openAIClient.callOpenAI(diary, OpenAIModel.GPT_3_5_TURBO);
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return openAIResponse.getChoices().get(0).getMessage().getContent();
  }

}
