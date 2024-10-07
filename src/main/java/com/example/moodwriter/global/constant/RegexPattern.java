package com.example.moodwriter.global.constant;

public final class RegexPattern {
  public static final String EMAIL = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
  public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$";
  public static final String NAME = "^(?!\\s)(?!.*\\s$)(?!\\s*$).+";
}
