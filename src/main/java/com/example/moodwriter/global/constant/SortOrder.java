package com.example.moodwriter.global.constant;

public enum SortOrder {
  DESC, ASC;


  public static SortOrder forValue(String value) {
    return SortOrder.valueOf(value.toUpperCase());
  }
}
