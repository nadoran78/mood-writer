package com.example.moodwriter.global.jwt;

import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.service.CustomUserDetailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TokenProvider {

  private final RedisTemplate<String, String> redisTemplate;
  private final CustomUserDetailService customUserDetailService;

  @Value("${spring.jwt.secret-key}")
  private String secretKey;

  public static final String TOKEN_PREFIX = "Bearer ";
  private static final String KEY_REFRESH_TOKEN = "refreshToken: ";
  private static final String KEY_ACCESS_TOKEN = "accessToken: ";
  private static final String KEY_ROLES = "roles";
  private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60; // 1시간
  private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 30; // 30일

  @PostConstruct
  protected void init() {
    secretKey = Base64.getEncoder()
        .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public TokenResponse generateTokenResponse(String email, List<String> roles) {
    Claims claims = Jwts.claims().setSubject(email);
    claims.put(KEY_ROLES, roles);

    String accessToken = generateToken(claims, ACCESS_TOKEN_EXPIRE_TIME);
    String refreshToken = generateToken(claims, REFRESH_TOKEN_EXPIRE_TIME);

    String savedToken = redisTemplate.opsForValue().get(KEY_REFRESH_TOKEN + email);
    if (savedToken != null) {
      redisTemplate.delete(KEY_REFRESH_TOKEN + email);
    }

    long expirationSeconds = getTokenExpireTime(refreshToken);
    redisTemplate.opsForValue()
        .set(KEY_REFRESH_TOKEN + email, refreshToken, expirationSeconds,
            TimeUnit.SECONDS);

    return TokenResponse.builder()
        .email(email)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  private String generateToken(Claims claims, Long expiredTime) {
    Date now = new Date();

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expiredTime))
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
  }

  public TokenResponse regenerateAccessToken(String refreshToken) {
    if (!validateToken(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(refreshToken)
        .getBody();

    String email = claims.getSubject();

    String findToken = redisTemplate.opsForValue().get(KEY_REFRESH_TOKEN + email);
    if (findToken == null) {
      throw new CustomException(ErrorCode.NOT_FOUND_REFRESH_TOKEN);
    }

    if (!refreshToken.equals(findToken)) {
      throw new CustomException(ErrorCode.UNMATCHED_SAVED_REFRESH_TOKEN);
    }

    String accessToken = generateToken(claims, ACCESS_TOKEN_EXPIRE_TIME);

    return TokenResponse.builder()
        .email(email)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  public CustomUserDetails getCustomUserDetailsByToken(String token) {
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
        .getBody();

    return (CustomUserDetails) customUserDetailService.loadUserByUsername(
        claims.getSubject());
  }

  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) {
      return false;
    }

    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

      return !claims.getBody().getExpiration().before(new Date());
    } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
      throw new CustomException(ErrorCode.INVALID_TOKEN);
    }
  }

  public Authentication getAuthentication(UserDetails userDetails) {
    return new JwtAuthenticationToken(userDetails, "",
        userDetails.getAuthorities());
  }

  public boolean isAccessTokenDenied(String accessToken) {
    String email = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken)
        .getBody().getSubject();
    String savedToken = redisTemplate.opsForValue().get(KEY_ACCESS_TOKEN + email);

    return savedToken != null && savedToken.equals(accessToken);
  }

  public String resolveTokenFromRequest(String token) {
    if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
      return token.substring(TOKEN_PREFIX.length());
    }
    return null;
  }

  public void deleteRefreshToken(String email) {
    String savedRefreshToken = redisTemplate.opsForValue().get(KEY_REFRESH_TOKEN + email);

    if (savedRefreshToken == null) {
      throw new CustomException(ErrorCode.NOT_FOUND_REFRESH_TOKEN);
    } else {
      redisTemplate.delete(KEY_REFRESH_TOKEN + email);
    }
  }

  public void addBlackList(String accessToken) {
    String email = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(accessToken)
        .getBody().getSubject();
    long expirationSeconds = getTokenExpireTime(accessToken);
    redisTemplate.opsForValue()
        .set(KEY_ACCESS_TOKEN + email, accessToken, expirationSeconds, TimeUnit.SECONDS);
  }

  private long getTokenExpireTime(String token) {
    Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
    return (claims.getBody().getExpiration().getTime() - new Date().getTime()) / 1000;
  }
}
