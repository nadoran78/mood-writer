package com.example.moodwriter.user.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.global.service.S3FileService;
import com.example.moodwriter.user.dao.UserRepository;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import com.example.moodwriter.user.dto.UserResponse;
import com.example.moodwriter.user.entity.User;
import com.example.moodwriter.user.exception.UserException;
import java.util.Collections;
import java.util.List;
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

}