package com.example.moodwriter.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.global.dto.FileDto;
import com.example.moodwriter.user.dto.UserRegisterRequest;
import com.example.moodwriter.user.dto.UserResponse;
import com.example.moodwriter.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  void successRegisterUser() throws Exception {
    // given
    List<FileDto> profileImages = List.of(
        new FileDto("https://example.com/file1.jpg", "file1.jpg"),
        new FileDto("https://example.com/file2.jpg", "file2.jpg")
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
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(response.getId().toString()))
        .andExpect(jsonPath("$.email").value(response.getEmail()))
        .andExpect(jsonPath("$.name").value(response.getName()))
        .andExpect(
            jsonPath("$.profilePictureUrl[0].url").value("https://example.com/file1.jpg"))
        .andExpect(
            jsonPath("$.profilePictureUrl[1].url").value("https://example.com/file2.jpg"))
        .andExpect(jsonPath("$.profilePictureUrl[0].filename").value("file1.jpg"))
        .andExpect(jsonPath("$.profilePictureUrl[1].filename").value("file2.jpg"));
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
        .andExpect(jsonPath("$.fieldErrors[0].message").value("암호는 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하여야 합니다."));
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
            .file(new MockMultipartFile("profileImages", "invalid-file.txt", "text/txt", "invalid-file".getBytes()))
            .with(csrf()))
      .andExpect(status().isBadRequest())
      .andDo(print())
      .andExpect(jsonPath("$.fieldErrors").exists());
  }

  private MockMultipartFile createMockImage(String filename) {
    return new MockMultipartFile("profileImages", filename, "image/png",
        "test-image-file.png".getBytes());
  }

}