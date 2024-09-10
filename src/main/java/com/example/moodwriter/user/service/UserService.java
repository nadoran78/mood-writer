package com.example.moodwriter.user.service;

import com.example.moodwriter.global.exception.code.ErrorCode;
import com.example.moodwriter.user.dao.UserRepository;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import com.example.moodwriter.user.dto.UserRegisterResponse;
import com.example.moodwriter.user.entity.User;
import com.example.moodwriter.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserRegisterResponse registerUser(UserRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserException(ErrorCode.ALREADY_REGISTERED_USER);
    }

    String encryptedPassword = passwordEncoder.encode(request.getPassword());

    User user = User.from(request, encryptedPassword);

    User savedUser = userRepository.save(user);

    return UserRegisterResponse.fromEntity(savedUser);
  }
}
