package com.example.moodwriter.domain.user.service;

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
import com.example.moodwriter.global.constant.FilePath;
import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.global.jwt.TokenProvider;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.s3.dto.FileDto;
import com.example.moodwriter.global.s3.service.S3FileService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final S3FileService s3FileService;
  private final TokenProvider tokenProvider;
  private final NotificationSettingService notificationSettingService;

  @Transactional
  public UserResponse registerUser(UserRegisterRequest request) {
    User existingUser = userRepository.findByEmail(request.getEmail())
        .orElse(null);

    if (existingUser != null && !existingUser.isDeleted()) {
      throw new UserException(ErrorCode.ALREADY_REGISTERED_USER);
    }

    String encryptedPassword = passwordEncoder.encode(request.getPassword());

    List<FileDto> profilePictureUrl;
    if (request.getProfileImages() == null || request.getProfileImages().isEmpty()) {
      profilePictureUrl = null;
    } else {
      profilePictureUrl = s3FileService.uploadManyFiles(request.getProfileImages(),
          FilePath.PROFILE);
    }

    // 탈퇴한 회원 재가입
    if (existingUser != null && existingUser.isDeleted()) {
      existingUser.reactivate(request, encryptedPassword, profilePictureUrl);
      return UserResponse.fromEntity(existingUser);
    }

    User user = User.from(request, encryptedPassword, profilePictureUrl);

    User savedUser = userRepository.save(user);

    return UserResponse.fromEntity(savedUser);
  }

  @Transactional
  public TokenResponse login(UserLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

    if (user.isDeleted()) {
      throw new UserException(ErrorCode.ALREADY_DEACTIVATED_USER);
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new UserException(ErrorCode.INCORRECT_PASSWORD);
    }

    return tokenProvider.generateTokenResponse(user.getEmail(),
        List.of(user.getRole().toString()));
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

    return UserResponse.fromEntity(user);
  }

  @Transactional
  public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

    if (request.getName() != null) {
      user.updateName(request.getName());
    }

    userRepository.save(user);
    return UserResponse.fromEntity(user);
  }

  @Transactional
  public void withdrawUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

    if (user.isDeleted()) {
      throw new UserException(ErrorCode.ALREADY_DEACTIVATED_USER);
    }

    user.deactivateUser(LocalDateTime.now());
  }

  public void logout(String email, String accessToken) {
    String resolvedAccessToken = tokenProvider.resolveTokenFromRequest(accessToken);
    tokenProvider.addBlackList(resolvedAccessToken);
    tokenProvider.deleteRefreshToken(email);
  }

  public TokenResponse reissueToken(TokenReissueRequest request) {
    return tokenProvider.regenerateAccessToken(
        request.getRefreshToken());
  }

  @Transactional
  public TokenResponse loginBySocialProvider(SocialLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElse(null);

    boolean isFirst = false;
    if (user == null) {
      user = User.from(request);
      isFirst = true;
    }

    User savedUser = userRepository.save(user);

    if (isFirst) {
      notificationSettingService.activateDailyReminder(
          DailyReminderRequest.firstRequest(), savedUser.getId());
    }

    return tokenProvider.generateTokenResponse(savedUser.getEmail(),
        List.of(savedUser.getRole().toString()));
  }
}
