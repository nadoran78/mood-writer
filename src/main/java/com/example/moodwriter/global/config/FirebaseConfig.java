package com.example.moodwriter.global.config;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
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
        FileInputStream serviceAccount = new FileInputStream(path);

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

        FirebaseApp.initializeApp(options);
        initialized.set(true);
        log.info("✅ Firebase initialized successfully.");
      }
    } catch (IOException e) {
      throw new CustomException(ErrorCode.FAIL_TO_INITIALIZE_FIREBASE);
    }
  }
}
