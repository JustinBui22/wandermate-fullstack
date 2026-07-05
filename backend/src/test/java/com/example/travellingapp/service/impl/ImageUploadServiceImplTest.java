package com.example.travellingapp.service.impl;

import com.example.travellingapp.dto.response.ImageUploadResponseDTO;
import com.example.travellingapp.entity.ErrorCodeEntity;
import com.example.travellingapp.entity.User;
import com.example.travellingapp.enums.ErrorCodeEnum;
import com.example.travellingapp.exception_handler.exception.BusinessException;
import com.example.travellingapp.repository.ErrorCodeRepository;
import com.example.travellingapp.repository.UserRepository;
import com.example.travellingapp.response_template.CompleteResponse;
import com.example.travellingapp.security.data_security.AuthenticatedUserProvider;
import com.example.travellingapp.service.CloudinaryImageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static com.example.travellingapp.enums.ErrorCodeEnum.USER_NOT_FOUND;
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
    private UserRepository userRepository;

    @Mock
    private CloudinaryImageClient cloudinaryImageClient;

    private ImageUploadServiceImpl imageUploadService;

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "JustinBo123";

    @BeforeEach
    void setUp() {
        imageUploadService = new ImageUploadServiceImpl(
                errorCodeRepository,
                authenticatedUserProvider,
                cloudinaryImageClient,
                "wandermate",
                userRepository
        );
    }

    @Test
    void uploadImage_shouldUploadProfileImageToCloudinaryUserFolder_whenFileIsValid() throws IOException {
        MockMultipartFile file = validImage("avatar.png", "image/png");
        User currentUser = activeUser();

        ImageUploadResponseDTO cloudinaryResponse = new ImageUploadResponseDTO(
                "https://res.cloudinary.com/demo/image/upload/v123/wandermate/profile-images/users/1/profile-1-abc.png",
                "wandermate/profile-images/users/1/profile-1-abc",
                "wandermate/profile-images/users/1/profile-1-abc",
                "profile-images"
        );

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());
        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME)).thenReturn(Optional.of(currentUser));
        when(cloudinaryImageClient.uploadImage(
                any(byte[].class),
                startsWith("profile-1-"),
                eq("wandermate/profile-images/users/1"),
                eq("profile-images")
        )).thenReturn(cloudinaryResponse);

        CompleteResponse<Object> response = imageUploadService.uploadImage(file, "profile-images");

        assertThat(response.getHttpCode()).isEqualTo(200);
        assertThat(response.getResponseBody().getCode()).isEqualTo(SEARCH_INFO_SUCCESS.getCode());
        assertThat(response.getResponseBody().getFlow()).isEqualTo(COMMON.name());
        assertThat(response.getResponseBody().getBody()).isEqualTo(cloudinaryResponse);

        ArgumentCaptor<String> publicIdCaptor = ArgumentCaptor.forClass(String.class);

        verify(cloudinaryImageClient).uploadImage(
                any(byte[].class),
                publicIdCaptor.capture(),
                eq("wandermate/profile-images/users/1"),
                eq("profile-images")
        );

        assertThat(publicIdCaptor.getValue()).startsWith("profile-1-");
    }

    @Test
    void uploadImage_shouldUploadTripCoverToCloudinaryUserFolder_whenImageTypeHasExtraSpacesAndUppercase() throws IOException {
        MockMultipartFile file = validImage("cover.jpeg", "image/jpeg");
        User currentUser = activeUser();

        ImageUploadResponseDTO cloudinaryResponse = new ImageUploadResponseDTO(
                "https://res.cloudinary.com/demo/image/upload/v123/wandermate/trip-covers/users/1/trip-cover-1-abc.jpg",
                "wandermate/trip-covers/users/1/trip-cover-1-abc",
                "wandermate/trip-covers/users/1/trip-cover-1-abc",
                "trip-covers"
        );

        mockErrorCode(SEARCH_INFO_SUCCESS, COMMON.name());
        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME)).thenReturn(Optional.of(currentUser));
        when(cloudinaryImageClient.uploadImage(
                any(byte[].class),
                startsWith("trip-cover-1-"),
                eq("wandermate/trip-covers/users/1"),
                eq("trip-covers")
        )).thenReturn(cloudinaryResponse);

        CompleteResponse<Object> response = imageUploadService.uploadImage(file, "  TRIP-COVERS  ");

        ImageUploadResponseDTO body =
                (ImageUploadResponseDTO) response.getResponseBody().getBody();

        assertThat(body.getImageUrl()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v123/wandermate/trip-covers/users/1/trip-cover-1-abc.jpg");
        assertThat(body.getPublicId()).isEqualTo("wandermate/trip-covers/users/1/trip-cover-1-abc");
        assertThat(body.getFileName()).isEqualTo("wandermate/trip-covers/users/1/trip-cover-1-abc");
        assertThat(body.getImageType()).isEqualTo("trip-covers");

        verify(cloudinaryImageClient).uploadImage(
                any(byte[].class),
                startsWith("trip-cover-1-"),
                eq("wandermate/trip-covers/users/1"),
                eq("trip-covers")
        );
    }

    @Test
    void uploadImage_shouldThrowUserNotFound_whenAuthenticatedUserDoesNotExist() throws IOException {
        MockMultipartFile file = validImage("avatar.png", "image/png");

        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, USER_NOT_FOUND, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenFileIsNull() throws IOException {
        mockCurrentUser();

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

        mockCurrentUser();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INVALID_INPUT, COMMON.name());
        verify(cloudinaryImageClient, never()).uploadImage(any(), anyString(), anyString(), anyString());
    }

    @Test
    void uploadImage_shouldThrowInvalidInput_whenImageTypeIsUnsupported() throws IOException {
        MockMultipartFile file = validImage("avatar.png", "image/png");

        mockCurrentUser();

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

        mockCurrentUser();

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

        mockCurrentUser();

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

        mockCurrentUser();
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
        MockMultipartFile file = validImage("avatar.png", "image/png");

        mockCurrentUser();
        when(cloudinaryImageClient.uploadImage(
                any(byte[].class),
                startsWith("profile-1-"),
                eq("wandermate/profile-images/users/1"),
                eq("profile-images")
        )).thenThrow(new IOException("Cloudinary unavailable"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageUploadService.uploadImage(file, "profile-images")
        );

        assertBusinessException(exception, INTERNAL_SERVER_ERROR, COMMON.name());
    }

    private MockMultipartFile validImage(String filename, String contentType) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                "fake-image-content".getBytes()
        );
    }

    private User activeUser() {
        User user = new User();
        user.setUserId(USER_ID);
        user.setUsername(USERNAME);
        user.setEmail("justin@example.com");
        user.setActive(true);
        return user;
    }

    private void mockCurrentUser() {
        when(authenticatedUserProvider.getUsername()).thenReturn(USERNAME);
        when(userRepository.findByUsernameAndActive(USERNAME)).thenReturn(Optional.of(activeUser()));
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
