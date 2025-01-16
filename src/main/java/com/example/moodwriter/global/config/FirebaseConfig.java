package com.example.moodwriter.global.config;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  @PostConstruct
  public void initializeFirebaseApp() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        ClassPathResource resource =
            new ClassPathResource("moodwriterFirebaseKey.json");

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
            .build();

        FirebaseApp.initializeApp(options);
      }
    } catch (IOException e) {
      throw new CustomException(ErrorCode.FAIL_TO_INITIALIZE_FIREBASE);
    }
  }
}
