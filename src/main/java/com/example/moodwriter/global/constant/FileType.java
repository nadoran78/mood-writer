package com.example.moodwriter.global.constant;

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
  IMAGE(Set.of("image/jpeg", "image/tiff", "image/png", "image/gif", "image/bmp", "image/webp")),
  ;

  private final Set<String> extensions;
}
