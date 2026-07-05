package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.response.ImageUploadResponseDTO;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.travellingapp.enums.CommonEnum.COMMON;
import static com.example.travellingapp.enums.ErrorCodeEnum.INTERNAL_SERVER_ERROR;
import static com.example.travellingapp.enums.ErrorCodeEnum.INVALID_INPUT;
import static com.example.travellingapp.enums.ErrorCodeEnum.SEARCH_INFO_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceImplTest {

    @Mock
    private ErrorCodeRepository errorCodeRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private CloudinaryImageClient cloudinaryImageClient;

    private ImageUploadServiceImpl imageUploadService;

    private static final String USERNAME = "JustinBo123";

    @BeforeEach
    void setUp() {
        imageUploadService = new ImageUploadServiceImpl(
                errorCodeRepository,
                authenticatedUserProvider,
                cloudinaryImageClient,
                "wandermate"
        );
    }

    @Test
    void uploadImage_shouldUploadProfileImageToCloudinary_whenFileIsValid() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        ImageUploadResponseDTO cloudinaryResponse = new ImageUploadResponseDTO(
                "https://res.cloudinary.com/demo/image/upload/v123/wandermate/profile-images/justinbo123-avatar.png",
                "wandermate/profile-images/justinbo123-avatar",
                "profile-images"
        );

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());
        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(cloudinaryImageClient.uploadImage(
                any(byte[].class),
                startsWith("justinbo123-"),
                eq("wandermate/profile-images"),
                eq("profile-images")
        )).thenReturn(cloudinaryResponse);

        CompleteResponse<Object> response = imageUploadService.uploadImage(file, "profile-images");

        assertThat(response.getHttpCode()).isEqualTo(200);
        assertThat(response.getResponseBody().getCode()).isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        assertThat(response.getResponseBody().getFlow()).isEqualTo(COMMON.name());
        assertThat(response.getResponseBody().getBody()).isEqualTo(cloudinaryResponse);

        verify(cloudinaryImageClient).uploadImage(
                any(byte[].class),
                startsWith("justinbo123-"),
                eq("wandermate/profile-images"),
                eq("profile-images")
        );
    }

    @Test
    void uploadImage_shouldUploadTripCoverAndNormalizeImageType_whenImageTypeHasExtraSpacesAndUppercase() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpeg",
                "image/jpeg",
                "fake-cover-content".getBytes()
        );

        ImageUploadResponseDTO cloudinaryResponse = new ImageUploadResponseDTO(
                "https://res.cloudinary.com/demo/image/upload/v123/wandermate/trip-covers/justinbo123-cover.jpg",
                "wandermate/trip-covers/justinbo123-cover",
                "trip-covers"
        );

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());
        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(cloudinaryImageClient.uploadImage(
                any(byte[].class),
                startsWith("justinbo123-"),
                eq("wandermate/trip-covers"),
                eq("trip-covers")
        )).thenReturn(cloudinaryResponse);

        CompleteResponse<Object> response = imageUploadService.uploadImage(file, "  TRIP-COVERS  ");

        ImageUploadResponseDTO body = (ImageUploadResponseDTO) response.getResponseBody().getBody();

        assertThat(body.getImageType()).isEqualTo("trip-covers");
        assertThat(body.getImageUrl()).contains("res.cloudinary.com");
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenFileIsNull() throws IOException {
        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(null, "profile-images")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
        verify(errorCodeRepository, never()).findByErrorEnumAndFlow(anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenFileIsEmpty() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenImageTypeIsUnsupported() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "activity-images")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenContentTypeIsUnsupported() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "not-an-image".getBytes()
        );

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenFileIsLargerThanFiveMb() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                new byte[(5 * 1024 * 1024) + 1]
        );

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInternalServerError_whenFileCannotBeRead() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenThrow(new IOException("Cannot read uploaded file"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    @Test
    void uploadImage_shouldThrowInternalServerError_whenCloudinaryUploadFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(cloudinaryImageClient.uploadImage(
                any(byte[].class),
                startsWith("justinbo123-"),
                eq("wandermate/profile-images"),
                eq("profile-images")
        )).thenThrow(new IOException("Cloudinary unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    private void mockErrorCode(ErrorCodeEnum errorCodeEnum, String flow) {
        ErrorCodeEntity entity = new ErrorCodeEntity();
        entity.setErrorCode(errorCodeEnum.getCode());
        entity.setErrorMessage(errorCodeEnum.getMessage());
        entity.setErrorEnum(errorCodeEnum.name());
        entity.setFlow(flow);
        entity.setCreatedDate(LocalDateTime.now());

        when(errorCodeRepository.findByErrorEnumAndFlow(errorCodeEnum.name(), flow))
                .thenReturn(Optional.of(entity));
    }

    private void assertBusinessException(
            BusinessException exception,
            ErrorCodeEnum expectedErrorCode,
            String expectedFlow
    ) {
        assertThat(exception.getErrorCodeEnum()).isEqualTo(expectedErrorCode);
        assertThat(exception.getFlow()).isEqualTo(expectedFlow);
    }
}
