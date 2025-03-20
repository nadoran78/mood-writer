package com.example.moodwriter.domain.user.controller;

import com.example.moodwriter.domain.user.dto.LogoutResponse;
import com.example.moodwriter.domain.user.dto.SocialLoginRequest;
import com.example.moodwriter.domain.user.dto.TokenReissueRequest;
import com.example.moodwriter.domain.user.dto.UserLoginRequest;
import com.example.moodwriter.domain.user.dto.UserRegisterRequest;
import com.example.moodwriter.domain.user.dto.UserResponse;
import com.example.moodwriter.domain.user.dto.UserUpdateRequest;
import com.example.moodwriter.domain.user.service.UserService;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
  public ResponseEntity<TokenResponse> login(
      @RequestBody @Valid UserLoginRequest request) {
    TokenResponse tokenResponse = userService.login(request);
    return ResponseEntity.ok(tokenResponse);
  }

  @GetMapping
  public ResponseEntity<UserResponse> getUserById(@AuthenticationPrincipal
  CustomUserDetails userDetails) {
    UserResponse response = userService.getUserById(userDetails.getId());
    return ResponseEntity.ok(response);
  }

  @PatchMapping
  public ResponseEntity<UserResponse> updateUserInfo(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @ModelAttribute UserUpdateRequest request) {
    UserResponse response = userService.updateUser(userDetails.getId(), request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Void> withdrawUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    userService.withdrawUser(userDetails.getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/logout")
  public ResponseEntity<LogoutResponse> logout(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestHeader("Authorization") String accessToken) {
    userService.logout(userDetails.getUsername(), accessToken);
    return ResponseEntity.ok(new LogoutResponse("성공"));
  }

  @PostMapping("/reissue-token")
  public ResponseEntity<TokenResponse> reissueToken(
      @RequestBody @Valid TokenReissueRequest request) {
    TokenResponse response = userService.reissueToken(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/social-login")
  public ResponseEntity<TokenResponse> loginBySocialProvider(@RequestBody @Valid
      SocialLoginRequest request) {
    TokenResponse tokenResponse = userService.loginBySocialProvider(request);
    return ResponseEntity.ok(tokenResponse);
  }
}
