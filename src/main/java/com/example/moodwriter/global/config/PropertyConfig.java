package com.example.moodwriter.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@Profile("local")
@PropertySources({
    @PropertySource("classpath:properties/env.properties")
})
public class PropertyConfig {

}
