package com.example.moodwriter.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePath {

  PROFILE("profile"),
  ;

  private final String path;
}
