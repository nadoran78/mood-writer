package com.example.moodwriter.user.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.jwt.TokenProvider;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.service.S3FileService;
import com.example.moodwriter.user.dao.UserRepository;
import com.example.moodwriter.user.dto.UserLoginRequest;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import com.example.moodwriter.user.dto.UserResponse;
import com.example.moodwriter.user.entity.User;
import com.example.moodwriter.user.exception.UserException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private S3FileService s3FileService;

  @Mock
  private TokenProvider tokenProvider;

  @InjectMocks
  private UserService userService;

  @Test
  void successRegisterUserWithoutProfileImages() {
    // given
    UserRegisterRequest request = UserRegisterRequest.builder()
        .email("user@example.com")
        .password("Password123!")
        .name("하하하")
        .profileImages(Collections.emptyList())
        .build();

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash("encryptedPassword")
        .name(request.getName())
        .profilePictureUrl(null)
        .build();

    given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("encryptedPassword");
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    UserResponse response = userService.registerUser(request);

    // then
    assertEquals(user.getId(), response.getId());
    assertEquals(user.getEmail(), response.getEmail());
    assertEquals(user.getName(), response.getName());
    assertEquals(user.getProfilePictureUrl(), response.getProfilePictureUrl());
    assertEquals(user.getCreatedAt(), response.getCreatedAt());

    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userArgumentCaptor.capture());

    assertEquals(request.getEmail(), userArgumentCaptor.getValue().getEmail());
    assertEquals(request.getName(), userArgumentCaptor.getValue().getName());
    assertNull(userArgumentCaptor.getValue().getProfilePictureUrl());
    assertEquals("encryptedPassword", userArgumentCaptor.getValue().getPasswordHash());

  }

  @Test
  void successRegisterUserWithProfileImages() {
    MockMultipartFile profileImage = new MockMultipartFile("profileImages", "profile.jpg",
        "image/jpeg", new byte[]{1, 2, 3});

    UserRegisterRequest request = UserRegisterRequest.builder()
        .email("user@example.com")
        .password("Password123!")
        .name("하하하")
        .profileImages(List.of(profileImage))
        .build();

    List<FileDto> uploadedFiles = List.of(
        new FileDto("profile.jpg", "url-to-profile.jpg"));

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash("encryptedPassword")
        .name(request.getName())
        .profilePictureUrl(uploadedFiles)
        .build();

    given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("encryptedPassword");
    given(s3FileService.uploadManyFiles(request.getProfileImages(),
        FilePath.PROFILE)).willReturn(uploadedFiles);
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    UserResponse response = userService.registerUser(request);

    // then
    assertEquals(user.getId(), response.getId());
    assertEquals(user.getEmail(), response.getEmail());
    assertEquals(user.getName(), response.getName());
    assertEquals(user.getProfilePictureUrl(), response.getProfilePictureUrl());
    assertEquals(user.getCreatedAt(), response.getCreatedAt());

    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userArgumentCaptor.capture());

    assertEquals(request.getEmail(), userArgumentCaptor.getValue().getEmail());
    assertEquals(request.getName(), userArgumentCaptor.getValue().getName());
    assertEquals(uploadedFiles, userArgumentCaptor.getValue().getProfilePictureUrl());
    assertEquals("encryptedPassword", userArgumentCaptor.getValue().getPasswordHash());
  }

  @Test
  void failRegisterUserWhenAlreadyRegisterUserThrowUserException() {
    // given
    UserRegisterRequest request = UserRegisterRequest.builder()
        .email("user@example.com")
        .password("Password123!")
        .name("하하하")
        .build();

    given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

    // when & then
    assertThrows(UserException.class, () -> userService.registerUser(request));
    verify(userRepository).existsByEmail(request.getEmail());

  }

  @Test
  void successLogin() {
    // given
    UserLoginRequest request = UserLoginRequest.builder()
        .email("test@example.com")
        .password("Password1!")
        .build();

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash("encodedPassword") // 이미 암호화된 비밀번호
        .role(Role.ROLE_USER)
        .build();

    TokenResponse tokenResponse = TokenResponse.builder()
        .accessToken("access-token")
        .refreshToken("refresh-token")
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        .willReturn(true);
    given(tokenProvider.generateTokenResponse(user.getEmail(),
        List.of(user.getRole().toString()))).willReturn(tokenResponse);

    // when
    TokenResponse response = userService.login(request);

    // then
    assertNotNull(response);
    assertEquals(tokenResponse, response);
  }

  @Test
  void failLogin_whenUserNotFound_thenThrowUserException() {
    // given
    UserLoginRequest request = UserLoginRequest.builder()
        .email("nonexistent@example.com")
        .password("Password1!")
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.login(request));
    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void failLogin_whenIncorrectPassword_thenThrowUserException() {
    // given
    UserLoginRequest request = UserLoginRequest.builder()
        .email("test@example.com")
        .password("wrongPassword")
        .build();

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash("encodedPassword") // 이미 암호화된 비밀번호
        .role(Role.ROLE_USER)
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        .willReturn(false);

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.login(request));
    assertEquals(ErrorCode.INCORRECT_PASSWORD, userException.getErrorCode());
  }

}