package com.example.moodwriter.global.config;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FirebaseConfig {

  private static final AtomicBoolean initialized = new AtomicBoolean(false);

  @Value("${firebase.key.path}")
  private String path;

  public synchronized void initializeFirebaseApp() {
    if (initialized.get()) return;

    try {
      if (FirebaseApp.getApps().isEmpty()) {
        log.info(path);

        File file = new File(path);
        InputStream serviceAccount = new FileInputStream(file);
        log.info("THERE IS FIREBASE KEY FILE.");

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        log.info("SUCCESS FIREBASE OPTIONS BUILD");

        FirebaseApp.initializeApp(options);
        initialized.set(true);
        log.info("✅ Firebase initialized successfully.");
      }
    } catch (Exception e) {
      log.error("🔥 Firebase 초기화 중 에러 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.FAIL_TO_INITIALIZE_FIREBASE);
    }
  }
}
