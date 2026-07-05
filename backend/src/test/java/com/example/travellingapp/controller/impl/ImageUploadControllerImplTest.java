package com.example.travellingapp.controller.impl;

import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.response_template.ResponseBody;
import com.example.travellingapp.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageUploadControllerImplTest {

    private ImageUploadService imageUploadService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        imageUploadService = mock(ImageUploadService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ImageUploadControllerImpl(imageUploadService))
                .build();
    }

    @Test
    void uploadImage_shouldReturnUploadedImageResponseWithPublicId_whenServiceSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E000",
                "Search info successfully",
                COMMON.name(),
                Map.of(
                        "imageUrl", "https://res.cloudinary.com/demo/image/upload/v123/wandermate/profile-images/users/1/profile-1-abc.png",
                        "publicId", "wandermate/profile-images/users/1/profile-1-abc",
                        "fileName", "wandermate/profile-images/users/1/profile-1-abc",
                        "imageType", "profile-images"
                )
        );

        when(imageUploadService.uploadImage(
                any(MultipartFile.class),
                eq("profile-images")
        )).thenReturn(new CompleteResponse<>(responseBody, 200));

        mockMvc.perform(multipart("/api/v1/uploads/images")
                        .file(file)
                        .param("imageType", "profile-images")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("E000"))
                .andExpect(jsonPath("$.message").value("Search info successfully"))
                .andExpect(jsonPath("$.flow").value(COMMON.name()))
                .andExpect(jsonPath("$.body.imageUrl").value("https://res.cloudinary.com/demo/image/upload/v123/wandermate/profile-images/users/1/profile-1-abc.png"))
                .andExpect(jsonPath("$.body.publicId").value("wandermate/profile-images/users/1/profile-1-abc"))
                .andExpect(jsonPath("$.body.fileName").value("wandermate/profile-images/users/1/profile-1-abc"))
                .andExpect(jsonPath("$.body.imageType").value("profile-images"));

        verify(imageUploadService).uploadImage(any(MultipartFile.class), eq("profile-images"));
    }

    @Test
    void uploadImage_shouldReturnBadRequest_whenServiceReturnsBadRequestResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.txt",
                "text/plain",
                "not-an-image".getBytes()
        );

        ResponseBody<Object> responseBody = new ResponseBody<>(
                "E001",
                "Invalid input",
                COMMON.name(),
                null
        );

        when(imageUploadService.uploadImage(
                any(MultipartFile.class),
                eq("profile-images")
        )).thenReturn(new CompleteResponse<>(responseBody, 400));

        mockMvc.perform(multipart("/api/v1/uploads/images")
                        .file(file)
                        .param("imageType", "profile-images")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E001"))
                .andExpect(jsonPath("$.message").value("Invalid input"))
                .andExpect(jsonPath("$.flow").value(COMMON.name()));

        verify(imageUploadService).uploadImage(any(MultipartFile.class), eq("profile-images"));
    }
}
