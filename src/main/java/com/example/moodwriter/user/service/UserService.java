package com.example.moodwriter.user.service;

import com.example.moodwriter.global.constant.FilePath;
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

  @Transactional
  public UserResponse registerUser(UserRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
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

    User user = User.from(request, encryptedPassword, profilePictureUrl);

    User savedUser = userRepository.save(user);

    return UserResponse.fromEntity(savedUser);
  }

  public TokenResponse login(UserLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

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
}
