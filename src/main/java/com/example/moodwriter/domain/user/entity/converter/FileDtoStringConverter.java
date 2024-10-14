package com.example.moodwriter.domain.user.entity.converter;

import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Converter
public class FileDtoStringConverter implements AttributeConverter<List<FileDto>, String> {

  private final ObjectMapper objectMapper;

  @Override
  public String convertToDatabaseColumn(List<FileDto> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new CustomException(ErrorCode.JSON_PARSE_ERROR);
    }
  }

  @Override
  public List<FileDto> convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return new ArrayList<>();
    }

    try {
      return objectMapper.readValue(dbData, new TypeReference<List<FileDto>>() {});
    } catch (JsonProcessingException e) {
      throw new CustomException(ErrorCode.JSON_PARSE_ERROR);
    }
  }
}
