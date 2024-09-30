package com.example.moodwriter.user.controller;

import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.user.dto.UserLoginRequest;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import com.example.moodwriter.user.dto.UserResponse;
import com.example.moodwriter.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<UserResponse> registerUser(@Valid @ModelAttribute
      UserRegisterRequest request) {

    UserResponse response = userService.registerUser(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody @Valid UserLoginRequest request) {
    TokenResponse tokenResponse = userService.login(request);
    return ResponseEntity.ok(tokenResponse);
  }

  @GetMapping
  public ResponseEntity<UserResponse> getUserById(@AuthenticationPrincipal
      CustomUserDetails userDetails) {
    UserResponse response = userService.getUserById(userDetails.getId());
    return ResponseEntity.ok(response);
  }


}
