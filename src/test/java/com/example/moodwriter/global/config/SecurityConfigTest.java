package com.example.moodwriter.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.global.jwt.TokenProvider;
import com.example.moodwriter.global.security.exception.CustomAccessDeniedHandler;
import com.example.moodwriter.global.security.exception.CustomAuthenticationEntryPoint;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.example.moodwriter.global.security.service.CustomUserDetailService;
import com.example.moodwriter.user.dao.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = TestController.class)
@Import(value = {SecurityConfig.class, JwtAuthenticationFilter.class,
    CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class,
    ObjectMapper.class,
    TokenProvider.class, CustomUserDetailService.class, RedisConfig.class
})
class SecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserRepository userRepository;

  @Test
  void whenAccessProtectedUrlWithoutAuth_then401() throws Exception {
    ResultActions resultActions = mockMvc.perform(get("/api/test")
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding("UTF-8"));
    resultActions.andExpect(status().isUnauthorized())
        .andDo(print());
  }

  @Test
  @WithMockUser
  void whenAccessProtectedUrlWithAuth_then200() throws Exception {
    ResultActions resultActions = mockMvc.perform(get("/api/test"));
    resultActions.andExpect(status().isOk())
        .andDo(print());
  }

  // TODO: 9/26/24 log in 페이지 만든 후 다시 한번 확인
  // 현재 accessDeniedHandler를 타지 않고 GlobalExceptionHandler에서 처리됨.
  // 따라서 403을 반환하는 것이 아니라 500을 반환함.
  // test 단계에서만 그런지 실제 api 접속할 때도 이와 같은 지 확인 필요.
//  @Test
//  @WithMockUser(username = "admin", roles = "ADMIN")
//  void whenAccessUserUrlWithAdminRole_then403() throws Exception {
//    ResultActions resultActions = mockMvc.perform(get("/api/test"));
//    resultActions.andExpect(status().isForbidden())
//        .andDo(print());
//  }

}

@RestController
class TestController {

  @GetMapping("/api/test")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> testEndpoint() {
    return ResponseEntity.ok("Protected Resource");
  }
}
