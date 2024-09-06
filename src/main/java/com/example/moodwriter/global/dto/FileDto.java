package com.example.moodwriter.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileDto {

  private final String url;

  private final String filename;
}
