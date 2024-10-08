package com.example.moodwriter.global.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.global.constant.Role;
import com.example.moodwriter.global.jwt.JwtAuthenticationToken;
import com.example.moodwriter.global.jwt.TokenProvider;
import com.example.moodwriter.global.security.dto.CustomUserDetails;
import com.example.moodwriter.global.security.exception.CustomAccessDeniedHandler;
import com.example.moodwriter.global.security.exception.CustomAuthenticationEntryPoint;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.example.moodwriter.user.controller.UserController;
import com.example.moodwriter.user.entity.User;
import com.example.moodwriter.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = UserController.class)
@Import(value = {SecurityConfig.class, JwtAuthenticationFilter.class,
    CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class,
    ObjectMapper.class
})
class SecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TokenProvider tokenProvider;

  @MockBean
  private UserService userService;

  @Test
  void successCustomAuthenticationEntryPointWithDeletedUser() throws Exception {

    given(tokenProvider.resolveTokenFromRequest(anyString())).willReturn("valid-token");
    given(tokenProvider.validateToken(anyString())).willReturn(true);
    given(tokenProvider.isAccessTokenDenied(anyString())).willReturn(false);
    given(tokenProvider.checkUserIsDeletedByToken(anyString())).willReturn(true);

    ResultActions resultActions = mockMvc.perform(get("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "valid-token")
        .characterEncoding("UTF-8"));
    resultActions.andExpect(status().isForbidden())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("ALREADY_DEACTIVATED_USER"))
        .andExpect(jsonPath("$.message").value("이미 탈퇴한 회원입니다."))
        .andExpect(jsonPath("$.path").value("/api/users"));

  }

  @Test
  void successCustomAuthenticationEntryPointWithInvalidToken() throws Exception {

    given(tokenProvider.resolveTokenFromRequest(anyString())).willReturn("invalid-token");
    given(tokenProvider.validateToken(anyString())).willReturn(false);

    ResultActions resultActions = mockMvc.perform(get("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "valid-token")
        .characterEncoding("UTF-8"));
    resultActions.andExpect(status().isUnauthorized())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("NEED_TO_SIGN_IN"))
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
        .andExpect(jsonPath("$.path").value("/api/users"));

  }

  @Test
  void successCustomAuthenticationEntryPointWithAccessTokenAddedBlackList()
      throws Exception {

    given(tokenProvider.resolveTokenFromRequest(anyString())).willReturn(
        "blacklist-token");
    given(tokenProvider.validateToken(anyString())).willReturn(true);
    given(tokenProvider.isAccessTokenDenied(anyString())).willReturn(true);

    ResultActions resultActions = mockMvc.perform(get("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "valid-token")
        .characterEncoding("UTF-8"));
    resultActions.andExpect(status().isUnauthorized())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("NEED_TO_SIGN_IN"))
        .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
        .andExpect(jsonPath("$.path").value("/api/users"));

  }

  @Test
  void successCustomAccessDeniedHandler() throws Exception {
    User user = User.builder()
        .email("test@example.com")
        .role(Role.ROLE_ADMIN)
        .isDeleted(false)
        .build();
    UserDetails userDetails = new CustomUserDetails(user);
    Authentication authentication = new JwtAuthenticationToken(userDetails, "",
        userDetails.getAuthorities());

    given(tokenProvider.resolveTokenFromRequest(anyString())).willReturn("valid-token");
    given(tokenProvider.validateToken(anyString())).willReturn(true);
    given(tokenProvider.isAccessTokenDenied(anyString())).willReturn(false);
    given(tokenProvider.checkUserIsDeletedByToken(anyString())).willReturn(false);
    given(tokenProvider.getAuthentication(anyString())).willReturn(authentication);

    ResultActions resultActions = mockMvc.perform(get("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "valid-token")
        .characterEncoding("UTF-8"));
    resultActions.andExpect(status().isForbidden())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
        .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
        .andExpect(jsonPath("$.path").value("/api/users"));

  }

}


