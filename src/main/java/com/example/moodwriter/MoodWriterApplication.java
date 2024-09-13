package com.example.moodwriter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication()
public class MoodWriterApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoodWriterApplication.class, args);
  }

}
