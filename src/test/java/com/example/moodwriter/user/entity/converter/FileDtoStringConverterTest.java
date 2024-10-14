package com.example.moodwriter.user.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.moodwriter.domain.user.entity.converter.FileDtoStringConverter;
import com.example.moodwriter.global.dto.FileDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileDtoStringConverterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FileDtoStringConverter fileDtoStringConverter = new FileDtoStringConverter(
      objectMapper);

  private List<FileDto> fileDtoList;

  @BeforeEach
  void setUp() {
    fileDtoList = new ArrayList<>();
    fileDtoList.add(
        new FileDto("https://example.com/file1.jpg", "file1.jpg", "image/jpeg"));
    fileDtoList.add(
        new FileDto("https://example.com/file2.jpg", "file2.jpg", "image/jpeg"));
  }

  @Test
  void successConvertToDatabaseColumn() {
    String expectedJson =
        "[{\"url\":\"https://example.com/file1.jpg\",\"filename\":\"file1.jpg\",\"fileType\":\"image/jpeg\"},"
            + "{\"url\":\"https://example.com/file2.jpg\",\"filename\":\"file2.jpg\",\"fileType\":\"image/jpeg\"}]";

    String jsonResult = fileDtoStringConverter.convertToDatabaseColumn(fileDtoList);

    assertEquals(expectedJson, jsonResult);
  }

  @Test
  void convertToDatabaseColumn_nullInput() {
    // when
    String jsonResult = fileDtoStringConverter.convertToDatabaseColumn(null);

    // then
    assertNull(jsonResult);
  }

  @Test
  void successConvertToEntityAttribute() {
    // given
    String dbData =
        "[{\"url\":\"https://example.com/file1.jpg\",\"filename\":\"file1.jpg\"},"
            + "{\"url\":\"https://example.com/file2.jpg\",\"filename\":\"file2.jpg\"}]";

    // when
    List<FileDto> result = fileDtoStringConverter.convertToEntityAttribute(dbData);

    // then
    assertEquals(2, result.size());
    assertEquals("https://example.com/file1.jpg", result.get(0).getUrl());
    assertEquals("file1.jpg", result.get(0).getFilename());
    assertEquals("https://example.com/file2.jpg", result.get(1).getUrl());
    assertEquals("file2.jpg", result.get(1).getFilename());
  }
}