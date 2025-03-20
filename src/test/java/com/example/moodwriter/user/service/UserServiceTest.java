package com.example.moodwriter.user.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.domain.notification.dto.DailyReminderRequest;
import com.example.moodwriter.domain.notification.service.NotificationSettingService;
import com.example.moodwriter.domain.user.dao.UserRepository;
import com.example.moodwriter.domain.user.dto.SocialLoginRequest;
import com.example.moodwriter.domain.user.dto.TokenReissueRequest;
import com.example.moodwriter.domain.user.dto.UserLoginRequest;
import com.example.moodwriter.domain.user.dto.UserRegisterRequest;
import com.example.moodwriter.domain.user.dto.UserResponse;
import com.example.moodwriter.domain.user.dto.UserUpdateRequest;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.exception.UserException;
import com.example.moodwriter.domain.user.service.UserService;
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.constant.SocialProvider;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.jwt.TokenProvider;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.s3.dto.FileDto;
import com.example.moodwriter.global.s3.service.S3FileService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

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
  @Mock
  private NotificationSettingService notificationSettingService;

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

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
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
        new FileDto("profile.jpg", "url-to-profile.jpg", "image/jpeg"));

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash("encryptedPassword")
        .name(request.getName())
        .profilePictureUrl(uploadedFiles)
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
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

    User user = User.builder()
        .isDeleted(false)
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.registerUser(request));
    assertEquals(ErrorCode.ALREADY_REGISTERED_USER, userException.getErrorCode());
    verify(userRepository).findByEmail(request.getEmail());

  }

  @Test
  void shouldReactivateUserWhenUserIsDeleted() {
    // given
    String email = "user@example.com";
    String password = "Password123!";
    String encryptedPassword = "encryptedPassword";

    UserRegisterRequest request = UserRegisterRequest.builder()
        .email(email)
        .password(password)
        .name("하하하")
        .build();

    User existingUser = User.builder()
        .email(email)
        .name("Old Name")
        .isDeleted(true)
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(
        Optional.of(existingUser));
    given(passwordEncoder.encode(request.getPassword())).willReturn(encryptedPassword);

    // when
    UserResponse response = userService.registerUser(request);

    // then
    assertEquals(request.getName(), existingUser.getName());
    assertEquals(encryptedPassword, existingUser.getPasswordHash());
    assertFalse(existingUser.isDeleted());
    assertEquals(email, response.getEmail());
    assertEquals(request.getName(), response.getName());
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
        .isDeleted(false)
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
        .isDeleted(false)
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        .willReturn(false);

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.login(request));
    assertEquals(ErrorCode.INCORRECT_PASSWORD, userException.getErrorCode());
  }

  @Test
  void shouldThrowUserExceptionWhenAlreadyDeactivatedUserTryToLogin() {
    // given
    UserLoginRequest request = UserLoginRequest.builder()
        .email("test@example.com")
        .password("password")
        .build();

    User user = User.builder()
        .email(request.getEmail())
        .passwordHash("encodedPassword") // 이미 암호화된 비밀번호
        .role(Role.ROLE_USER)
        .isDeleted(true)
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.login(request));
    assertEquals(ErrorCode.ALREADY_DEACTIVATED_USER, userException.getErrorCode());
  }

  @Test
  void successGetUserById() {
    // given
    UUID userId = UUID.randomUUID();

    User user = spy(User.builder()
        .email("test@example.com")
        .name("John Doe")
        .profilePictureUrl(List.of(new FileDto("url", "filename", "image/jpeg")))
        .build());

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(user.getId()).willReturn(userId);

    // when
    UserResponse response = userService.getUserById(userId);

    // then
    assertEquals(userId, response.getId());
    assertEquals(user.getEmail(), response.getEmail());
    assertEquals(user.getName(), response.getName());
    assertEquals(user.getProfilePictureUrl(), response.getProfilePictureUrl());
    assertEquals(user.getCreatedAt(), response.getCreatedAt());
    assertEquals(user.getUpdatedAt(), response.getUpdatedAt());
  }

  @Test
  void failGetUserById_whenUserIsNotRegistered_throwNotFoundUser() {
    // given
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.getUserById(userId));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void successUpdateUser() {
    // given
    UUID userId = UUID.randomUUID();
    String newName = "UpdatedName";

    List<MultipartFile> profileImages = new ArrayList<>();
    profileImages.add(new MockMultipartFile("image.jpg", "image.jpg",
        "image/jpeg", "image content".getBytes()));

    User user = User.builder()
        .name("oldName")
        .profilePictureUrl(Collections.emptyList())
        .build();

    UserUpdateRequest request = UserUpdateRequest.builder()
        .name(newName)
        .profileImages(profileImages)
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    List<FileDto> uploadedFiles = Collections.singletonList(
        new FileDto("url-to-uploaded-image", "image1.jpg", "image/jpeg"));
    given(s3FileService.uploadManyFiles(anyList(), any(FilePath.class))).willReturn(
        uploadedFiles);

    // when
    UserResponse response = userService.updateUser(userId, request);

    // then
    assertEquals(newName, response.getName());
    assertEquals(uploadedFiles.get(0).getUrl(),
        response.getProfilePictureUrl().get(0).getUrl());
    assertEquals(uploadedFiles.get(0).getFilename(),
        response.getProfilePictureUrl().get(0).getFilename());

    verify(s3FileService).uploadManyFiles(anyList(), any(FilePath.class));
    verify(s3FileService).deleteManyFile(anyList());
  }

  @Test
  void updateUser_userNotFound() {
    // given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = UserUpdateRequest.builder()
        .name("UpdatedName")
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.updateUser(userId, request));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void successUpdateUserWhenNameAndProfileImagesAreNull() {
    // given
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .name("oldName")
        .profilePictureUrl(Collections.emptyList())
        .build();

    UserUpdateRequest request = UserUpdateRequest.builder().build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    UserResponse response = userService.updateUser(userId, request);

    // then
    assertEquals("oldName", response.getName());
    assertEquals(Collections.emptyList(), response.getProfilePictureUrl());
    verify(s3FileService, never()).uploadManyFiles(anyList(), any(FilePath.class));
    verify(s3FileService, never()).deleteManyFile(anyList());
  }

  @Test
  void successWithdrawUser() {
    // given
    UUID userId = UUID.randomUUID();
    User user = spy(User.builder()
        .isDeleted(false)
        .build());

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    userService.withdrawUser(userId);

    // then
    verify(user).deactivateUser(any(LocalDateTime.class));

    assertTrue(user.isDeleted());
    assertNotNull(user.getDeletedAt());
  }

  @Test
  void withdrawUserShouldThrowUserExceptionWhenUserIsNotExist() {
    // given
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.withdrawUser(userId));

    assertEquals(ErrorCode.NOT_FOUND_USER, userException.getErrorCode());
  }

  @Test
  void withdrawUserShouldThrowUserExceptionWhenUserIsAlreadyDeactivated() {
    // given
    UUID userId = UUID.randomUUID();
    User user = spy(User.builder()
        .isDeleted(true)
        .build());

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when & then
    UserException userException = assertThrows(UserException.class,
        () -> userService.withdrawUser(userId));

    assertEquals(ErrorCode.ALREADY_DEACTIVATED_USER, userException.getErrorCode());
  }

  @Test
  void successLogout() {
    // given
    String email = "test@example.com";
    String accessToken = "Bearer access-token";
    String resolvedAccessToken = accessToken.replace("Bearer ", "");

    given(tokenProvider.resolveTokenFromRequest(accessToken)).willReturn(
        resolvedAccessToken);

    // when
    userService.logout(email, accessToken);

    // then
    verify(tokenProvider).resolveTokenFromRequest(accessToken);
    verify(tokenProvider).addBlackList(resolvedAccessToken);
    verify(tokenProvider).deleteRefreshToken(email);
  }

  @Test
  void successReissueToken() {
    // given
    String email = "test@example.com";
    TokenReissueRequest request = new TokenReissueRequest("access-token",
        "refresh-token");
    TokenResponse response = TokenResponse.builder()
        .email(email)
        .accessToken("new-access-token")
        .refreshToken("refresh-token")
        .build();

    given(tokenProvider.regenerateAccessToken(request.getRefreshToken()))
        .willReturn(response);

    // when
    TokenResponse tokenResponse = userService.reissueToken(request);

    // then
    assertEquals(response.getEmail(), tokenResponse.getEmail());
    assertEquals(response.getAccessToken(), tokenResponse.getAccessToken());
    assertEquals(response.getRefreshToken(), tokenResponse.getRefreshToken());
  }

  @Test
  void successLoginBySocialProvider_whenUserExists() {
    // given
    String email = "test@test.com";

    SocialLoginRequest request = SocialLoginRequest.builder()
        .email(email)
        .name("name")
        .socialProvider(SocialProvider.GOOGLE)
        .build();

    User user = User.builder()
        .email(email)
        .role(Role.ROLE_USER)
        .build();

    TokenResponse response = TokenResponse.builder()
        .email(email)
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
    given(userRepository.save(any(User.class))).will(returnsFirstArg());
    given(tokenProvider.generateTokenResponse(user.getEmail(),
        List.of(user.getRole().toString()))).willReturn(response);

    // when
    TokenResponse tokenResponse = userService.loginBySocialProvider(request);

    // then
    assertEquals(response, tokenResponse);
    verify(userRepository).findByEmail(request.getEmail());
    verify(notificationSettingService, never()).activateDailyReminder(any(), any());
    verify(tokenProvider).generateTokenResponse(user.getEmail(),
        List.of(user.getRole().toString()));

  }

  @Test
  void successLoginBySocialProvider_whenUserDoesNotExists() {
    // given
    String email = "test@test.com";

    SocialLoginRequest request = SocialLoginRequest.builder()
        .email(email)
        .name("name")
        .socialProvider(SocialProvider.GOOGLE)
        .build();

    TokenResponse response = TokenResponse.builder()
        .email(email)
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();

    given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());
    given(userRepository.save(any(User.class))).will(returnsFirstArg());
    given(tokenProvider.generateTokenResponse(request.getEmail(),
        List.of(Role.ROLE_USER.toString()))).willReturn(response);

    // when
    TokenResponse tokenResponse = userService.loginBySocialProvider(request);

    // then
    assertEquals(response, tokenResponse);
    verify(userRepository).findByEmail(request.getEmail());
    verify(userRepository).save(any(User.class));
    verify(notificationSettingService).activateDailyReminder(
        any(DailyReminderRequest.class), any());
    verify(tokenProvider).generateTokenResponse(request.getEmail(),
        List.of(Role.ROLE_USER.toString()));

  }

}