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

}

@RestController
class TestController {

  @GetMapping("/api/test")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> testEndpoint() {
    return ResponseEntity.ok("Protected Resource");
  }
}
