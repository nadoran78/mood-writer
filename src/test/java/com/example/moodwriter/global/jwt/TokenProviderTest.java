package com.example.moodwriter.global.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.exception.CustomException;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.service.CustomUserDetailService;
import com.example.moodwriter.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private CustomUserDetailService customUserDetailService;

  private final String testMail = "test@example.com";
  private final List<String> roles = List.of(Role.ROLE_USER.toString());
  private final String secretKey = "secretKey";

  @InjectMocks
  private TokenProvider tokenProvider;

  @BeforeEach
  void tokenProviderInit() {
    ReflectionTestUtils.setField(tokenProvider, "secretKey", secretKey);
    tokenProvider.init();
  }

  @Test
  void successGenerateTokenResponse() {
    //given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn(null);

    // when
    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    // then
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
    verify(redisTemplate.opsForValue()).set(argumentCaptor.capture(),
        argumentCaptor.capture(), longArgumentCaptor.capture(), eq(TimeUnit.SECONDS));

    assertNotNull(tokenResponse.getAccessToken());
    assertNotNull(tokenResponse.getRefreshToken());

    assertEquals("refreshToken: " + testMail, argumentCaptor.getAllValues().get(0));
    assertEquals(tokenResponse.getRefreshToken(), argumentCaptor.getAllValues().get(1));

    long refreshTokenExpireTime = 1000L * 60 * 60 * 24 * 30;
    long refreshTokenExpireTimeBefore10seconds = refreshTokenExpireTime - 1000L * 10;
    long captureTime = longArgumentCaptor.getValue() * 1000L;

    assertTrue(captureTime <= refreshTokenExpireTime
        && captureTime > refreshTokenExpireTimeBefore10seconds);
  }

  @Test
  void successGenerateTokenResponseWithSavedToken() {
    //given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn("savedToken");

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    //when
    assertNotNull(tokenResponse.getAccessToken());
    assertNotNull(tokenResponse.getRefreshToken());

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(redisTemplate).delete(argumentCaptor.capture());
    assertEquals("refreshToken: " + testMail, argumentCaptor.getValue());
  }

  @Test
  void successRegenerateAccessToken() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    given(valueOperations.get(anyString())).willReturn(tokenResponse.getRefreshToken());

    // when
    TokenResponse regenerateTokenResponse = tokenProvider.regenerateAccessToken(
        tokenResponse.getRefreshToken());

    // then
    assertNotNull(regenerateTokenResponse.getAccessToken());
    assertEquals(tokenResponse.getRefreshToken(),
        regenerateTokenResponse.getRefreshToken());
  }

  @Test
  void throwNotFoundRefreshTokenExceptionWhenSavedTokenIsNotExist() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    given(valueOperations.get(anyString())).willReturn(null);

    // when & then
    CustomException customException = assertThrows(CustomException.class,
        () -> tokenProvider.regenerateAccessToken(tokenResponse.getRefreshToken()));

    // then
    assertEquals(ErrorCode.NOT_FOUND_REFRESH_TOKEN, customException.getErrorCode());
  }

  @Test
  void throwUnmatchedSavedRefreshTokenExceptionWhenSavedTokenIsNotSameWithRefreshToken() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    given(valueOperations.get(anyString())).willReturn("unmatchedToken");

    // when & then
    CustomException customException = assertThrows(CustomException.class,
        () -> tokenProvider.regenerateAccessToken(tokenResponse.getRefreshToken()));

    // then
    assertEquals(ErrorCode.UNMATCHED_SAVED_REFRESH_TOKEN, customException.getErrorCode());
  }

  @Test
  void throwInvalidTokenExceptionWhenInputTokenIsNotValid() {
    // given
    String refreshToken = "invalidToken";

    // when & then
    CustomException customException = assertThrows(CustomException.class,
        () -> tokenProvider.regenerateAccessToken(refreshToken));

    // then
    assertEquals(ErrorCode.INVALID_TOKEN, customException.getErrorCode());
  }

  @Test
  void successValidateToken() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    // when & then
    assertTrue(tokenProvider.validateToken(tokenResponse.getRefreshToken()));
  }

  @Test
  void throwInvalidTokenExceptionWhenExpiredTimeIsOver() {
    //given
    Date now = new Date();
    Date beforeTwoHour = new Date(now.getTime() - (1000L * 60 * 60 * 2));
    Date beforeOneHour = new Date(now.getTime() - (1000L * 60 * 60));
    Claims claims = Jwts.claims().setSubject("abc@abcd.com");
    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(beforeTwoHour)
        .setExpiration(beforeOneHour)
        .signWith(SignatureAlgorithm.HS256, Base64.getEncoder()
            .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8)))
        .compact();
    //when
    CustomException customException = assertThrows(CustomException.class,
        () -> tokenProvider.validateToken(token));
    //then
    assertEquals(customException.getErrorCode(), ErrorCode.INVALID_TOKEN);
  }

  @Test
  void successGetAuthentication() {
    //given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    User user = User.builder()
        .email(this.testMail)
        .role(Role.ROLE_USER)
        .build();
    UserDetails userDetails = new CustomUserDetails(user);

    given(customUserDetailService.loadUserByUsername(testMail)).willReturn(userDetails);
    //when
    Authentication authentication = tokenProvider.getAuthentication(
        tokenResponse.getAccessToken());
    //then
    assertEquals(authentication.getPrincipal(), userDetails);
    assertEquals(authentication.getAuthorities(), userDetails.getAuthorities());
  }

  @Test
  void successResolveTokenFromRequest() {
    //given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    String token = "Bearer " + tokenResponse.getAccessToken();
    //when
    String resolvedToken = tokenProvider.resolveTokenFromRequest(token);
    //then
    assertEquals(resolvedToken, tokenResponse.getAccessToken());
  }

  @Test
  void successIsAccessTokenDenied() {
    //given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    TokenResponse tokenResponse = tokenProvider.generateTokenResponse(testMail, roles);

    given(valueOperations.get(anyString())).willReturn(tokenResponse.getAccessToken());

    //when & then
    assertTrue(tokenProvider.isAccessTokenDenied(tokenResponse.getAccessToken()));
  }

  @Test
  void successDeleteRefreshToken() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn("refreshToken");

    // when
    tokenProvider.deleteRefreshToken("refreshToken");

    // then
    verify(redisTemplate).delete("refreshToken: " + "refreshToken");
  }

  @Test
  void throwNotFoundRefreshTokenExceptionWhenDeleteRefreshTokenWithNotSavedToken() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn(null);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> tokenProvider.deleteRefreshToken("refreshToken"));

    // then
    assertEquals(ErrorCode.NOT_FOUND_REFRESH_TOKEN, customException.getErrorCode());
  }

  @Test
  void successAddBlackList() {
    // given
    Date now = new Date();
    Date beforeTwoHour = new Date(now.getTime() - (1000L * 60 * 60 * 2));
    Date after30Seconds = new Date(now.getTime() + (1000L * 30));
    Claims claims = Jwts.claims().setSubject(testMail);
    String token = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(beforeTwoHour)
        .setExpiration(after30Seconds)
        .signWith(SignatureAlgorithm.HS256, Base64.getEncoder()
            .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8)))
        .compact();

    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    doNothing().when(valueOperations).set(
        anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // when
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
    tokenProvider.addBlackList(token);

    // then(만료시간 확인)
    verify(valueOperations).set(
        eq("accessToken: " + testMail),
        eq(token),
        captor.capture(),
        eq(TimeUnit.SECONDS));
    Long expirationSeconds = captor.getValue();

    assertTrue(expirationSeconds < 30L);
  }

}