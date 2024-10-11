package com.example.moodwriter.global.config;

import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.security.exception.CustomAccessDeniedHandler;
import com.example.moodwriter.global.security.exception.CustomAuthenticationEntryPoint;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
      throws Exception {
    httpSecurity
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(request -> request
            .requestMatchers(HttpMethod.GET, "/api/users")
            .hasRole(Role.ROLE_USER.getRole())
            .requestMatchers(HttpMethod.PATCH, "/api/users")
            .hasRole(Role.ROLE_USER.getRole())
            .requestMatchers(HttpMethod.DELETE, "/api/users")
            .hasRole(Role.ROLE_USER.getRole())
            .requestMatchers("/api/users/logout").hasRole(Role.ROLE_USER.getRole())
            .requestMatchers("/api/users/reissue-token").hasRole(Role.ROLE_USER.getRole())
            .requestMatchers("/api/users/register").permitAll()
            .requestMatchers("/api/users/login").permitAll()

            .requestMatchers(HttpMethod.POST, "/api/diaries/images").hasRole(Role.ROLE_USER.getRole())
            .anyRequest().authenticated())

        .exceptionHandling(exception -> {
          exception.authenticationEntryPoint(customAuthenticationEntryPoint);
          exception.accessDeniedHandler(customAccessDeniedHandler);
        })

        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
