package com.example.moodwriter.global.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SocialProvider {
  GOOGLE;

  @JsonCreator
  public static SocialProvider forValue(String value) {
    return SocialProvider.valueOf(value.toUpperCase());
  }

  @JsonValue
  public String toValue() {
    return this.name().toLowerCase();
  }
}
