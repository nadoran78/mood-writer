package com.example.moodwriter.domain.notification.converter;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class MapToJsonConverter implements
    AttributeConverter<Map<String, String>, String> {

  private final ObjectMapper objectMapper;

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    try {
      return attribute == null ? null : objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new CustomException(ErrorCode.JSON_PARSE_ERROR);
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    try {
      return dbData == null ? null
          : objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {
          });
    } catch (JsonProcessingException e) {
      throw new CustomException(ErrorCode.JSON_PARSE_ERROR);
    }
  }
}
