package com.example.moodwriter.global.converter;

import com.example.moodwriter.global.constant.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortOrderConverter implements Converter<String, SortOrder> {


  @Override
  public SortOrder convert(@NotNull String source) {
    return SortOrder.forValue(source);
  }
}
