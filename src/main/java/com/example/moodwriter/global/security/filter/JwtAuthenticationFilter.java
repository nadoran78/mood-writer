package com.example.moodwriter.global.security.filter;

import com.example.moodwriter.global.jwt.TokenProvider;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String TOKEN_HEADER = "Authorization";

  private final TokenProvider tokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = tokenProvider.resolveTokenFromRequest(request.getHeader(TOKEN_HEADER));

    if (tokenProvider.validateToken(token) && !tokenProvider.isAccessTokenDenied(token)) {
      CustomUserDetails userDetails = tokenProvider.getCustomUserDetailsByToken(token);
      if (userDetails.isDeleted()) {
        request.setAttribute("deactivatedUser", true);
      } else {
        Authentication authentication = tokenProvider.getAuthentication(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } else {
      log.info("토큰 유효성 검증 실패");
    }

    filterChain.doFilter(request, response);
  }
}
