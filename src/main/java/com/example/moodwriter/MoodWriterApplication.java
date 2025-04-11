package com.example.moodwriter;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@Slf4j
public class MoodWriterApplication {

  @PostConstruct
  public void started() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    log.info("Current Time: {}", LocalDateTime.now());
  }

  public static void main(String[] args) {
    SpringApplication.run(MoodWriterApplication.class, args);
  }

}
