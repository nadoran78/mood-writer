package com.example.moodwriter.diary.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moodwriter.diary.dto.DiaryImageUploadResponse;
import com.example.moodwriter.diary.service.DiaryImageService;
import com.example.moodwriter.global.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(controllers = DiaryImageController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class)})
@AutoConfigureMockMvc(addFilters = false)
class DiaryImageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private DiaryImageService diaryImageService;

  @Test
  void successUploadImages() throws Exception {
    // given
    MockMultipartFile imageFile1 = createMockImage("image1.jpg");
    MockMultipartFile imageFile2 = createMockImage("image2.jpg");

    DiaryImageUploadResponse response = DiaryImageUploadResponse.builder()
        .imageUrls(
            List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg"))
        .message("이미지가 성공적으로 업로드되었습니다.")
        .build();

    given(diaryImageService.uploadDiaryImages(List.of(imageFile1, imageFile2)))
        .willReturn(response);

    // when & then
    mockMvc.perform(multipart("/api/diaries/images")
            .file(imageFile1)
            .file(imageFile2))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.imageUrls[0]").value("https://example.com/image1.jpg"))
        .andExpect(jsonPath("$.imageUrls[1]").value("https://example.com/image2.jpg"))
        .andExpect(jsonPath("$.message").value("이미지가 성공적으로 업로드되었습니다."));
  }

  private MockMultipartFile createMockImage(String filename) {
    return new MockMultipartFile(
        "images", filename, MediaType.IMAGE_JPEG_VALUE,
        filename.replace(".", "").getBytes());
  }

  @Test
  void uploadImages_shouldReturnBadRequest_whenMoreThanMaxFileUploaded()
      throws Exception {
    // given
    List<MockMultipartFile> mockMultipartFileList = new ArrayList<>();

    for (int i = 1; i <= 6; i++) {
      mockMultipartFileList.add(createMockImage("image" + i + ".jpg"));
    }

    // when & then
    MockMultipartHttpServletRequestBuilder multipartRequest = multipart(
        "/api/diaries/images");
    for (MockMultipartFile multipartFile : mockMultipartFileList) {
      multipartRequest.file(multipartFile);
    }

    mockMvc.perform(multipartRequest)
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/diaries/images"))
        .andExpect(jsonPath("$.parameterErrors[0].parameter").value("imageFiles"))
        .andExpect(jsonPath("$.parameterErrors[0].messages[0]").value(
            "한 번에 업로드할 수 있는 파일의 수는 최대 5개입니다."));
  }

  @Test
  void uploadImages_shouldReturnBadRequest_whenInvalidFileUploaded()
      throws Exception {
    // given
    MockMultipartFile invalidFile = new MockMultipartFile("images",
        "invalid-file.txt", "text/txt", "invalid-file".getBytes());

    // when & then
    mockMvc.perform(multipart("/api/diaries/images")
            .file(invalidFile))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.path").value("/api/diaries/images"))
        .andExpect(jsonPath("$.parameterErrors[0].parameter").value("imageFiles"))
        .andExpect(jsonPath("$.parameterErrors[0].messages[0]").value(
            "유효한 파일이 아닙니다."));
  }

  @Test
  void uploadImages_shouldReturnBadRequest_whenUploadNoFile() throws Exception {
    mockMvc.perform(multipart("/api/diaries/images"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }
}