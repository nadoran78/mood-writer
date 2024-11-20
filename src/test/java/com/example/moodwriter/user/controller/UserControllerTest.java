package com.example.moodwriter.user.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.domain.user.controller.UserController;
import com.example.moodwriter.domain.user.dto.SocialLoginRequest;
import com.example.moodwriter.domain.user.dto.TokenReissueRequest;
import com.example.moodwriter.domain.user.dto.UserLoginRequest;
import com.example.moodwriter.domain.user.dto.UserRegisterRequest;
import com.example.moodwriter.domain.user.dto.UserResponse;
import com.example.moodwriter.domain.user.dto.UserUpdateRequest;
import com.example.moodwriter.domain.user.entity.User;
import com.example.moodwriter.domain.user.service.UserService;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.jwt.dto.TokenResponse;
import com.example.moodwriter.global.s3.dto.FileDto;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = UserController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  private final UUID userId = UUID.randomUUID();

  private final String email = "test@example.com";

  @BeforeEach
  void setup() {
    User user = mock(User.class);
    CustomUserDetails userDetails = mock(CustomUserDetails.class);

    when(user.getId()).thenReturn(userId);
    when(userDetails.getId()).thenReturn(userId);
    when(userDetails.getUsername()).thenReturn(email);

    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(
        new JwtAuthenticationToken(userDetails, "", null));
  }

  @Test
  void successRegisterUser() throws Exception {
    // given
    List<FileDto> profileImages = List.of(
        new FileDto("https://example.com/file1.jpg", "file1.jpg", "image/jpeg"));

    UserResponse response = UserResponse.builder()
        .id(UUID.randomUUID())
        .email("user@example.com")
        .name("example")
        .profilePictureUrl(profileImages)
        .createdAt(LocalDateTime.now())
        .build();

    // when
    given(userService.registerUser(any(UserRegisterRequest.class))).willReturn(response);

    // then
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "Password123!".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.getId().toString()))
        .andExpect(jsonPath("$.email").value(response.getEmail()))
        .andExpect(jsonPath("$.name").value(response.getName()))
        .andExpect(
            jsonPath("$.profilePictureUrl[0].url").value("https://example.com/file1.jpg"))
        .andExpect(jsonPath("$.profilePictureUrl[0].filename").value("file1.jpg"));
  }

  @Test
  void successRegisterUserWithProfileImageNull() throws Exception {
    // given
    UserResponse response = UserResponse.builder()
        .id(UUID.randomUUID())
        .email("user@example.com")
        .name("example")
        .createdAt(LocalDateTime.now())
        .build();

    // when
    given(userService.registerUser(any(UserRegisterRequest.class))).willReturn(response);

    // then
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "Password123!".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .with(csrf()))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.id").value(response.getId().toString()))
        .andExpect(jsonPath("$.email").value(response.getEmail()))
        .andExpect(jsonPath("$.name").value(response.getName()));
  }

  @Test
  void failRegisterUserWithImageFileOver2() throws Exception {
    // given
    List<FileDto> profileImages = List.of(
        new FileDto("https://example.com/file1.jpg", "file1.jpg", "image/jpeg"),
        new FileDto("https://example.com/file2.jpg", "file2.jpg", "image/jpeg")
    );

    UserResponse response = UserResponse.builder()
        .id(UUID.randomUUID())
        .email("user@example.com")
        .name("example")
        .profilePictureUrl(profileImages)
        .createdAt(LocalDateTime.now())
        .build();

    // when
    given(userService.registerUser(any(UserRegisterRequest.class))).willReturn(response);

    // then
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "Password123!".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .file(createMockImage("profileImage2.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("profileImages"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("프로필 이미지는 1장만 업데이트 가능합니다."));
  }

  @Test
  void failRegisterUserWithInvalidEmail() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "invalid-email".getBytes()))
                .part(new MockPart("password", "Password123!".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("email"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("올바른 이메일 형식이 아닙니다."));
  }

  @Test
  void failRegisterUserWithEmailIsNull() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("password", "Password123!".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("email"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("이메일은 반드시 입력해야 합니다."));
  }

  @Test
  void failRegisterUserWithInvalidPassword() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "invalid-password".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("password"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value(
            "암호는 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하여야 합니다."));
  }

  @Test
  void failRegisterUserWithPasswordIsNull() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("password"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("비밀번호는 반드시 입력해야 합니다."));
  }

  @Test
  void failRegisterUserWithBlankName() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "Password123!@".getBytes()))
                .part(new MockPart("name", "".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("이름을 빈칸으로 입력할 수 없습니다."));
  }

  @Test
  void failRegisterUserWithNameSizeOver10() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "Password123!@".getBytes()))
                .part(new MockPart("name", "12345678910".getBytes()))
                .file(createMockImage("profileImage1.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("이름은 10자 이하여야 합니다."));
  }

  @Test
  void failRegisterUserWithInvalidFile() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/users/register")
                .part(new MockPart("email", "user@example.com".getBytes()))
                .part(new MockPart("password", "Password123!".getBytes()))
                .part(new MockPart("name", "example".getBytes()))
                .file(new MockMultipartFile("profileImages", "invalid-file.txt", "text/txt",
                    "invalid-file".getBytes()))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors").exists());
  }

  private MockMultipartFile createMockImage(String filename) {
    return new MockMultipartFile("profileImages", filename, "image/png",
        "test-image-file.png".getBytes());
  }

  @Test
  void successLogin() throws Exception {
    // given
    String testEmail = "test@example.com";

    UserLoginRequest request = UserLoginRequest.builder()
        .email(testEmail)
        .password("Password1!")
        .build();

    TokenResponse tokenResponse = TokenResponse.builder()
        .email(testEmail)
        .accessToken("access-token")
        .refreshToken("refresh-token")
        .build();

    given(userService.login(any(UserLoginRequest.class))).willReturn(tokenResponse);

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(testEmail))
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
        .andDo(print());
  }

  @Test
  void failLogin_whenInvalidRequest_thenReturnBadRequest()
      throws Exception {
    // given
    UserLoginRequest invalidRequest = UserLoginRequest.builder()
        .email("invalid-email")
        .password("invalid-password")
        .build();

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/users/login"))
        .andExpect(jsonPath("$.fieldErrors[0].field").exists())
        .andExpect(jsonPath("$.fieldErrors[0].message").exists())
        .andExpect(jsonPath("$.fieldErrors[1].field").exists())
        .andExpect(jsonPath("$.fieldErrors[1].message").exists());
  }

  @Test
  void failLogin_whenNullRequest_thenReturnBadRequest()
      throws Exception {
    // given
    UserLoginRequest invalidRequest = UserLoginRequest.builder().build();

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/users/login"))
        .andExpect(jsonPath("$.fieldErrors[0].field").exists())
        .andExpect(jsonPath("$.fieldErrors[0].message").exists())
        .andExpect(jsonPath("$.fieldErrors[1].field").exists())
        .andExpect(jsonPath("$.fieldErrors[1].message").exists());
  }

  @Test
  void successGetUserById() throws Exception {
    // given
    UserResponse userResponse = UserResponse.builder()
        .id(userId)
        .email("user@example.com")
        .name("John Doe")
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now())
        .build();

    given(userService.getUserById(userId)).willReturn(userResponse);

    // when & then
    mockMvc.perform(get("/api/users")) // 사용자 정보를 주입
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("user@example.com"))
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andDo(print());
  }

  @Test
  void successUpdateUserInfo() throws Exception {
    // given
    String updateName = "NewName";
    String filename = "profileImage.jpg";

    UserResponse userResponse = UserResponse.builder()
        .id(userId)
        .name(updateName)
        .profilePictureUrl(
            List.of(new FileDto("https://image.url", filename, "image/jpeg")))
        .build();

    given(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class)))
        .willReturn(userResponse);

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", updateName.getBytes()))
            .file(createMockImage(filename))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(updateName))
        .andExpect(
            jsonPath("$.profilePictureUrl[0].url").value(
                userResponse.getProfilePictureUrl().get(0).getUrl()))
        .andExpect(jsonPath("$.profilePictureUrl[0].filename").value(filename));
  }

  @Test
  void successUpdateUserInfoWithNameIsNull() throws Exception {
    // given
    String oldName = "oldName";
    String filename = "profileImage.jpg";

    UserResponse userResponse = UserResponse.builder()
        .id(userId)
        .name(oldName)
        .profilePictureUrl(
            List.of(new FileDto("https://image.url", filename, "image/jpeg")))
        .build();

    given(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class)))
        .willReturn(userResponse);

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .file(createMockImage(filename))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(oldName))
        .andExpect(
            jsonPath("$.profilePictureUrl[0].url").value(
                userResponse.getProfilePictureUrl().get(0).getUrl()))
        .andExpect(jsonPath("$.profilePictureUrl[0].filename").value(filename));
  }

  @Test
  void successUpdateUserInfoWithFileIsNull() throws Exception {
    // given
    String updateName = "NewName";
    String oldFilename = "profileImage.jpg";

    UserResponse userResponse = UserResponse.builder()
        .id(userId)
        .name(updateName)
        .profilePictureUrl(
            List.of(new FileDto("https://image.url", oldFilename, "image/jpeg")))
        .build();

    given(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class)))
        .willReturn(userResponse);

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", updateName.getBytes()))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(updateName))
        .andExpect(
            jsonPath("$.profilePictureUrl[0].url").value(
                userResponse.getProfilePictureUrl().get(0).getUrl()))
        .andExpect(jsonPath("$.profilePictureUrl[0].filename").value(oldFilename));
  }

  @Test
  void successUpdateUserInfoWithNameAndFileIsNull() throws Exception {
    // given
    String oldName = "oldName";
    String oldFilename = "profileImage.jpg";

    UserResponse userResponse = UserResponse.builder()
        .id(userId)
        .name(oldName)
        .profilePictureUrl(
            List.of(new FileDto("https://image.url", oldFilename, "image/jpeg")))
        .build();

    given(userService.updateUser(any(UUID.class), any(UserUpdateRequest.class)))
        .willReturn(userResponse);

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(oldName))
        .andExpect(
            jsonPath("$.profilePictureUrl[0].url").value(
                userResponse.getProfilePictureUrl().get(0).getUrl()))
        .andExpect(jsonPath("$.profilePictureUrl[0].filename").value(oldFilename));
  }

  @Test
  void failUpdateUserInfoWithNameIsEmptyString() throws Exception {
    // given
    String emptyName = "";

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", emptyName.getBytes()))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value(
                "이름의 시작, 끝, 전체를 공백으로 입력할 수 없습니다."));

  }

  @Test
  void failUpdateUserInfoWithNameStartsWithSpace() throws Exception {
    // given
    String invalidName = " 이름";

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", invalidName.getBytes()))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value(
                "이름의 시작, 끝, 전체를 공백으로 입력할 수 없습니다."));

  }

  @Test
  void failUpdateUserInfoWithNameEndsWithSpace() throws Exception {
    // given
    String invalidName = "이름 ";

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", invalidName.getBytes()))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value(
                "이름의 시작, 끝, 전체를 공백으로 입력할 수 없습니다."));

  }

  @Test
  void failUpdateUserInfoWithNameIsOnlySpace() throws Exception {
    // given
    String invalidName = "   ";

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", invalidName.getBytes()))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value(
                "이름의 시작, 끝, 전체를 공백으로 입력할 수 없습니다."));

  }

  @Test
  void failUpdateUserInfoWithNameIsOver10() throws Exception {
    // given
    String invalidName = "12345678910";

    // when & then
    mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
            .part(new MockPart("name", invalidName.getBytes()))
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value(
                "이름은 10자 이하여야 합니다."));

  }

  @Test
  void failUpdateUserWithInvalidFile() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
                .file(new MockMultipartFile("profileImages", "invalid-file.txt", "text/txt",
                    "invalid-file".getBytes()))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("profileImages"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value(
                "유효한 파일이 아닙니다."));
  }

  @Test
  void failUpdateUserWithFileSizeIsOver2() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/users")
                .file(createMockImage("profileImage1.jpg"))
                .file(createMockImage("profileImage2.jpg"))
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("profileImages"))
        .andExpect(
            jsonPath("$.fieldErrors[0].message").value("프로필 이미지는 1장만 업데이트 가능합니다."));
  }

  @Test
  void successWithdrawUser() throws Exception {
    mockMvc.perform(delete("/api/users"))
        .andExpect(status().isNoContent());

    verify(userService).withdrawUser(userId);
  }

  @Test
  void successLogout() throws Exception {
    // given
    String accessToken = "Bearer access-token";

    // when & then
    mockMvc.perform(post("/api/users/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andDo(print());

    verify(userService).logout(email, accessToken);
  }

  @Test
  void successReissueToken() throws Exception {
    // given
    String accessToken = "Bearer access-token";
    TokenReissueRequest request = new TokenReissueRequest("accessToken", "refresh-token");
    TokenResponse response = TokenResponse.builder()
        .email(email)
        .accessToken("new-access-token")
        .refreshToken("refresh-token")
        .build();

    given(userService.reissueToken(any(TokenReissueRequest.class))).willReturn(response);

    // when & then
    mockMvc.perform(post("/api/users/reissue-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", accessToken))
        .andExpect(jsonPath("$.email").value(response.getEmail()))
        .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
        .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()));
  }

  @Test
  void reissueTokenShouldThrowCustomExceptionWhenRefreshTokenIsNull() throws Exception {
    // given
    String accessToken = "Bearer access-token";
    TokenReissueRequest request = new TokenReissueRequest(null, null);

    // when & then
    mockMvc.perform(post("/api/users/reissue-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", accessToken))
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/users/reissue-token"))
        .andExpect(jsonPath("$.fieldErrors", hasSize(2)))
        .andExpect(jsonPath("$.fieldErrors", hasItems(
            allOf(
                hasEntry("field", "accessToken"),
                hasEntry("message", "액세스 토큰을 입력해주세요.")
            ),
            allOf(
                hasEntry("field", "refreshToken"),
                hasEntry("message", "리프레쉬 토큰을 입력해주세요.")
            )
        )));

  }

  @Test
  void reissueTokenShouldThrowCustomExceptionWhenRefreshTokenIsEmpty() throws Exception {
    // given
    String accessToken = "Bearer access-token";
    TokenReissueRequest request = new TokenReissueRequest("", "");

    // when & then
    mockMvc.perform(post("/api/users/reissue-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .header("Authorization", accessToken))
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/users/reissue-token"))
        .andExpect(jsonPath("$.fieldErrors", hasSize(2)))
        .andExpect(jsonPath("$.fieldErrors", hasItems(
            allOf(
                hasEntry("field", "accessToken"),
                hasEntry("message", "액세스 토큰을 입력해주세요.")
            ),
            allOf(
                hasEntry("field", "refreshToken"),
                hasEntry("message", "리프레쉬 토큰을 입력해주세요.")
            )
        )));

  }

  @Test
  void successLoginBySocialProvider() throws Exception {
    // given
    String jsonRequest = """
        {
           "email": "test@test.com",
           "name": "name",
           "socialProvider": "google"
        }
        """;

    TokenResponse tokenResponse = TokenResponse.builder()
        .email("test@test.com")
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();

    given(userService.loginBySocialProvider(any(SocialLoginRequest.class)))
        .willReturn(tokenResponse);

    // when & then
    mockMvc.perform(post("/api/users/social-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(jsonPath("$.email").value(tokenResponse.getEmail()))
        .andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
        .andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken()));
  }

  @Test
  void loginBySocialProvider_throwValidationError_whenCallWithInvalidRequest()
      throws Exception {
    // given
    String jsonRequest = """
        {
           "email": "test@testcom",
           "name": null,
           "socialProvider": null
        }
        """;

    // when & then
    mockMvc.perform(post("/api/users/social-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.fieldErrors", hasSize(3)))
        .andExpect(jsonPath("$.fieldErrors", hasItems(
            allOf(
                hasEntry("field", "email"),
                hasEntry("message", "올바른 이메일 형식이 아닙니다.")
            ),
            allOf(
                hasEntry("field", "socialProvider"),
                hasEntry("message", "어떤 소셜네트워크서비스를 통해 로그인하였는지 입력하여 주세요.")
            ),
            allOf(
                hasEntry("field", "name"),
                hasEntry("message", "이름을 입력하여 주세요.")
            )
        )));
  }

  @Test
  void loginBySocialProvider_throwHttpMessageNotReadableException_whenCallWithInvalidSocialProvider()
      throws Exception {
    // given
    String jsonRequest = """
        {
           "email": "test@test.com",
           "name": "name",
           "socialProvider": "kakao"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/users/social-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("HTTP_MESSAGE_NOT_READABLE"))
        .andExpect(jsonPath("$.message").value("HTTP 메시지를 읽을 수 없습니다."))
        .andExpect(jsonPath("$.path").value("/api/users/social-login"));

  }

}