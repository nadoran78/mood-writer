package com.example.moodwriter.global.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePath {

  PROFILE("profile"),
  DIARY("diary")
  ;

  private final String path;
}
