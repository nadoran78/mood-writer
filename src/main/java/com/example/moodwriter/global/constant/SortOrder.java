package com.example.moodwriter.global.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortOrder {
  DESC, ASC;


  public static SortOrder forValue(String value) {
    return SortOrder.valueOf(value.toUpperCase());
  }

  @JsonValue
  public String getValue() {
    return this.name().toLowerCase();
  }
}
